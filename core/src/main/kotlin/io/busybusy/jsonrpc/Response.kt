package io.busybusy.jsonrpc

interface Response {
    val jsonrpc: String
    val id: String?
}

interface ResponseWithResult<out Output : Any> : Response {
    val result: Output
}

interface ResponseWithError : Response {
    val error: Details

    interface Details {
        val code: Int
        val message: String
        val data: Any?
    }
}
