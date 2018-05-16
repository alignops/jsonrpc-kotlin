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
import org.junit.jupiter.api.assertThrows

internal class RpcExceptionTest {
    @Test
    fun `JsonParseException returns code -32700`() {
        JsonParseException("bad stuff") `should have code` -32700
    }

    @Test
    fun `InvalidRequestException returns code -32600`() {
        InvalidRequestException("bad stuff") `should have code` -32600
    }

    @Test
    fun `MethodNotFoundException returns code -32601`() {
        MethodNotFoundException("bad stuff") `should have code` -32601
    }

    @Test
    fun `InvalidParametersException returns code -32602`() {
        InvalidParametersException("bad stuff") `should have code` -32602
    }

    @Test
    fun `InternalErrorException returns code -32603`() {
        InternalErrorException("bad stuff") `should have code` -32603
    }

    @Test
    fun `CustomRpcException allows and returns codes between -32000 and -32099`() {
        (-32000 downTo -32099).forEach {
            TestingCustomRpcException(it) `should have code` it
        }
    }

    @Test
    fun `CustomRpcException raises an IllegalArgumentException for a code outside -32000 and -32099`() {
        // Above the maximum bound
        assertThrows<IllegalArgumentException> {
            TestingCustomRpcException(-31999)
        }

        // Below the minimum bound
        assertThrows<IllegalArgumentException> {
            TestingCustomRpcException(-32100)
        }
    }

    private class TestingCustomRpcException(code: Int) : CustomRpcException(code, "bad stuff")
    private infix fun RpcException.`should have code`(expected: Int) = assertEquals(expected, details.code)
}