package io.busybusy.jsonrpc

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

internal class UnitAdapterTest {
    @Test
    fun `it reads Unit regardless of the value`() {
        assertSame(Unit, UnitAdapter.fromJson("null"))
        assertSame(Unit, UnitAdapter.fromJson("\"foo\""))
        assertSame(Unit, UnitAdapter.fromJson("100"))
        assertSame(Unit, UnitAdapter.fromJson("100.0"))
        assertSame(Unit, UnitAdapter.fromJson("{}"))
        assertSame(Unit, UnitAdapter.fromJson("""{"foo":"bar"}"""))
        assertSame(Unit, UnitAdapter.fromJson("[]"))
        assertSame(Unit, UnitAdapter.fromJson("""["foo","bar"]"""))
    }

    @Test
    fun `it writes Unit? as null`() {
        assertEquals("null", UnitAdapter.toJson(null))
        assertEquals("null", UnitAdapter.toJson(Unit))
    }

    @Test
    fun `it always writes null even in nested contexts`() {
        val adapter = Moshi.Builder().apply {
            add(KotlinJsonAdapterFactory())
            add(Unit::class.java, UnitAdapter)
        }.build().adapter(Foo::class.java)

        assertEquals("""{"foo":null,"bar":null}""", adapter.toJson(Foo()))
    }

    data class Foo(val foo: Unit = Unit, val bar: Unit = Unit)
}
