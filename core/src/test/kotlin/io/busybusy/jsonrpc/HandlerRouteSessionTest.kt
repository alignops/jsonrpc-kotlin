package io.busybusy.jsonrpc

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.reflect.jvm.jvmErasure

internal class HandlerRouteSessionTest {
    @Test
    fun `it can be created with an anonymous object with the expected type information`() {
        val session = HandlerRouteSession.create(object : Handler<String, Int> {
            override fun invoke(p1: String) = p1.toInt()
        })

        assertEquals(String::class, session.inputType.jvmErasure)
        assertEquals(Int::class, session.outputType.jvmErasure)

        assertEquals(100, session("100"))
        assertEquals(1000, session("1000"))
    }

    @Test
    fun `it is created with a reference to the correct single parameter invoke method`() {
        val session = HandlerRouteSession.create(object : Handler<Int, Int> {
            override fun invoke(p1: Int) = p1 * 10
            operator fun invoke(p1: String) = p1
        })

        assertEquals(Int::class, session.inputType.jvmErasure)
        assertEquals(Int::class, session.outputType.jvmErasure)

        assertEquals(100, session(10))
        assertEquals(1000, session(100))
    }

    @Test
    fun `it is created with a reference to the correct invoke method through inheritance`() {
        val session = HandlerRouteSession.create(object : HandlerViaImplementation() {
            operator fun invoke(p1: String) = p1
        })

        assertEquals(Int::class, session.inputType.jvmErasure)
        assertEquals(String::class, session.outputType.jvmErasure)

        assertEquals("100", session(100))
    }

    @Test
    fun `it creates a response adapter for basic types with expected serialization`() {
        HandlerRouteSession.create(object : Handler<Unit, String> {
            override fun invoke(p1: Unit) = "That's my story"
        }) `should create result` "\"That's my story\""
    }

    @Test
    fun `it creates a response adapter for parameterized generic types`() {
        HandlerRouteSession.create(object : Handler<Unit, Array<String>> {
            override fun invoke(p1: Unit) = arrayOf("A", "B", "C")
        }) `should create result` """["A","B","C"]"""
    }

    @Test
    fun `it creates a response adapter for a type with an owner`() {
        HandlerRouteSession.create(object : Handler<Unit, OwnedType> {
            override fun invoke(p1: Unit) = OwnedType("bar")
        }) `should create result` """{"foo":"bar"}"""
    }

    @Test
    fun `it creates a response adapter for a generic type with an owner`() {
        HandlerRouteSession.create(object : Handler<Unit, OwnedGenericType<Int>> {
            override fun invoke(p1: Unit) = OwnedGenericType(100)
        }) `should create result` """{"foo":100}"""
    }

    @Test
    fun `it creates a response adapter for a generic type with an owner and multiple parameters`() {
        HandlerRouteSession.create(object : Handler<Unit, ComplexOwnedGenericType<Int, String>> {
            override fun invoke(p1: Unit) = ComplexOwnedGenericType(100, "baz")
        }) `should create result` """{"foo":100,"bar":"baz"}"""
    }

    @Test
    fun `it creates a response adapter for a Unit response with the default moshi builder`() {
        HandlerRouteSession.create(object : Handler<Unit, Unit> {
            override fun invoke(p1: Unit) {}
        }) `should create result` "null"
    }

    @Test
    fun `it creates a request adapter for basic types`() {
        HandlerRouteSession.create(object : Handler<String, Unit> {
            override fun invoke(p1: String) {
                assertEquals("foo", p1)
            }
        }) `should receive params` "\"foo\""
    }

    @Test
    fun `it creates a request adapter for basic generic types`() {
        HandlerRouteSession.create(object : Handler<Array<String>, Unit> {
            override fun invoke(p1: Array<String>) {
                assertEquals(arrayOf("A", "B").toList(), p1.toList())
            }
        }) `should receive params` """
            ["A", "B"]
        """.trimIndent()
    }

    @Test
    fun `it creates a request adapter for a type with an owner`() {
        HandlerRouteSession.create(object : Handler<OwnedType, Unit> {
            override fun invoke(p1: OwnedType) {
                assertEquals("bar", p1.foo)
            }
        }) `should receive params` """
            {"foo": "bar"}
        """.trimIndent()
    }

    @Test
    fun `it creates a request adapter for a generic type with an owner`() {
        HandlerRouteSession.create(object : Handler<OwnedGenericType<Int>, Unit> {
            override fun invoke(p1: OwnedGenericType<Int>) {
                assertEquals(100, p1.foo)
            }
        }) `should receive params` """
            {"foo": 100}
        """.trimIndent()
    }

    @Test
    fun `it creates a request adapter for a generic type with an owner and multiple parameters`() {
        HandlerRouteSession.create(object : Handler<ComplexOwnedGenericType<Double, String>, Unit> {
            override fun invoke(p1: ComplexOwnedGenericType<Double, String>) {
                assertEquals(8.4, p1.foo)
                assertEquals("baz", p1.bar)
            }
        }) `should receive params` """
            {
                "foo": 8.4,
                "bar": "baz"
            }
        """.trimIndent()
    }

    @Test
    fun `it creates a special request adapter to allow for optional parameters when request type is Unit`() {
        val session = HandlerRouteSession.create(object : Handler<Unit, Unit> {
            override fun invoke(p1: Unit) {}
        })

        val adapter = session.createRequestAdapter(moshi)

        infix fun JsonAdapter<RequestWithParameters<Unit>>.`creates a UnitRequest for`(request: String) {
            fromJson(request)!!.also {
                assertNotNull(it as? UnitRequest)
                session(it.params)
            }
        }

        adapter `creates a UnitRequest for` """
            {
                "jsonrpc": "2.0",
                "id": "foo",
                "method": "bar"
            }
        """.trimIndent()

        adapter `creates a UnitRequest for` """
            {
                "jsonrpc": "2.0",
                "id": "foo",
                "method": "bar",
                "params": null
            }
        """.trimIndent()

        // Even if a real value is provided, the params should be ignored and Unit should still be provided
        adapter `creates a UnitRequest for` """
            {
                "jsonrpc": "2.0",
                "id": "foo",
                "method": "baz",
                "params": {
                    "some": "quaz"
                }
            }
        """.trimIndent()
    }

    private infix fun <Output : Any> HandlerRouteSession<Unit, Output>.`should create result`(result: String) {
        assertEquals(
            """{"id":"foo","result":$result,"jsonrpc":"2.0"}""",
            createResponseAdapter(moshi).toJson(createResponse("foo", invoke(Unit)))
        )
    }

    private infix fun <Input : Any> HandlerRouteSession<Input, Unit>.`should receive params`(params: String) {
        val random = Random()
        val id = "id-${random.nextInt()}"
        val method = "method-${random.nextInt()}"
        invoke(createRequestAdapter(moshi).fromJson("""
            {
                "jsonrpc": "2.0",
                "id": "$id",
                "method": "$method",
                "params": $params
            }
        """.trimIndent())!!.let {
            assertEquals("2.0", it.jsonrpc)
            assertEquals(id, it.id)
            assertEquals(method, it.method)
            it.params
        })
    }

    private val moshi: Moshi get() = defaultMoshiBuilder().build()

    data class OwnedType(val foo: String)
    data class OwnedGenericType<T>(val foo: T)
    data class ComplexOwnedGenericType<A, B>(val foo: A, val bar: B)

    open class HandlerViaImplementation : Handler<Int, String> {
        override fun invoke(p1: Int) = p1.toString()
    }
}