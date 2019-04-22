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

package io.busybusy.jsonrpc

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Type
import kotlin.reflect.KCallable
import kotlin.reflect.KType
import kotlin.reflect.KVisibility
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmErasure

class HandlerRoute<Parameters : Any, Output : Any>(
    private val handlerFactory: () -> Handler<Parameters, Output>
) : Route<Parameters, Output> {
    override fun createSession() = HandlerRouteSession.create(handlerFactory())
}

/**
 * {@code RouteSession} implementation that uses on-demand reflection to determine the {@code RouteSession::inputType}
 * and the {@code RouteSession::outputType}
 */
class HandlerRouteSession<Parameters : Any, Output : Any> private constructor(
    private val handler: Handler<Parameters, Output>,
    private val method: KCallable<Output>,
    override val inputType: KType,
    override val outputType: KType
) : RouteSession<Parameters, Output> {
    private val requestType: Type get() = when (inputType.jvmErasure) {
        Unit::class -> UnitRequest::class.java
        else -> Types.newParameterizedType(FullRequest::class.java, inputType.javaType)
    }
    private val responseType: Type get() = Types.newParameterizedType(SuccessResponse::class.java, outputType.javaType)

    override fun createRequestAdapter(moshi: Moshi): JsonAdapter<RequestWithParameters<Parameters>> = moshi.adapter(requestType)

    override fun createResponse(id: String?, result: Output) = SuccessResponse(id, result)
    override fun createResponseAdapter(moshi: Moshi): JsonAdapter<ResponseWithResult<Output>> = moshi.adapter(responseType)

    /**
     * Executes the handler through reflection. A {@code InvocationTargetException} exception is thrown when
     * {@code java.reflect.Method::call} gets an exception back from the target method. This exception should be unwrapped
     * and rethrown as normal.
     */
    override fun invoke(params: Parameters): Output = try {
        method.call(handler, params)
    } catch (failure: InvocationTargetException) {
        throw failure.targetException
    }

    companion object {
        /**
         * Constructs a {@code HandlerRouteSession} with the full typing information determined via the {@code Handler}
         * interface implementation.
         */
        fun <Parameters : Any, Output : Any> create(handler: Handler<Parameters, Output>) = run {
            val handlerErasure = Handler::class
            val handlerClass = handler::class

            val types = handlerClass.allSupertypes.first { handlerErasure == it.jvmErasure }.arguments
            assert(types.size == 2)

            val inputType = types[0].type!!
            val outputType = types[1].type!!

            /**
             * This search ensures that the referenced invoke method is the implementation of {@code Handler::invoke}
             */
            @Suppress("UNCHECKED_CAST")
            val method = handlerClass.memberFunctions.first {
                it.run {
                    name == "invoke" &&
                        valueParameters.size == 1 &&
                        visibility == KVisibility.PUBLIC &&
                        valueParameters[0].type == inputType &&
                        returnType == outputType
                }
            } as KCallable<Output>

            HandlerRouteSession(handler, method, inputType, outputType)
        }
    }
}
