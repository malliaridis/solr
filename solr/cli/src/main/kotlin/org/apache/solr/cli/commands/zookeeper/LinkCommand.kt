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
import org.apache.solr.cli.options.CommonOptions.verboseOption
import org.apache.solr.cli.options.ConnectionOptions
import org.apache.solr.cli.utils.ZkUtils
import org.apache.solr.cloud.ZkController

class LinkCommand : SuspendingCliktCommand(name = "link") {

    private val collection by argument()
        .help("Collection to link with the ConfigSet.")

    private val config by argument()
        .help("The name of the ConfigSet to link the collection to.")

    private val connection by ConnectionOptions()

    private val verbose by verboseOption

    override suspend fun run() {
        val zkHost = connection.getZkHost()
        if (verbose) echo("\nConnecting to ZooKeeper at $zkHost ...")

        ZkUtils.getZkClient(zkHost, connection.timeout).use { zkClient ->
            try {
                ZkController.linkConfSet(zkClient, collection, config)
            } catch (exception: Exception) {
                echo(message = "Could not complete link operation.", err = true)
                echo(exception.message, err = true)
            }
        }
    }
}
