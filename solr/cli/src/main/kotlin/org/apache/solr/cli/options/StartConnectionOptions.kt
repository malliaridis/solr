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
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import org.apache.solr.cli.Constants
import org.apache.solr.cli.domain.UrlScheme

internal class StartConnectionOptions : OptionGroup(
    name = "Connection options",
    help = "Options that are used for establishing a Solr connection or configuring a Solr instance.",
) {

    @Deprecated("Use security options and set scheme via SSL flag.")
    val urlScheme by option("--url-scheme")
        .help("Solr URL scheme: http or https, defaults to http if not specified.")
        .enum<UrlScheme>()

    val jettyHost by option(
        envvar = "SOLR_JETTY_HOST",
        valueSourceKey = "solr.jetty.host",
        hidden = true,
    )

    val host by option(
        "--host",
        envvar = "SOLR_HOST",
        valueSourceKey = "solr.host",
    ).help("Specify the hostname for this Solr instance.")
        .defaultLazy { jettyHost ?: "127.0.0.1" }

    val port by option(
        "-p", "--port",
        envvar = "SOLR_PORT",
        valueSourceKey = "solr.port.bind",
    ).help("Specify the port to start the Solr HTTP listener on.")
        .int()
        .restrictTo(0, UShort.MAX_VALUE.toInt())
        .default(Constants.DEFAULT_SOLR_PORT)

    val zkHost by option(
        "-z", "--zk-host",
        envvar = "ZK_HOST",
        valueSourceKey = "solr.zk.host",
    ).help("Zookeeper connection string.")

    fun composeHostArguments(): Array<String> {
        return if (jettyHost != "127.0.0.1") emptyArray()
        else arrayOf("-Dhost=${host}")
    }
}
