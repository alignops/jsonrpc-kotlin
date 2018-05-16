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
@file:JvmName("MoshiUtils")

package io.busybusy.jsonrpc

import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okio.BufferedSource

internal inline fun <reified T : Any> Moshi.adapter(): JsonAdapter<T> = adapter(T::class.java)

/**
 * Helper method to convert nulls to exceptions when parsing with Moshi
 */
internal fun <T : Any> JsonAdapter<T>.fromJsonOrFail(source: BufferedSource): T
    = fromJson(source) ?: throw JsonDataException("Adapter returned null")

fun defaultMoshiBuilder() = Moshi.Builder().apply {
    add(KotlinJsonAdapterFactory())
    add(Unit::class.java, UnitAdapter)
}

internal object UnitAdapter : JsonAdapter<Unit>() {
    override fun fromJson(reader: JsonReader) = Unit.also { reader.skipValue() }
    override fun toJson(writer: JsonWriter, value: Unit?) {
        writer.serializeNullsDuring { nullValue() }
    }

    private inline fun JsonWriter.serializeNullsDuring(block: JsonWriter.() -> Unit) {
        serializeNulls.also {
            serializeNulls = true
            block()
            serializeNulls = it
        }
    }
}