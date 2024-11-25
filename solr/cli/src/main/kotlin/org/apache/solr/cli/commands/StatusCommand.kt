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
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.long
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.apache.solr.cli.domain.SolrState
import org.apache.solr.cli.options.CommonOptions.credentialsOption
import org.apache.solr.cli.options.CommonOptions.solrUrlOption
import org.apache.solr.cli.options.SolrContextOptions
import org.apache.solr.cli.services.ProcessAnalyzer
import org.apache.solr.cli.services.SolrStateAnalyzer

class StatusCommand : SuspendingCliktCommand(name = "status") {

    private val timeoutMs by option(
        "--timeout",
        metavar = "ms",
        hidden = true,
    ).help("Wait up to the specified number of milliseconds to see Solr running.").long()

    private val isCompact by option("--compact").help("Keeps output brief and concise.").flag()

    private val solrUrl by solrUrlOption

    private val credentials by credentialsOption

    private val solrContext by SolrContextOptions(
        help = "Context options relevant for getting Solr status from local processes."
    )

    override suspend fun run() {
        val url = solrUrl
        if (url != null) checkSingleUrl(url)
        else checkLocalProcesses()
    }

    /**
     * @return Value of 1 if a Solr instance is running on the specified [url], 0 if not.
     */
    private suspend fun checkSingleUrl(url: String): Int {
        timeoutMs?.let { timeout ->
            echo("Checking the state of Solr within $timeout milliseconds.")
            val state = SolrStateAnalyzer.getSolrState(url, credentials, timeout.milliseconds)
            when (state) {
                is SolrState.Unknown, SolrState.Offline -> echo(
                    message = "Solr at $url did not come online within given timeout.",
                    err = true,
                )

                is SolrState.Online -> echo(message = "Solr running and accessible at $url.")
                is SolrState.AuthRequired ->
                    echo(message = "Solr running but not accessible at $url.", err = true)
            }
            return if (state.isOnline) 1 else 0
        } ?: return if (SolrStateAnalyzer.getSolrState(url, credentials).isOnline) 1 else 0
    }

    private suspend fun checkLocalProcesses() = coroutineScope {
        val processes = ProcessAnalyzer.getProcessesByPidFiles(solrContext.pidDirectory)
        if (processes.isNotEmpty()) {
            val results = processes.map { process ->
                async { checkSingleUrl(process.localUrl) }
            }
            val running = results.awaitAll().sum()
            echo("$running/$processes Solr processes running.")
        } else if (!isCompact) echo(message = "No Solr nodes are running.")
    }
}
