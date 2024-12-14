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
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.URLBuilder
import io.ktor.http.path
import java.nio.file.Path
import java.util.Properties
import java.util.concurrent.TimeUnit
import org.apache.solr.cli.data.SystemData
import org.apache.solr.common.cloud.SolrZkClient
import org.apache.solr.common.util.Compressor
import org.apache.solr.common.util.ZLibCompressor
import org.apache.solr.core.NodeConfig
import org.apache.solr.core.SolrXmlConfig

internal object ZkUtils {

    @Deprecated(
        "This function depends on multiple Solr modules and introduces dependencies " +
                "that may be avoided. Auth is also not supported."
    )
    fun getZkClient(zkHost: String, solrHome: Path, timeout: Int, compression: Int): SolrZkClient {
        return SolrZkClient.Builder()
            .withUrl(zkHost)
            .withTimeout(timeout, TimeUnit.MILLISECONDS)
            .withStateFileCompression(compression, getCompressor(zkHost, solrHome))
            .build()
    }

    @Deprecated(
        "This function depends on multiple Solr modules and introduces dependencies " +
                "that may be avoided. Auth is also not supported."
    )
    fun getZkClient(zkHost: String, timeout: Int): SolrZkClient {
        return SolrZkClient.Builder()
            .withUrl(zkHost)
            .withTimeout(timeout, TimeUnit.MILLISECONDS)
            .build()
    }

    // TODO Find alternative approach
    @Deprecated(
        "This implementation tries to get the compressor information from a local solr " +
                "home directory and therefore requires a local installation in order to not " +
                "fallback to defaults. This behavior is bad and does not take into account the " +
                "actual nodes the command is talking to."
    )
    private fun getCompressor(zkHost: String, solrHome: Path): Compressor {
        val properties = Properties()
        properties[SolrXmlConfig.ZK_HOST] = zkHost
        val nodeConfig = NodeConfig.loadNodeConfig(solrHome, properties)
        val stateCompressorClass: String? = nodeConfig.cloudConfig.stateCompressorClass

        return if (!stateCompressorClass.isNullOrBlank()) {
            Class.forName(stateCompressorClass)
                .asSubclass(Compressor::class.java)
                .getDeclaredConstructor().newInstance()
        } else ZLibCompressor()
    }

    @Deprecated(
        "Zookeeper host url and Solr URL should not be distinguished and therefore" +
                "this function should not be used."
    )
    suspend fun getZkHostFromSolrUrl(client: HttpClient, solrUrl: String): String {
        val infoUrl = URLBuilder(solrUrl).apply {
            path("admin", "info", "system")
        }.build()

        val systemInfo = client.get(infoUrl).body<SystemData>()
        return systemInfo.zkHost
    }

    suspend fun getZkHost(zkHost: String?, solrUrl: String?, credentials: String? = null): String {
        return zkHost ?: solrUrl?.let { url ->
            Utils.createHttpClient(credentials).use { client ->
                getZkHostFromSolrUrl(client, url)
            }
        } ?: throw Error("Either --zk-host or --solr-url has to be provided.")
    }
}
