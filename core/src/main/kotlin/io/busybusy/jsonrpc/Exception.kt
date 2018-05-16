package io.busybusy.jsonrpc

sealed class RpcException(
    val details: ErrorResponse.Details,
    previous: Throwable? = null
) : Exception(details.message, previous)

/**
 * Invalid JSON was received by the server.
 * An error occurred on the server while parsing the JSON text.
 */
class JsonParseException(
    message: String,
    data: Any? = null,
    previous: Throwable? = null
) : RpcException(PredefinedError(PredefinedError.Codes.PARSE_ERROR, message, data), previous)

/**
 * The JSON sent is not a valid Request object.
 */
class InvalidRequestException(
    message: String,
    data: Any? = null,
    previous: Throwable? = null
) : RpcException(PredefinedError(PredefinedError.Codes.INVALID_REQUEST, message, data), previous)

/**
 * The method does not exist / is not available.
 */
class MethodNotFoundException(
    message: String,
    data: Any? = null,
    previous: Throwable? = null
) : RpcException(PredefinedError(PredefinedError.Codes.METHOD_NOT_FOUND, message, data), previous)

/**
 * Invalid method parameter(s).
 */
class InvalidParametersException(
    message: String,
    data: Any? = null,
    previous: Throwable? = null
) : RpcException(PredefinedError(PredefinedError.Codes.INVALID_PARAMS, message, data), previous)

/**
 * Internal JSON-RPC error.
 */
class InternalErrorException(
    message: String,
    data: Any? = null,
    previous: Throwable? = null
) : RpcException(PredefinedError(PredefinedError.Codes.INTERNAL_ERROR, message, data), previous)

/**
 * Reserved for implementation-defined server-errors.
 */
abstract class CustomRpcException(
    details: CustomError,
    previous: Throwable? = null
) : RpcException(details, previous) {
    /**
     * {@code code} must be between -32000 and -32099
     */
    constructor(
        code: Int,
        message: String,
        data: Any? = null,
        previous: Throwable? = null
    ) : this(CustomError(code, message, data), previous)
}
