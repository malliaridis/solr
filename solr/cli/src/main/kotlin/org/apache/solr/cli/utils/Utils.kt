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

package org.apache.solr.cli.utils

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import org.apache.solr.cli.Constants
import org.apache.solr.cli.domain.SolrProcess
import org.apache.solr.cli.domain.UrlScheme
import org.apache.solr.cli.exceptions.ProcessNotFoundException
import org.apache.solr.cli.services.ProcessAnalyzer

object Utils {

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json()
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
        }
    }

    /**
     * Searches for a locally running Solr instance and returns the PID if found.
     *
     * @param port The port the Solr instance is running on.
     * @return Process ID (PID) of the running Solr instance.
     */
    suspend fun findSolrPIDByPort(port: Int): Long? {
        val solrProcesses = ProcessAnalyzer.findProcesses(
            "java", "start.jar", "jetty.port=$port"
        ).getOrNull()

        if (solrProcesses.isNullOrEmpty()) throw ProcessNotFoundException()
        return solrProcesses.first() // there can be only one process running on a specified port
    }

    /**
     * Retrieves the Jetty port (jetty.port) from a running Solr instance via process arguments.
     *
     * @param pid The process ID of the Solr instance to get the port for.
     * @return The Jetty port (value of `-Djetty.port`) iff it could be determined by the arguments.
     * @see ProcessAnalyzer.getProcessArguments
     */
    suspend fun getJettyPort(pid: Long): Int? {
        val result = ProcessAnalyzer.getProcessArguments(pid)
        val arguments = result.getOrNull() ?: return null
        return getPortFromArguments(arguments)
    }

    internal suspend fun getSolrProcessByPid(pid: Long): SolrProcess? {
        val result = ProcessAnalyzer.getProcessArguments(pid)
        val arguments = result.getOrNull() ?: return null

        val port = getPortFromArguments(arguments) ?: Constants.DEFAULT_SOLR_PORT
        val scheme = getSchemeFromArguments(arguments)

        return SolrProcess(pid, port, scheme)
    }

    private fun getPortFromArguments(arguments: List<String>): Int? {
        // Get the jetty.port value from the arguments returned
        val portParam = arguments.firstOrNull { it.contains("jetty.port") } ?: return null

        val keyValue = portParam.split("=")

        if (keyValue.size != 2 || keyValue[1].isBlank()) return null
        return keyValue[1].toIntOrNull()
    }

    private fun getSchemeFromArguments(arguments: List<String>): UrlScheme {
        // Get the jetty.port value from the arguments returned
        val portParam = arguments.firstOrNull { it.contains("module=http") }
            ?: return UrlScheme.http

        val keyValue = portParam.split("=")

        if (keyValue.size != 2 || keyValue[1].isBlank()) return UrlScheme.http
        return UrlScheme.valueOf(keyValue[1])
    }

    fun getHttpClient(credentials: String?) = httpClient
}
