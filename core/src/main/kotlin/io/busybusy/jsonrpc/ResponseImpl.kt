/**
 * Copyright 2018 busybusy, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    override val error: Details,
    override val jsonrpc: String = "2.0"
) : ResponseWithError {
    open class Details(override val code: Int, override val message: String, override val data: Any?) : ResponseWithError.Details
}

/**
 * Provided implementation of {@code ResponseWithError.Details} for pre-defined codes as specified by JSON-RPC 2.0
 */
class PredefinedError(
    code: Codes,
    message: String,
    data: Any? = null
) : ErrorResponse.Details(code.value, message, data) {
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
class CustomError(
    code: Int,
    message: String,
    data: Any? = null
) : ErrorResponse.Details(code, message, data) {
    init {
        require(code in -32000 downTo -32099) { "Custom errors must have a code between -32000 and -32099" }
    }
}
