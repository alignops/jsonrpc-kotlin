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