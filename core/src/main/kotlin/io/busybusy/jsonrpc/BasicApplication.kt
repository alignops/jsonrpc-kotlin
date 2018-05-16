package io.busybusy.jsonrpc

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import okio.Buffer
import okio.BufferedSink
import okio.BufferedSource

/**
 * A basic application that handles a single incoming request (no JSON-RPC batch support)
 *
 * Example with `id`:
 *
 * Input:
 * """
 * {
 *     "jsonrpc": "2.0",
 *     "id": "my-example",
 *     "method": "foo",
 *     "params": {
 *         "bar": "sample",
 *         "baz": "quaz"
 *     }
 * }
 * """
 *
 * Output:
 * """
 * {
 *     "jsonrpc": "2.0",
 *     "id": "my-example",
 *     "result": true
 * }
 * """
 *
 * Example without `id` (aka notification):
 *
 * Input:
 * """
 * {
 *     "jsonrpc": "2.0",
 *     "method": "foo",
 *     "params": {
 *         "bar": "sample",
 *         "baz": "quaz"
 *     }
 * }
 * """
 *
 * Output:
 * """
 * """
 */
class BasicApplication private constructor(
    private val routes: Map<String, Route<Any, Any>>,
    private val moshi: Moshi
) : Application {
    /**
     * Every incoming request
     */
    private val peekAdapter = moshi.adapter<PeekRequest>()

    /**
     * An adapter for JSON-RPC errors. Configured for {@code ErrorResponse} since the {@code RpcException.toResponse()}
     * helper method creates instances of that type.
     */
    private val errorAdapter get() = moshi.adapter<ResponseWithError>(ErrorResponse::class.java)

    override fun handle(source: BufferedSource, sink: BufferedSink) {
        /**
         * Create a copy of the source for parsing by multiple adapters
         */
        val (peekSource, fullSource) = source.copyTo(Buffer(), Buffer())

        /**
         * Attempt to parse the request to get the minimum routing information
         * TODO: should the fullSource be closed if an exception is caught?
         */
        val peek = try {
            parsePeekRequest(peekSource)
        } catch (rpcException: RpcException) {
            errorAdapter.toJson(sink, rpcException.toResponse(id = null))
            return
        } catch (other: Throwable) {
            errorAdapter.toJson(sink, other.toInternalErrorResponse(id = null))
            return
        }

        /**
         * Attempt to execute the request through the routing handler
         * A response MUST NOT be written to the {@code sink} if the {@code peek.id} is null (a JSON-RPC notification)
         */
        try {
            val session = loadRoute(peek.method)
            val result = routeRequest(session, fullSource)

            if (peek.id != null) {
                /**
                 * Build the response into the implementation used by the session
                 */
                val response = session.createResponse(peek.id, result)

                /**
                 * Write the response using a qualified adapter for the response implementation used by the session
                 */
                session.createResponseAdapter(moshi).toJson(sink, response)
            }
        } catch (rpcException: RpcException) {
            if (peek.id != null) {
                errorAdapter.toJson(sink, rpcException.toResponse(peek.id))
            }
        } catch (other: Throwable) {
            if (peek.id != null) {
                errorAdapter.toJson(sink, other.toInternalErrorResponse(peek.id))
            }
        }
    }

    /**
     * Attempts to load the route by method name from the {@code routes}
     */
    private fun loadRoute(method: String) = routes[method]?.createSession()
        ?: throw MethodNotFoundException("Route was not found for method `$method`")

    /**
     * Dispatch the {@code session} after deserializing the request {@code source}
     */
    private fun routeRequest(session: RouteSession<Any, Any>, source: BufferedSource): Any {
        val request = parseFullRequest(session, source)
        return session(request.params)
    }

    /**
     * Parses a request from an adapter that's prepared for the fully qualified input type of the handler
     */
    private fun parseFullRequest(session: RouteSession<Any, Any>, source: BufferedSource) = source.parseRequest(session.createRequestAdapter(moshi)) {
        InvalidParametersException(it.messageFromValidation("Invalid parameters were provided"), previous = it)
    }

    /**
     * Parses a request that one contains identifying information about the request
     */
    private fun parsePeekRequest(source: BufferedSource) = source.parseRequest(peekAdapter) {
        InvalidRequestException(it.messageFromValidation("Invalid JSON-RPC request provided"), previous = it)
    }

    /**
     * Common abstraction of catch blocks for parsing an incoming request using a specific {@code adapter}
     */
    private inline fun <T : Request> BufferedSource.parseRequest(
        adapter: JsonAdapter<T>,
        invalidException: (Throwable) -> RpcException
    ): T = try {
        adapter.fromJsonOrFail(this)
    } catch (encoding: JsonEncodingException) {
        throw JsonParseException("Invalid JSON was provided", previous = encoding)
    } catch (data: JsonDataException) {
        throw invalidException(data)
    } catch (validation: IllegalArgumentException) {
        throw invalidException(validation)
    } finally {
        close()
    }

    private fun RpcException.toResponse(id: String?): ResponseWithError = ErrorResponse(id, details)
    private fun Throwable.toInternalErrorResponse(id: String?): ResponseWithError = InternalErrorException("An exception has occurred", previous = this).toResponse(id)
    private fun Throwable.messageFromValidation(fallback: String) = (this as? IllegalArgumentException)?.message ?: fallback

    companion object {
        fun create(
            moshi: Moshi = defaultMoshiBuilder().build(),
            configure: Configure.() -> Unit
        ) = BasicApplication(Configure().apply(configure).routes, moshi)
    }

    class Configure internal constructor() {
        internal val routes = mutableMapOf<String, Route<Any, Any>>()

        /**
         * Register a new method by dispatching via a hard-link to the route of an existing method
         *
         * Because this method creates a hard-link reference to the route, overwriting the existing method a different
         * handler will have no effect on this alias.
         *
         * @throws IllegalArgumentException if the target method does not exist
         *
         * Example:
         *
         * ```
         * handle<Int, String>("intToString") {
         *     it.toString()
         * }
         *
         * alias("numberAsString" to "intToString")
         * ```
         */
        @Throws(IllegalArgumentException::class)
        fun alias(newToExisting: Pair<String, String>) {
            require(routes.contains(newToExisting.second)) { "Cannot alias new `${newToExisting.first}` route to unknown `${newToExisting.second}` route" }
            routes[newToExisting.first] = routes[newToExisting.second]!!
        }

        /**
         * Register a new method that dispatches to the handler provided by the {@code factory} lambda.
         *
         * This method will overwrite any existing registered routed.
         *
         * Example:
         *
         * ```
         * class ParseDateTime : Handler<String, LocalDateTime> {
         *     override fun invoke(provided: String) = try {
         *         LocalDateTime.parse(provided)
         *     } catch (failed: Throwable) {
         *         throw FailedToParseException(failed)
         *     }
         *
         *     class FailedToParseException(previous: Throwable) : CustomRpcException(-32000, "Provided date time string could not be parsed", previous = previous)
         * }
         *
         * val app = BasicApplication.create {
         *     method("stringToDateTime") {
         *         ParseDateTime()
         *     }
         * }
         * ```
         */
        @Suppress("UNCHECKED_CAST")
        fun method(name: String, factory: () -> Handler<*, *>) {
            routes[name] = HandlerRoute(factory) as Route<Any, Any>
        }
    }
}
