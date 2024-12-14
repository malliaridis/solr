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

package org.apache.solr.cli.commands

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.groups.required
import com.github.ajalt.clikt.parameters.options.required
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.apache.solr.cli.EchoUtils.debug
import org.apache.solr.cli.EchoUtils.err
import org.apache.solr.cli.EchoUtils.success
import org.apache.solr.cli.options.CommonOptions.collectionNameOption
import org.apache.solr.cli.options.CommonOptions.connectionOptions
import org.apache.solr.cli.options.CommonOptions.credentialsOption
import org.apache.solr.cli.options.CommonOptions.verboseOption
import org.apache.solr.cli.parameters.types.actionValue
import org.apache.solr.cli.utils.Utils

class ConfigCommand : SuspendingCliktCommand(name = "config") {

    private val solrUrl by connectionOptions.required()

    private val credentials by credentialsOption

    private val collection by collectionNameOption.required()

    private val action by argument(name = "action")
        .help("Config API action.")

    private val value by argument("value")
        .help("Config API action value. May be any JSON object, key=value or string.")
        .actionValue()

    private val verbose by verboseOption

    override suspend fun run() {

        val actualValue = value

        if (action.startsWith("unset-") || action.startsWith("delete-")) {
            require(actualValue is JsonPrimitive && actualValue.isString) {
                "Value cannot be a json object or contain '=' if action is of type unset/delete."
            }
        } else require(value !is JsonPrimitive) { "Value must be key=value or json object." }

        val updateUrl = URLBuilder(solrUrl).apply {
            path("api", "collections", collection, "config")
        }.build()

        debug(verbose) { "POSTing request to Config API: $updateUrl" }

        Utils.createHttpClient(credentials).use { client ->
            val result = withContext(Dispatchers.IO) {
                client.post(updateUrl) {
                    setBody(JsonObject(mapOf(action to value)))
                }
            }

            val body = result.body<JsonObject>()

            debug(verbose) { "Response body: $body" }

            when (result.status) {
                HttpStatusCode.OK -> success("Config successfully updated.")
                HttpStatusCode.Created -> success("Config update successfully created.")
                HttpStatusCode.Accepted -> success("Config update successfully sent.")
                else -> err(message = "Failed with status ${result.status} : $body")
            }
        }
    }
}
