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
