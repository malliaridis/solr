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
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import java.net.URI
import org.apache.solr.cli.Constants
import org.apache.solr.cli.EchoUtils.err
import org.apache.solr.cli.domain.UriScheme
import org.apache.solr.cli.domain.UriScheme.Companion.toUriScheme
import org.apache.solr.cli.options.CommonOptions.recursiveOption
import org.apache.solr.cli.options.ConnectionOptions
import org.apache.solr.cli.utils.ZkUtils

// TODO Consider unifying zk-command connection options
class CopyCommand : SuspendingCliktCommand(name = "cp") {

    override fun help(context: Context): String =
        "Copies files or folders from and/or to Zookeeper."

    override fun helpEpilog(context: Context): String =
        """When <source> is a zk resource, <destination> may be '.'.
        |If <destination> ends with '/', then <dest> will be a local folder or parent znode and the
        |last element of the <destination> path will be appended unless <source> also ends in a slash. 
        |<destination> may be zk:, which may be useful when using the cp -r form to backup/restore 
        |the entire zk state.
        |You must enclose local paths that end in a wildcard in quotes or just
        |end the local path in a slash. That is,
        |'bin/solr zk cp -r /some/dir/ zk:/ -z localhost:2181' is equivalent to
        |'bin/solr zk cp -r "/some/dir/*" zk:/ -z localhost:2181'
        |but 'bin/solr zk cp -r /some/dir/* zk:/ -z localhost:2181' will throw an error
        |
        |to copy to local: 'bin/solr zk cp -r zk:/ /some/dir -z localhost:2181'
        |to restore to ZK: 'bin/solr zk cp -r /some/dir/ zk:/ -z localhost:2181'
        |
        |The 'file:' prefix is stripped, thus 'file:/wherever' specifies an absolute local path and
        |'file:somewhere' specifies a relative local path. All paths on Zookeeper are absolute.
        |
        |Zookeeper nodes CAN have data, so moving a single file to a parent znode will overlay the
        |data on the parent Znode so specifying the trailing slash can be important.
        |
        |Wildcards are supported when copying from local, trailing only and must be quoted.
        """.trimMargin()

    private val source by argument()
        .help("The source file or directory.")

    private val destination by argument()
        .help("The destination file or directory.")

    private val solrHome by option(
        "--solr-home",
        envvar = "SOLR_HOME",
        valueSourceKey = "solr.home",
    ).help("Required to look up configuration for compressing state.json.")
        .path()
        .required()

    private val connection by ConnectionOptions()

    private val recursive by recursiveOption

    private val compression by option("--compression", metavar = "bytes")
        .help {
            """Enable compression of the state.json over the wire and stored in Zookeeper. 
            |The value provided is the minimum length of bytes to compress state.json, i.e. any 
            |state.json above that size in bytes will be compressed. The default is -1, 
            |meaning state.json is always uncompressed.
            """.trimMargin()
        }.int()
        .default(Constants.DEFAULT_ZK_MIN_STATE_COMPRESSION)

    override suspend fun run() {
        val srcUri = URI(source)
        val srcScheme = srcUri.scheme?.toUriScheme() ?: UriScheme.File
        val srcPathString = srcUri.path ?: "/"

        val destUri = URI(destination)
        val destScheme = destUri.scheme?.toUriScheme() ?: UriScheme.File
        val destPathString = destUri.path ?: "/"

        // TODO Reconsider this limitation
        require(srcScheme == UriScheme.Zk || destScheme == UriScheme.Zk) {
            """Either source or destination has to be a valid Zookeeper URI (starting with "zk://")."""
        }

        val zkHost = connection.getZkHost()

        ZkUtils.getZkClient(zkHost, solrHome, connection.timeout, compression).use { client ->
            try {
                client.zkTransfer(
                    srcPathString,
                    srcScheme.isRemote,
                    destPathString,
                    destScheme.isRemote,
                    recursive,
                )
            } catch (exception: Exception) {
                err(message = "Could not complete cp operation.", error = exception)
            }
        }
    }
}
