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

package org.apache.solr.cli.commands.zookeeper

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import org.apache.solr.cli.EchoUtils.debug
import org.apache.solr.cli.EchoUtils.err
import org.apache.solr.cli.EchoUtils.info
import org.apache.solr.cli.options.CommonOptions.verboseOption
import org.apache.solr.cli.options.ConnectionOptions
import org.apache.solr.cli.utils.ZkUtils

class DownloadCommand : SuspendingCliktCommand(name = "download") {

    private val name by argument()
        .help("Name of the ConfigSet stored in ZooKeeper.")

    private val directory by option("-d", "--directory")
        .help("Local directory where to download the configuration. Defaults to current directory.")
        .path()
        .default(Path("."))

    private val connection by ConnectionOptions()

    private val verbose by verboseOption

    override fun help(context: Context): String = "Downloads a configuration stored in Zookeeper."

    override suspend fun run() {
        directory.createDirectories()

        val zkHost = connection.getZkHost()
        debug(verbose) { "Connecting to ZooKeeper at $zkHost ..." }

        ZkUtils.getZkClient(zkHost, connection.timeout).use { zkClient ->
            info(
                message = "Downloading ConfigSet $name from ZooKeeper at $zkHost " +
                        "to directory ${directory.toAbsolutePath()}",
            )
            try {
                zkClient.downConfig(name, directory)
            } catch (exception: Exception) {
                err(message = "Could not complete download operation.", error = exception)
            }
        }
    }
}
