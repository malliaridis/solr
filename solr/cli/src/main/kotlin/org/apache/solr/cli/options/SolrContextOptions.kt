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
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

internal class SolrContextOptions : OptionGroup() {

    val installDirectory by option(
        "--install-dir",
        envvar = "SOLR_INSTALL_DIR",
        valueSourceKey = "solr.install.dir",
        hidden = true,
    ).path(mustExist = true, canBeDir = true, canBeFile = false)
        .defaultLazy { Path("..") }

    val serverDirectory by option(
        "--server-dir",
        envvar = "SOLR_SERVER_DIR",
        valueSourceKey = "solr.server.dir",
    ).help("Path to the Solr server directory.")
        .path(canBeFile = false, canBeDir = true, mustExist = true)
        .defaultLazy { installDirectory.resolve("server") }

    val solrHome by option(
        "--solr-home",
        envvar = "SOLR_HOME",
        valueSourceKey = "solr.home",
    ).help {
        // TODO Review line breaks in console output
        """Solr will create core directories under this directory. This allows you to run multiple
        | Solr instances on the same host while reusing the same server directory set using the
        | --server-dir parameter. If set,the specified directory should contain a solr.xml file,
        | unless solr.xml exists in Zookeeper. This parameter is ignored when running examples
        | (-e), as the solr.home depends on which example is run. The default value is server/solr.
        | If passed relative dir, validation with current dir will be done, before trying default
        | server/<dir>.
        """.trimMargin()
    }.path(canBeFile = false, canBeDir = true, mustExist = true)
        .defaultLazy { serverDirectory.resolve("solr") }

    val dataHome by option()
        .help {
            """Sets the directory where Solr will store index data in <instance_dir>/data
            | subdirectories. If not set, Solr uses solr.solr.home for config and data.
            """.trimMargin()
        }.path(canBeFile = false, canBeDir = true, mustExist = true)
        .defaultLazy { solrHome.resolve("data") }

    val configDirectory by option(
        "--conf-dir",
        envvar = "SOLR_DEFAULT_CONFDIR",
        valueSourceKey = "solr.default.confdir",
    ).path(canBeFile = false, canBeDir = true)
        .defaultLazy {
            Path(
                serverDirectory.absolutePathString(),
                "solr",
                "configsets",
                "_default",
                "conf",
            )
        }

    val logsDirectory by option(
        "--logs-dir",
        envvar = "SOLR_LOGS_DIR",
        valueSourceKey = "solr.logs.dir",
    ).path(canBeFile = false, canBeDir = true)
        .defaultLazy { serverDirectory.resolve("logs") }

    val pidDirectory by option(
        envvar = "SOLR_PID_DIR",
        valueSourceKey = "solr.pid.dir",
        hidden = true,
    ).path(canBeFile = false, canBeDir = true)
        .defaultLazy { installDirectory.resolve("bin") }
}
