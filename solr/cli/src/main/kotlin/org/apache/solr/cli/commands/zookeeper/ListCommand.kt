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
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import org.apache.solr.cli.EchoUtils.debug
import org.apache.solr.cli.EchoUtils.err
import org.apache.solr.cli.EchoUtils.info
import org.apache.solr.cli.options.CommonOptions.recursiveOption
import org.apache.solr.cli.options.CommonOptions.verboseOption
import org.apache.solr.cli.options.ConnectionOptions
import org.apache.solr.cli.utils.ZkUtils

class ListCommand : SuspendingCliktCommand(name = "ls") {

    // TODO allow user to provide ZK URIs with all information,
    //  like zk://username:password@127.0.0.1:9983/path
    private val path by argument()
        .help("The ZNode / path to list.")

    private val recursive by recursiveOption

    private val connection by ConnectionOptions()

    private val verbose by verboseOption

    override suspend fun run() {
        val zkHost = connection.getZkHost()
        debug(verbose) { "Connecting to ZooKeeper at $zkHost ..." }

        ZkUtils.getZkClient(zkHost, connection.timeout).use { zkClient ->
            info("Getting listing for ZooKeeper node $path from ZooKeeper at $zkHost.")
            try {
                zkClient.listZnode(path, recursive)
            } catch (exception: Exception) {
                err(message = "Could not complete ls operation.", error = exception)
            }
        }
    }
}
