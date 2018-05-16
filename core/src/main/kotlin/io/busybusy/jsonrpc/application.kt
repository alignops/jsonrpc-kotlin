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