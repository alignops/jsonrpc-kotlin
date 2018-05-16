package io.busybusy.jsonrpc

data class PeekRequest(
    override val jsonrpc: String,
    override val id: String?,
    override val method: String
) : Request

/**
 * Unit request is a special case in which the Handler specifies `Unit` as the input, meaning parameters are ignored
 */
data class UnitRequest(
    override val jsonrpc: String,
    override val id: String?,
    override val method: String,
    override val params: Unit = Unit
) : RequestWithParameters<Unit>

data class FullRequest<out Parameters : Any>(
    override val jsonrpc: String,
    override val id: String?,
    override val method: String,
    override val params: Parameters
) : RequestWithParameters<Parameters>
