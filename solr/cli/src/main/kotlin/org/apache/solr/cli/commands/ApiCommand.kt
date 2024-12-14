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
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.Url
import kotlinx.serialization.json.JsonObject
import org.apache.solr.cli.Constants
import org.apache.solr.cli.EchoUtils.info
import org.apache.solr.cli.options.CommonOptions.credentialsOption
import org.apache.solr.cli.options.CommonOptions.solrUrlOption
import org.apache.solr.cli.utils.Utils
import org.apache.solr.cli.utils.toPrettyString

class ApiCommand : SuspendingCliktCommand(name = "api") {

    private val solrUrl by solrUrlOption.help("Send a GET request to a Solr API endpoint.")
        .default(Constants.DEFAULT_SOLR_URL)

    private val credentials by credentialsOption

    override suspend fun run() {
        val url = Url(solrUrl)

        Utils.createHttpClient(credentials).use { client ->
            val response = client.get(url)
            val responseData = response.body<JsonObject>()
            info(responseData.toPrettyString())
        }
    }
}
