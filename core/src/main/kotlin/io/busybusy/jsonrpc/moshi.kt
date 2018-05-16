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