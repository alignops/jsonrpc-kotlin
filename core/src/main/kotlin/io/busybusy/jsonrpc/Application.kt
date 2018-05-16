package io.busybusy.jsonrpc

import okio.BufferedSink
import okio.BufferedSource

interface Application {
    fun handle(source: BufferedSource, sink: BufferedSink)
}

