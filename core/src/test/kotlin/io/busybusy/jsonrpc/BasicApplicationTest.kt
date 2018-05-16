package io.busybusy.jsonrpc

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class BasicApplicationTest {
    @Test
    fun `it operates with a very basic handler configuration`() {
        val application = BasicApplication.create {
            method("bar") {
                StringToIntHandler()
            }
        }

        val response = application.handle("""
            {
                "jsonrpc": "2.0",
                "id": "foo",
                "method": "bar",
                "params": "100"
            }
        """.trimIndent())

        assertEquals("""
            {"id":"foo","result":100,"jsonrpc":"2.0"}
        """.trimIndent(), response.readUtf8())
    }

    @Test
    fun `it responds on bad json`() {
        val application = BasicApplication.create { }

        val response = application.handle("""
            {
                "jsonrpc": "2.0
            }
        """.trimIndent())

        assertEquals("""
            {"error":{"code":-32700,"message":"Invalid JSON was provided"},"jsonrpc":"2.0"}
        """.trimIndent(), response.readUtf8())
    }

    @Test
    fun `it responds on an invalid request`() {
        val application = BasicApplication.create { }
        val response = application.handle("""
            {
                "jsonrpc": "2.0"
            }
        """.trimIndent())

        assertEquals("""
            {"error":{"code":-32600,"message":"Invalid JSON-RPC request provided"},"jsonrpc":"2.0"}
        """.trimIndent(), response.readUtf8())
    }

    @Test
    fun `it responds on unknown method`() {
        val application = BasicApplication.create { }

        val response = application.handle("""
            {
                "jsonrpc": "2.0",
                "id": "foo",
                "method": "bar"
            }
        """.trimIndent())

        assertEquals("""
            {"id":"foo","error":{"code":-32601,"message":"Route was not found for method `bar`"},"jsonrpc":"2.0"}
        """.trimIndent(), response.readUtf8())
    }

    @Test
    fun `it responds on invalid parameters`() {
        val application = BasicApplication.create {
            method("bar") { CustomParametersHandler() }
        }

        val response = application.handle("""
            {
                "jsonrpc": "2.0",
                "id": "foo",
                "method": "bar"
            }
        """.trimIndent())

        assertEquals("""
            {"id":"foo","error":{"code":-32602,"message":"Invalid parameters were provided"},"jsonrpc":"2.0"}
        """.trimIndent(), response.readUtf8())
    }

    @Test
    fun `it responds on internal errors`() {
        val application = BasicApplication.create {
            method("fatal") { FatalHandler() }
        }

        val response = application.handle("""
            {
                "jsonrpc": "2.0",
                "id": "foo",
                "method": "fatal"
            }
        """.trimIndent())

        assertEquals("""
            {"id":"foo","error":{"code":-32603,"message":"An exception has occurred"},"jsonrpc":"2.0"}
        """.trimIndent(), response.readUtf8())
    }

    class StringToIntHandler : Handler<String, Int> {
        override fun invoke(p1: String) = p1.toInt()
    }

    class FatalHandler : Handler<Unit, Unit> {
        override fun invoke(p1: Unit) {
            check(false)
        }
    }

    class CustomParametersHandler : Handler<CustomParametersHandler.Parameters, Unit> {
        override fun invoke(p1: Parameters) {
        }

        data class Parameters(val baz: String) {
            init {
                require(baz.isNotEmpty())
            }
        }
    }
}