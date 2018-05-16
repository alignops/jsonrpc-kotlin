package io.busybusy.jsonrpc

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlin.reflect.KType

/**
 * A route should be a lightweight, inexpensive object that contains the minimal information needed to construct
 * a {@code RouteSession} for the provided type.
 */
interface Route<Parameters : Any, Output : Any> {
    fun createSession(): RouteSession<Parameters, Output>
}

/**
 * A route session is constructed after a request has come in and the route has been located.
 * This allows for more expensive computations or allocations to only be done when the route is actively in use.
 */
interface RouteSession<Parameters : Any, Output : Any> : (Parameters) -> Output {
    /**
     * The {@code KType} representation of {@code Parameters}
     */
    val inputType: KType

    /**
     * The {@code KType} representation of {@code Output}
     */
    val outputType: KType

    /**
     * Creates a {@code JsonAdapter} that's configured for {@code inputType}-specific requests
     */
    fun createRequestAdapter(moshi: Moshi): JsonAdapter<RequestWithParameters<Parameters>>

    /**
     * Creates a {@code JsonAdapter} that's configured for {@code outputType}-specific responses
     */
    fun createResponseAdapter(moshi: Moshi): JsonAdapter<ResponseWithResult<Output>>

    /**
     * Creates an implementation specific instance of a {@code ResponseWithResult}
     */
    fun createResponse(id: String?, result: Output): ResponseWithResult<Output>
}
