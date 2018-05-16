package io.busybusy.jsonrpc

/**
 * A handler encapsulates procedure logic based on its incoming {@code Parameters}
 */
interface Handler<in Parameters : Any, out Output : Any> : (Parameters) -> Output