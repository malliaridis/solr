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
import com.github.ajalt.clikt.parameters.types.path
import org.apache.solr.cli.options.CommonOptions.verboseOption
import org.apache.solr.cli.options.ConnectionOptions
import org.apache.solr.cli.utils.ZkUtils

class UpdateAclsCommand : SuspendingCliktCommand(name = "updateacls") {

    private val path by argument()
        .help("Path to the updated ACLs.")
        .path(mustExist = true)

    private val connection by ConnectionOptions()

    private val verbose by verboseOption

    override suspend fun run() {
        val zkHost = connection.getZkHost()
        if (verbose) echo("\nConnecting to ZooKeeper at $zkHost ...")

        // TODO Create chroot if it does not exist

        ZkUtils.getZkClient(zkHost, connection.timeout).use { zkClient ->
            echo("Updating ACLs from ${path.toAbsolutePath()} to ZooKeeper at $zkHost.")
            try {
                zkClient.updateACLs(path.toAbsolutePath().toString())
            } catch (exception: Exception) {
                echo(message = "Could not complete updateacls operation.", err = true)
                echo(exception.message, err = true)
            }
        }
    }
}
