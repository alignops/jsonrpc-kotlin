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

import okio.BufferedSink
import okio.BufferedSource
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

internal class ApplicationUtilsTest {
    @Test
    fun `it accepts an InputStream and OutputStream`() {
        val sourceInt = 10_000
        val input = sourceInt.toString().byteInputStream()
        val output = ByteArrayOutputStream()

        testApplication.handle(input, output)

        assertEquals(sourceInt, output.toString().toInt())
    }

    @Test
    fun `it accepts a ByteArray and returns a Buffer`() {
        val sourceInt = 10_000
        val input = sourceInt.toString().toByteArray()

        val result = testApplication.handle(input)

        assertEquals(sourceInt, result.readUtf8().toInt())
    }

    @Test
    fun `it accepts a String and returns a Buffer`() {
        val sourceInt = 10_000
        val input = sourceInt.toString()

        val result = testApplication.handle(input)

        assertEquals(sourceInt, result.readUtf8().toInt())
    }

    private val testApplication = object : Application {
        override fun handle(source: BufferedSource, sink: BufferedSink) {
            sink.writeUtf8(source.readUtf8())
        }
    }
}
