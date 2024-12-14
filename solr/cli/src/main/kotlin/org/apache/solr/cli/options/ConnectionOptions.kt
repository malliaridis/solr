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

package org.apache.solr.cli.options

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.groups.single
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import org.apache.solr.cli.Constants
import org.apache.solr.cli.utils.Utils
import org.apache.solr.cli.utils.ZkUtils.getZkHostFromSolrUrl

class ConnectionOptions : OptionGroup(name = "Connection Options") {

    private val zkHostOption = option(
        "-z", "--zk-host",
        metavar = "url",
        envvar = "ZK_HOST",
        valueSourceKey = "solr.zk.host",
    ).help("Zookeeper connection string.")

    val zkHost by zkHostOption

    private val solrUrlOption = option(
        "-s", "--solr-url",
        metavar = "url",
        // envvar = "SOLR_URL", // TODO See if these values are relevant
        // valueSourceKey = "solr.url",
    ).help("Base Solr URL, which can be used to determine the zk-host if that's not known.")

    val solrUrl by solrUrlOption

    val credentials by option("-u", "--credentials")
        .help {
            """Credentials in the format username:password.
            |Example: --credentials solr:SolrRocks
            """.trimMargin()
        }

    val timeout by option(
        "--timeout",
        envvar = "SOLR_ZK_CLIENT_TIMEOUT",
        valueSourceKey = "solr.zk.client.timeout",
    ).help("Timeout in milliseconds to use for Zookeeper client connections.")
        .int()
        .default(Constants.DEFAULT_ZK_CLIENT_TIMEOUT)

    val connectionUrlOption = mutuallyExclusiveOptions(zkHostOption, solrUrlOption)
            .single()

    /**
     * Function that returns a Zookeeper host by taking into account both [zkHost] and [solrUrl].
     *
     * @return [zkHost] if defined, otherwise it fetches the [zkHost] configured for the Solr
     * instance at [solrUrl].
     */
    suspend fun getZkHost(): String {
        return zkHost ?: solrUrl?.let { url ->
            Utils.createHttpClient(credentials).use { client ->
                getZkHostFromSolrUrl(client, url)
            }
        } ?: throw Error("Either --zk-host or --solr-url has to be provided.")
    }
}
