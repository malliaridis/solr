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

import com.github.ajalt.clikt.core.ParameterHolder
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.groups.single
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import org.apache.solr.cli.Constants

object CommonOptions {

    val ParameterHolder.verboseOption
        get() = option("-v", "--verbose")
            .help("Enable verbose command output.")
            .flag()

    val ParameterHolder.zkHostOption
        get() = option(
            "-z", "--zk-host",
            metavar = "url",
            envvar = "ZK_HOST",
            valueSourceKey = "solr.zk.host",
        ).help("Zookeeper connection string.")

    val ParameterHolder.solrUrlOption
        get() = option(
            "-s", "--solr-url",
            metavar = "url",
            // envvar = "SOLR_URL", // TODO See if these values are relevant
            // valueSourceKey = "solr.url",
        ).help("Base Solr URL, which can be used to determine the zk-host if that's not known.")

    val ParameterHolder.recursiveOption
        get() = option("-r", "--recursive")
            .help("Apply the command recursively.")
            .flag()

    val ParameterHolder.credentialsOption
        get() = option("-u", "--credentials")
            .help {
                """Credentials in the format username:password.
                |Example: --credentials solr:SolrRocks
                """.trimMargin()
            }

    val ParameterHolder.connectionOptions
        get() = mutuallyExclusiveOptions(zkHostOption, solrUrlOption)
            .single()

    val ParameterHolder.collectionNameOption
        get() = option("-c", "--name")
            .help("Name of the collection.")

}