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
import com.github.ajalt.clikt.parameters.types.int

internal class StopOptions(private val port: () -> Int) : OptionGroup() {

    val stopKey by option(
        "-k", "--key",
        envvar = "SOLR_STOP_KEY",
        valueSourceKey = "solr.stop.key",
        hidden = true,
    ).help("Stop key; default is solrrocks.")
        .default("SolrRocks")

    val stopPort by option(
        envvar = "SOLR_STOP_PORT",
        valueSourceKey = "solr.stop.port",
        hidden = true,
    ).int()
        .defaultLazy { port() - 1000 }

}