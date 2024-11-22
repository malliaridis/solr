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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

private val conversion: TransformContext.(String, Json) -> JsonElement = { data, json ->
    val element = json.parseToJsonElement(data)
    if (element is JsonPrimitive) {
        val content = element.content
        if (content.contains("=")) {
            // Case of key=value
            val key = content.substringBefore("=")
            val value = content.substringAfter("=")
            JsonObject(mapOf(key to JsonPrimitive(value)))
        } else JsonPrimitive(content)
    } else element // Fallback case
}

/**
 * Convert the argument values to a [JsonElement].
 */
fun RawArgument.actionValue(json: Json = Json): ProcessedArgument<JsonElement, JsonElement> =
    convert(conversion = { conversion(it, json) })

/**
 * Convert the option values to a [JsonElement].
 *
 * @param json Json configuration to use for parsing.
 */
fun RawOption.actionValue(json: Json = Json): NullableOption<JsonElement, JsonElement> {
    return convert({ localization.intMetavar() }, conversion = { conversion(it, json) })
}
