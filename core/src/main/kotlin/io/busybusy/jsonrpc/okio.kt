@file:JvmName("OkioUtils")
package io.busybusy.jsonrpc

import okio.BufferedSink
import okio.BufferedSource
import okio.Okio
import java.io.InputStream
import java.io.OutputStream

internal fun InputStream.toBufferedSource(): BufferedSource = Okio.buffer(Okio.source(this))
internal fun OutputStream.toBufferedSink(): BufferedSink = Okio.buffer(Okio.sink(this))

/**
 * Reads the Okio source and copies it into each of the provided Okio sinks
 */
internal fun <T : BufferedSink> BufferedSource.copyTo(vararg buffers: T): Array<out T> {
    val SIZE = 8192
    val segment = ByteArray(SIZE)
    while (!exhausted()) {
        val bytesRead = read(segment, 0, SIZE)
        buffers.forEach { it.write(segment, 0, bytesRead) }
    }
    close()
    return buffers
}