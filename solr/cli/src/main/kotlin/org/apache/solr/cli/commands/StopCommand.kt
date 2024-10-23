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
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import org.apache.solr.cli.Environment

internal class StopCommand : SuspendingCliktCommand(name = "stop") {

    // TODO Consider stop command for remote server as well?

    private val port by option("-p", "--port")
        .help("Specify the port the Solr HTTP listener is bound to.")
        .int()
        .default(
            value = Environment.SOLR_PORT,
            defaultForHelp = "Uses environment variable SOLR_PORT if present and falls back to 8983."
        )

    private val key by option("-k", "--key")
        .help("Stop key; default is solrrocks.")

    private val all by option("--all")
        .help("Find and stop all running Solr servers on this host")

    private val force by option("-f", "--force")
        .help("Force option in case Solr is run as root.")
        .flag()

    private val verbose by option("--verbose")
        .help("Enable verbose command output.")
        .flag()

    override suspend fun run() {
        val solrInstances = findSolrInstances()
        stopSolrInstances(solrInstances)
        // TODO Find Solr instances running on port / Find all Solr instances
        // TODO stop each Solr instance
        // TODO For timed out stop attempts, gracefully stop processes

        // TODO remove pid files from stopped solr instances if present

        TODO("Not yet implemented")
    }

    override fun helpEpilog(context: Context): String {
        return "NOTE: To see if any Solr servers are running, do: solr status"
    }

    private suspend fun findSolrInstances(): List<String> {
        TODO("Not yet implemented")
    }

    private suspend fun stopSolrInstances(solrInstances: List<String>) {
        TODO("Not yet implemented")
    }
}
