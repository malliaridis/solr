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
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import org.apache.solr.cli.options.CommonOptions.verboseOption
import org.apache.solr.cli.options.ConnectionOptions
import org.apache.solr.cli.utils.ZkUtils

// TODO Consider unifying zk-command connection options
class MakeRootCommand : SuspendingCliktCommand(name = "mkroot") {

    override fun help(context: Context): String =
        """Creates a ZNode in Zookeeper with no data.
        |Can be used to make a path of arbitrary depth but primarily intended to create a "chroot".
        """.trimMargin()

    // TODO allow user to provide ZK URIs with all information,
    //  like zk://username:password@127.0.0.1:9983/path
    private val path by argument()
        .help("ZNode / path to create.")

    private val ignore by option("--ignore")
        .help("Ignores errors if ZNode / path already exists.")
        .flag()

    private val connection by ConnectionOptions()

    private val verbose by verboseOption

    override suspend fun run() {
        val zkHost = connection.getZkHost()
        if (verbose) echo("Connecting to ZooKeeper at $zkHost ...")

        ZkUtils.getZkClient(zkHost, connection.timeout).use { zkClient ->
            echo("Creating ZooKeeper path $path on ZooKeeper at $zkHost")
            try {
                zkClient.makePath(path, !ignore, true)
            } catch (exception: Exception) {
                echo(message = "Could not complete mkroot operation.", err = true)
                echo(exception.message, err = true)
            }
        }
    }
}
