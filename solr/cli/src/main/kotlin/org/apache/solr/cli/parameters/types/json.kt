/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.solr.cli.parameters.types

import com.github.ajalt.clikt.parameters.arguments.ProcessedArgument
import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.NullableOption
import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.transform.TransformContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

private val conversion: TransformContext.(String, Json) -> JsonObject =
    { value, json -> json.parseToJsonElement(value).jsonObject }

/**
 * Convert the argument values to a [JsonObject].
 */
fun RawArgument.json(json: Json = Json): ProcessedArgument<JsonObject, JsonObject> =
    convert(conversion = { conversion(it, json) })

/**
 * Convert the option values to a [JsonObject].
 *
 * @param json Json configuration to use for parsing.
 */
fun RawOption.json(json: Json = Json): NullableOption<JsonObject, JsonObject> {
    return convert({ localization.intMetavar() }, conversion = { conversion(it, json) })
}
