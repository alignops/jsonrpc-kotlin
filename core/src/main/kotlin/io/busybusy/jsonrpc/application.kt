@file:JvmName("ApplicationUtils")
package io.busybusy.jsonrpc

import okio.Buffer
import java.io.InputStream
import java.io.OutputStream

fun Application.handle(input: InputStream, output: OutputStream) {
    val outputBuffer = output.toBufferedSink()

    handle(input.toBufferedSource(), outputBuffer)

    /**
     * We must flush {@code outputBuffer} so the result is written to {@code output}
     */
    outputBuffer.flush()
}

fun Application.handle(input: ByteArray) = Buffer().also {
    handle(input.inputStream().toBufferedSource(), it)
}

fun Application.handle(input: String) = handle(input.toByteArray())
