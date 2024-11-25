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

package org.apache.solr.cli.commands

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import org.apache.solr.cli.Environment
import org.apache.solr.cli.domain.UserLimits
import org.apache.solr.cli.processes.UserLimitsChecker.checkUserLimits

class RestartCommand : SuspendingCliktCommand(name = "restart") {

    private val host by option("--host")
        .help("Specify the hostname for this Solr instance.")

    private val port by option("-p", "--port")
        .help("Specify the port to start the Solr HTTP listener on.")
        .int()
        .restrictTo(0..UShort.MAX_VALUE.toInt())
        .default(
            value = Environment.SOLR_PORT,
            defaultForHelp = "Uses environment variable SOLR_PORT if present and falls back to 8983."
        )

    private val zkHost by option("-z", "--zk-host")
        .help("Zookeeper connection string; unnecessary if ZK_HOST is defined in solr.in.sh")
        .default(Environment.ZK_HOST)

    private val solrHome by option("--solr-home")
        .help {
            // TODO Review line breaks in console output
            """Sets the solr.solr.home system property; Solr will create core directories under
            | this directory. This allows you to run multiple Solr instances on the same host
            | while reusing the same server directory set using the --server-dir parameter.
            | If set,the specified directory should contain a solr.xml file, unless solr.xml exists
            | in Zookeeper. This parameter is ignored when running examples (-e), as the
            | solr.solr.home depends on which example is run. The default value is server/solr.
            | If passed relative dir, validation with current dir will be done, before trying
            | default server/<dir>.
            """.trimMargin()
        }.file(canBeFile = false, canBeDir = true)
    // TODO Lookup default values for solrHome

    private val dataHome by option()
        .help {
            """Sets the solr.data.home system property, where Solr will store index data in
            | <instance_dir>/data subdirectories. If not set, Solr uses solr.solr.home for config
            | and data.
            """.trimMargin()
        }.file(canBeFile = false, canBeDir = true)

    private val serverDir by option("--server-dir")
        .help("Path to the Solr server directory.")
        .file(canBeFile = false, canBeDir = true)
        // TODO Consider suing ./server as default
        .required()

    private val userManagedMode by option("--user-managed")
        .help {
            """Start Solr in User Managed mode. See the Ref Guide for more details:
            | https://solr.apache.org/guide/solr/latest/deployment-guide/cluster-types.html
            """.trimMargin()
        }.flag()

    private val memory by option("-m", "--memory")
        .help {
            """Sets the min (-Xms) and max (-Xmx) heap size for the JVM, such as: -m 4g results in:
            | -Xms4g -Xmx4g; by default, this script sets the heap size to 512m.
            """.trimMargin()
        }
        // TODO See if there is a default memory size

    private val jvmOptions by option("--jvm-opts")
        .help("Additional parameters to pass to the JVM when starting Solr.")
        .multiple()

    // TODO Consider deprecating this option.
    private val jettyParams by option("-j", "--jetty-params")
        .help("Additional parameters to pass to Jetty when starting Solr.")

    private val foreground by option("--foreground")
        .help {
            """Start Solr in foreground; default starts Solr in the background and sends
            | stdout / stderr to solr-PORT-console.log
            """.trimMargin()
        }.flag()

    // TODO Move option to ExampleCommand
    // TODO Consider replacing with --prompt and use --no-prompt behavior by default
    private val noPrompt by option("--no-prompt")
        .help("Don't prompt for input; accept all defaults when running examples that accept user input.")
        .flag()

    private val force by option("-f", "--force")
        .help("Force option in case Solr is run as root.")
        .flag()

    private val verbose by option("--verbose")
        .help("Enable verbose command output.")
        .flag()

    // TODO If this affects solr and not the CLI, consider removing it
    private val quiet by option("-q", "--quiet")
        .help("Sets default log level of Solr to WARN instead of INFO.")

    override suspend fun run() {
        // TODO See if restart can be a combination of stop and start instead
        checkUserLimits(UserLimits()) // TODO pass user limit defaults in UserLimits
        TODO("Not yet implemented")
    }
}
