package io.busybusy.jsonrpc

/**
 * Provided implementation of {@code ResponseWithResult}
 */
data class SuccessResponse<out Output : Any>(
    override val id: String?,
    override val result: Output,
    override val jsonrpc: String = "2.0"
) : ResponseWithResult<Output>

/**
 * Provided implementation of {@code ResponseWithError}
 */
data class ErrorResponse(
    override val id: String?,
    override val error: ResponseWithError.Details,
    override val jsonrpc: String = "2.0"
) : ResponseWithError

/**
 * Provided implementation of {@code ResponseWithError.Details} for pre-defined codes as specified by JSON-RPC 2.0
 */
class PredefinedError(
    code: Codes,
    override val message: String,
    override val data: Any? = null
) : ResponseWithError.Details {
    override val code: Int = code.value

    enum class Codes(val value: Int) {
        PARSE_ERROR(-32700),
        INVALID_REQUEST(-32600),
        METHOD_NOT_FOUND(-32601),
        INVALID_PARAMS(-32602),
        INTERNAL_ERROR(-32603)
    }
}

/**
 * Provided implementation of {@code ResponseWithError.Details} for the JSON-RPC 2.0 implementation-defined code range
 */
data class CustomError(
    override val code: Int,
    override val message: String,
    override val data: Any? = null
) : ResponseWithError.Details {
    init {
        require(code in -32000 downTo -32099) { "Custom errors must have a code between -32000 and -32099" }
    }
}
