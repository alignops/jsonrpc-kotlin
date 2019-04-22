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

data class PeekRequest(
    override val jsonrpc: String,
    override val id: String?,
    override val method: String
) : Request

/**
 * Unit request is a special case in which the Handler specifies `Unit` as the input, meaning parameters are ignored
 */
data class UnitRequest(
    override val jsonrpc: String,
    override val id: String?,
    override val method: String,
    override val params: Unit = Unit
) : RequestWithParameters<Unit>

data class FullRequest<out Parameters : Any>(
    override val jsonrpc: String,
    override val id: String?,
    override val method: String,
    override val params: Parameters
) : RequestWithParameters<Parameters>
