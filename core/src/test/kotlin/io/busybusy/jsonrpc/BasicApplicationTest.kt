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

    @Test
    fun `it responds on custom rpc error`() {
        val application = BasicApplication.create {
            method("myError") { CustomErrorHandler() }
        }

        val response = application.handle("""
            {
                "jsonrpc": "2.0",
                "id": "foo",
                "method": "myError",
                "params": "my little pony"
            }
        """.trimIndent())

        assertEquals("""
            {"id":"foo","error":{"code":-32000,"message":"Your message was `my little pony`"},"jsonrpc":"2.0"}
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

    class CustomErrorHandler : Handler<String, Unit> {
        override fun invoke(p1: String) {
            throw MyRpcException("Your message was `$p1`")
        }

        class MyRpcException(message: String) : CustomRpcException(-32000, message)
    }
}
