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
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import com.github.ajalt.clikt.parameters.types.restrictTo
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createFile
import kotlin.system.exitProcess
import org.apache.solr.cli.ExitCode
import org.apache.solr.cli.data.MemoryAllocation
import org.apache.solr.cli.data.UserLimits
import org.apache.solr.cli.enums.UrlScheme
import org.apache.solr.cli.processes.CommandChecker
import org.apache.solr.cli.processes.PrivilegeChecker
import org.apache.solr.cli.processes.UserLimitsChecker.checkUserLimits

internal class StartCommand : SuspendingCliktCommand(name = "start") {

    private val javaHome by option(
        envvar = "JAVA_HOME",
        valueSourceKey = "java.home",
    ).path()

    private val solrJavaHome: Path? by option(
        envvar = "JAVA_HOME",
        valueSourceKey = "java.home",
    ).path(canBeFile = false, canBeDir = true)

    private val javaExec by option()
        .defaultLazy {
            val javaDir = solrJavaHome ?: javaHome ?: return@defaultLazy "java"
            javaDir.resolve("bin/java").absolutePathString()
        }

    private val jstackExec by option()
        .defaultLazy {
            val javaDir = solrJavaHome ?: javaHome ?: return@defaultLazy "jstack"
            javaDir.resolve("bin/jstack").absolutePathString()
        }

    private val urlScheme by option("--url-scheme").help("Solr URL scheme: http or https, defaults to http if not specified.")
        .enum<UrlScheme>()

    private val host by option(
        "--host",
        envvar = "SOLR_HOST",
        valueSourceKey = "solr.host",
    ).help("Specify the hostname for this Solr instance.")

    private val port by option(
        "-p", "--port",
        envvar = "SOLR_PORT",
        valueSourceKey = "solr.port",
    ).help("Specify the port to start the Solr HTTP listener on.")
        .int()
        .restrictTo(0, UShort.MAX_VALUE.toInt())
        .default(8983)

    private val zkHost by option(
        "-z", "--zk-host",
        envvar = "ZK_HOST",
        valueSourceKey = "solr.zk.host",
    ).help("Zookeeper connection string.")

    private val installDir by option(
        "--install-dir",
        hidden = true,
        envvar = "SOLR_INSTALL_DIR",
        valueSourceKey = "solr.install.dir",
    ).path(mustExist = true, canBeDir = true, canBeFile = false)
        .defaultLazy { Path("..") }

    private val serverDir by option(
        "--server-dir",
        envvar = "SOLR_SERVER_DIR",
        valueSourceKey = "solr.server.dir",
    ).help("Path to the Solr server directory.")
        .path(canBeFile = false, canBeDir = true, mustExist = true)
        .defaultLazy { installDir.resolve("server") }

    private val solrHome by option(
        "--solr-home",
        envvar = "SOLR_HOME",
        valueSourceKey = "solr.home",
    ).help {
        // TODO Review line breaks in console output
        """Solr will create core directories under this directory. This allows you to run multiple
           Solr instances on the same host while reusing the same server directory set using the
           --server-dir parameter. If set,the specified directory should contain a solr.xml file,
           unless solr.xml exists in Zookeeper. This parameter is ignored when running examples
           (-e), as the solr.home depends on which example is run. The default value is server/solr.
           If passed relative dir, validation with current dir will be done, before trying default
           server/<dir>.
        """.trimIndent()
    }.path(canBeFile = false, canBeDir = true, mustExist = true)
        .defaultLazy { serverDir.resolve("solr") }

    private val dataHome by option()
        .help(
            """Sets the directory where Solr will store index data in <instance_dir>/data
               subdirectories. If not set, Solr uses solr.solr.home for config and data.
             """.trimIndent(),
        ).path(canBeFile = false, canBeDir = true, mustExist = true)
        .defaultLazy { solrHome.resolve("data") }

    private val confDir by option(
        "--conf-dir",
        envvar = "SOLR_DEFAULT_CONFDIR",
        valueSourceKey = "solr.default.confdir",
    ).path(canBeFile = false, canBeDir = true)
        .defaultLazy {
            Path(
                serverDir.absolutePathString(),
                "solr",
                "configsets",
                "_default",
                "conf",
            )
        }

    private val logsDir by option(
        "--logs-dir",
        envvar = "SOLR_LOGS_DIR",
        valueSourceKey = "solr.logs.dir",
    ).path(canBeFile = false, canBeDir = true)
        .defaultLazy { serverDir.resolve( "logs") }

    private val userManagedMode by option("--user-managed")
        .help(
            """Start Solr in User Managed mode.
               See the Ref Guide for more details: https://solr.apache.org/guide/solr/latest/deployment-guide/cluster-types.html
            """.trimIndent()
        ).flag()

    private val memory by option(
        "-m",
        "--memory",
        envvar = "SOLR_HEAP",
        valueSourceKey = "solr.heap.value",
    ).help(
        """Sets the min (-Xms) and max (-Xmx) heap size for the JVM, such as: -m 4g results in:
           -Xms4g -Xmx4g; by default, this script sets the heap size to 512m.
        """.trimIndent()
    ).convert { MemoryAllocation(it) }

    private val jvmOptions by option("--jvm-opts")
        .help(
            """Additional parameters to pass to the JVM when starting Solr.
               Note that the values need to be quoted.
            """.trimIndent())
        .multiple()

    // TODO Consider deprecating this option.
    private val jettyParams by option(
        "-j",
        "--jetty-config"
    ).help("Additional parameters to pass to Jetty when starting Solr.")

    // TODO Consider making foreground mutually exclusive with example
    private val foreground by option("--foreground").help("Start Solr in foreground; default starts Solr in the background and sends stdout / stderr to solr-PORT-console.log")
        .flag()

    // TODO Move option to ExampleCommand
    // TODO Consider replacing with --prompt and use --no-prompt behavior by default
    private val noPrompt by option("--no-prompt").help("Don't prompt for input; accept all defaults when running examples that accept user input.")
        .flag()

    private val force by option("-f", "--force").help("Force option in case Solr is run as root.")
        .flag()

    private val verbose by option("--verbose").help("Enable verbose command output.").flag()

    // TODO If this affects solr and not the CLI, consider removing it
    private val quiet by option(
        "-q",
        "--quiet"
    ).help("Sets default log level of Solr to WARN instead of INFO.")

    private val sslEnabled by option(
        "--ssl-enabled",
        envvar = "SOLR_SSL_ENABLED",
        valueSourceKey = "solr.ssl.enabled",
    ).flag()

    private val sslReloadEnabled by option(
        "--ssl-reload-enabled",
        envvar = "SOLR_SSL_RELOAD_ENABLED",
        valueSourceKey = "solr.ssl.reload.enabled",
    ).flag()

    private val enableUserLimitChecks by option(
        "--enable-ulimits",
        hidden = true,
        envvar = "SOLR_ULIMIT_CHECKS_ENABLED",
        valueSourceKey = "solr.ulimit.checks.enabled",
    ).help("Whether to enable user resource limit checks.")
        .flag("--disable-ulimits", default = true)

    private val requestLogEnabled by option(
        "--request-log-enabled",
        envvar = "SOLR_REQUESTLOG_ENABLED",
        valueSourceKey = "solr.requestlog.enabled",
    ).flag()

    private val gzipEnabled by option(
        "--gzip-enabled",
        envvar = "SOLR_GZIP_ENABLED",
        valueSourceKey = "solr.gzip.enabled",
    ).flag()

    override suspend fun run() {
        echo("Start World!")
        if (enableUserLimitChecks) checkUserLimits(UserLimits()) // TODO pass user limit defaults in UserLimits

        // Check current user if not force
        if (!force) PrivilegeChecker.isRootUser()
            .onSuccess { isRoot ->
                if (isRoot) {
                    // Exit if root and not --force used
                    echo("""[WARN] Starting Solr as the root user is a security risk and not considered best practice.""")
                    echo("Exiting.")
                    exitProcess(1)
                }
            }

        // TODO Make sure port is not used
        start()
    }

    /**
     * Starts Solr based on the configuration provided.
     */
    private suspend fun start() {
        checkCommands()

        // TODO Set working directory to solrServerDir

        // TODO solrServerDir/start.jar must exist

        // TODO Make sure logsDir not reserved path (contexts|etc|lib|modules|resources|scripts|solr|solr-webapp)
        // TODO Create and make writeable logs dir
        val errorFile = logsDir.resolve("jvm_crash_%p.log").createFile()

        // TODO If heap dump dir set, make writeable
        val startArguments = arrayOf(
            javaExec,
            "-server",
            // TODO memory allocation -Xms and -Xmx
            // TODO GC options
            // TODO GC log options
            // TODO ACL options
            // TODO JMX options
            // TODO Cloud options if cloud mode
            // TODO Solr logs dir
            // TODO Jetty port
            // TODO stop word
            // TODO stop key
            // TODO Solr host args (-Dhost=SOLR_HOST)
            // TODO Timezone
            "-XX:-OmitStackTraceInFastThrow", // ensures stack traces in errors
            "-XX:+CrashOnOutOfMemoryError", // ensures that Solr crashes whenever OOME is thrown
            "-XX:ErrorFile=$errorFile",
            "-Djetty.home=${serverDir.absolutePathString()}",
            // TODO LOG4J_CONFIG options
            // TODO Script Solr options
            // TODO Security manager options
            // TODO Solr AdminUI options
            // TODO Solr options
        )

        val command = if (foreground) arrayOf(
            javaExec,
            *startArguments,
            *jvmOptions.toTypedArray(),
            "-jar",
            "start.jar",
            *composeJettyConfigs(),
            // TODO $SOLR_JETTY_ADDL_CONFIG
        ) else arrayOf(
            javaExec,
            *startArguments,
            *jvmOptions.toTypedArray(),
            "-Dsolr.log.muteconsole",
            "-jar",
            "start.jar",
            *composeJettyConfigs(),
            // TODO $SOLR_JETTY_ADDL_CONFIG
            // TODO 1>"$SOLR_LOGS_DIR/solr-$SOLR_PORT-console.log" 2>&1 & echo $! > "$SOLR_PID_DIR/solr-$SOLR_PORT.pid"
        )

        // TODO Execute command
        // TODO Linux: Check for low entropy
        // TODO Wait for Solr to come online
    }

    /**
     * Checks if mandatory commands like java can be found in the current environment and exits if
     * they are not found.
     *
     * @param warnOptionals Echos warn messages for optional commands if `true`.
     */
    private suspend fun checkCommands(warnOptionals: Boolean = true) {
        CommandChecker.commandExists(javaExec)
            .onFailure {
                echo(
                    message = """Could not find java executable "$javaExec".
                        Please make sure if JAVA_HOME or SOLR_JAVA_HOME is set, that they point
                        to the right directory. Alternatively, if these environment variables are
                        not set, make sure that the "java" command can be executed. 
                        """.trimIndent(),
                    err = true,
                )
                exitProcess(ExitCode.COMMAND_NOT_FOUND)
            }

        // TODO Check and warn about jstack
    }

    private fun composeJettyConfigs(): Array<String> {
        val arguments = mutableListOf<String>()
        if (sslEnabled) {
            arguments.add("--module=https")
            arguments.add("--lib=$serverDir/solr-webapp/webapp/WEB-INF/lib/*")
            if (sslReloadEnabled) {
                arguments.add("--module=ssl-reload")
            }
        } else arguments.add("--module=http")

        if (requestLogEnabled) arguments.add("--module=requestlog")
        if (gzipEnabled) arguments.add("--module=gzip")

        return arguments.toTypedArray()
    }
}
