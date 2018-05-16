package io.busybusy.jsonrpc

/**
 * A basic request has the minimal information required for a JSON-RPC call to be successfully dispatched to a method
 */
interface Request {
    val jsonrpc: String
    val id: String?
    val method: String
}

interface RequestWithParameters<out Parameters : Any> : Request {
    val params: Parameters
}

