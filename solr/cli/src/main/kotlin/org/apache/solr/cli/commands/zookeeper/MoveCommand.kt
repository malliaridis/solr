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
import java.net.URI
import org.apache.solr.cli.EchoUtils.debug
import org.apache.solr.cli.EchoUtils.err
import org.apache.solr.cli.EchoUtils.info
import org.apache.solr.cli.domain.UriScheme
import org.apache.solr.cli.domain.UriScheme.Companion.toUriScheme
import org.apache.solr.cli.options.CommonOptions.verboseOption
import org.apache.solr.cli.options.ConnectionOptions
import org.apache.solr.cli.utils.ZkUtils

// TODO Consider unifying zk-command connection options
class MoveCommand : SuspendingCliktCommand(name = "mv") {

    override fun help(context: Context): String = "Moves (renames) ZNodes on a Zookeeper."

    private val source by argument()
        .help("The source file or directory.")

    private val destination by argument()
        .help("The destination file or directory.")

    private val connection by ConnectionOptions()

    private val verbose by verboseOption

    override suspend fun run() {
        val srcUri = URI(source)
        val srcScheme = srcUri.scheme?.toUriScheme() ?: UriScheme.Zk

        val destUri = URI(destination)
        val destScheme = destUri.scheme?.toUriScheme() ?: UriScheme.Zk

        // TODO Reconsider this limitation
        require(srcScheme == UriScheme.Zk || destScheme == UriScheme.Zk) {
            """mv command operates on znodes and "file://" URI is not allowed."""
        }

        val zkHost = connection.getZkHost()
        debug(verbose) { "\nConnecting to ZooKeeper at $zkHost ..." }

        ZkUtils.getZkClient(zkHost, connection.timeout).use { zkClient ->
            info("Moving ZNode $source to $destination on ZooKeeper at $zkHost")
            try {
                zkClient.moveZnode(source, destination)
            } catch (exception: Exception) {
                err(message = "Could not complete mv operation.", error = exception)
            }
        }
    }
}
