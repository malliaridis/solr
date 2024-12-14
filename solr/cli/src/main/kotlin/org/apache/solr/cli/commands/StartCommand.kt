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
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import com.github.ajalt.clikt.parameters.types.boolean
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import com.github.ajalt.clikt.parameters.types.path
import com.github.ajalt.clikt.parameters.types.restrictTo
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import org.apache.solr.cli.StatusCode
import org.apache.solr.cli.domain.MemoryAllocation
import org.apache.solr.cli.domain.PlacementPluginMode
import org.apache.solr.cli.domain.SolrMode
import org.apache.solr.cli.domain.UserLimits
import org.apache.solr.cli.options.AuthOptions
import org.apache.solr.cli.options.JavaOptions
import org.apache.solr.cli.options.SecurityManagerOptions
import org.apache.solr.cli.options.SecurityOptions
import org.apache.solr.cli.options.SolrContextOptions
import org.apache.solr.cli.options.StartConnectionOptions
import org.apache.solr.cli.services.CommandChecker
import org.apache.solr.cli.services.CommandExecutor
import org.apache.solr.cli.services.PrivilegeChecker
import org.apache.solr.cli.services.UserLimitsChecker.checkUserLimits
import org.apache.solr.cli.utils.ReservedPaths

internal class StartCommand : SuspendingCliktCommand(name = "start") {

    override val invokeWithoutSubcommand = true

    private val javaOptions by JavaOptions()

    private val connectionOptions by StartConnectionOptions()

    private val solrContextOptions by SolrContextOptions()

    private val solrMode by option()
        .switch(
            "--cloud" to SolrMode.Cloud,
            "--user-managed" to SolrMode.UserManaged,
        ).help {
            """Start Solr in User Managed mode.
            | See the Ref Guide for more details:
            | https://solr.apache.org/guide/solr/latest/deployment-guide/cluster-types.html
            """.trimMargin()
        }.default(SolrMode.Cloud)

    private val memory by option(
        "-m", "--memory",
        envvar = "SOLR_HEAP",
        valueSourceKey = "solr.heap.value",
    ).help {
        """Sets the min (-Xms) and max (-Xmx) heap size for the JVM, such as: -m 4g results in:
        | -Xms4g -Xmx4g; by default, this script sets the heap size to 512m.
        """.trimMargin()
    }.convert { MemoryAllocation(initial = it) }

    private val jvmMemoryOptions by option(
        envvar = "SOLR_JAVA_MEM",
        valueSourceKey = "solr.java.memory.options.value",
        hidden = true,
    ).multiple()

    private val jvmOptions by option("--jvm-opts")
        .help {
            """Additional parameters to pass to the JVM when starting Solr.
            | Note that the values need to be quoted.
            """.trimMargin()
        }.multiple()

    // TODO Consider deprecating this option.
    private val jettyParams by option(
        "-j", "--jetty-config"
    ).help("Additional parameters to pass to Jetty when starting Solr.")

    // TODO Consider making foreground mutually exclusive with example
    private val foreground by option("--foreground")
        .help {
            """Start Solr in foreground; default starts Solr in the background and sends
            | stdout / stderr to solr-[PORT]-console.log
            """.trimMargin()
        }
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
        .flag()

    private val securityOptions by SecurityOptions(port = { connectionOptions.port })

    private val isUserLimitChecksEnabled by option(
        "--enable-ulimits",
        envvar = "SOLR_ULIMIT_CHECKS_ENABLED",
        valueSourceKey = "solr.ulimit.checks.enabled",
        hidden = true,
    ).help("Whether to enable user resource limit checks.")
        .flag("--disable-ulimits", default = true)

    private val isRequestLogEnabled by option(
        "--request-log-enabled",
        envvar = "SOLR_REQUESTLOG_ENABLED",
        valueSourceKey = "solr.requestlog.enabled",
        hidden = true,
    ).flag()

    private val isGzipEnabled by option(
        "--gzip-enabled",
        envvar = "SOLR_GZIP_ENABLED",
        valueSourceKey = "solr.gzip.enabled",
        hidden = true,
    ).flag()

    private val timeZone by option(
        envvar = "SOLR_TIMEZONE",
        valueSourceKey = "solr.timezone",
        hidden = true,
    ).default("UTC") // TODO Consider using a reference instead of string

    private val stopKey by option(
        "-k", "--key",
        envvar = "SOLR_STOP_KEY",
        valueSourceKey = "solr.stop.key",
        hidden = true,
    ).default("SolrRocks")

    private val stopPort by option(
        envvar = "SOLR_STOP_PORT",
        valueSourceKey = "solr.stop.port",
        hidden = true,
    ).int()
        .defaultLazy { connectionOptions.port - 1000 }

    private val zkClientTimeout by option(
        envvar = "ZK_CLIENT_TIMEOUT",
        valueSourceKey = "solr.zk.client.timeout",
        hidden = true,
    ).long()
        .default(30000)

    // TODO See if this is identical with zk client timeout
    private val zkWait by option(
        envvar = "SOLR_WAIT_FOR_ZK",
        valueSourceKey = "solr.zk.wait",
        hidden = true,
    ).help("Time in seconds Solr will try to connect to Zookeeper before throwing a timeout.")
        .long()
        .default(30) // TODO Default value not set by CLI

    private val zkCreateChRoot by option(
        envvar = "SOLR_ZK_CREATE_CHROOT",
        valueSourceKey = "solr.zk.embedded.chroot.create",
        hidden = true,
    ).flag()

    private val isSolrXmlRequired by option(
        envvar = "SOLR_SOLRXML_REQUIRED",
        valueSourceKey = "solr.solrxml.required",
        hidden = true,
    ).boolean()
        .defaultLazy { solrMode.isUserManaged }

    private val isRemoteJmxEnabled by option(
        envvar = "SOLR_JMX_ENABLED",
        valueSourceKey = "solr.jmx.enabled",
        hidden = true,
    ).flag()

    private val rmiPort by option(
        envvar = "SOLR_JMX_RMI_PORT",
        valueSourceKey = "solr.jmx.rmi.port",
        hidden = true,
    ).int()
        .restrictTo(0, UShort.MAX_VALUE.toInt())
        .defaultLazy {
            val rmiPort = connectionOptions.port + 10_000
            if (rmiPort > UShort.MAX_VALUE.toInt()) {
                echo(
                    message = "RMI port is $rmiPort, which is invalid.",
                    err = true,
                )
                currentContext.exitProcess(StatusCode.GENERAL_ERROR)
            }
            rmiPort
        }

    private val aclAllowList by option(
        envvar = "SOLR_ACL_ALLOW_LIST",
        valueSourceKey = "solr.security.acl.allow",
        hidden = true,
    ).multiple()

    private val aclDenyList by option(
        envvar = "SOLR_ACL_DENY_LIST",
        valueSourceKey = "solr.security.acl.deny",
        hidden = true,
    ).multiple()

    private val gcOptions by option(
        envvar = "SOLR_GC_OPTS",
        valueSourceKey = "solr.java.gc.options",
        hidden = true,
    ).multiple(
        listOf(
            "-XX:+PerfDisableSharedMem",
            "-XX:+ParallelRefProcEnabled",
            "-XX:MaxGCPauseMillis=250",
            "-XX:+UseLargePages",
            "-XX:+AlwaysPreTouch",
            "-XX:+ExplicitGCInvokesConcurrent",
        )
    )

    private val gcLogOptions by option(
        envvar = "SOLR_GC_LOG_OPTS",
        valueSourceKey = "solr.java.gc.log.options.value",
        hidden = true,
    ).defaultLazy {
        """-Xlog:gc*:file=${solrContextOptions.logsDirectory.absolutePathString()}\solr_gc.log:time,level,tags:filecount=9,filesize=20M"""
    } // TODO May require multiple() instead of string

    // TODO Consider merging plugin properties into a data class
    private val isPlacementPluginEnabled by option(
        envvar = "SOLR_PLUGIN_PLACEMENT_ENABLED",
        valueSourceKey = "solr.plugins.placement.enabled",
        hidden = true,
    ).flag()

    private val placementPluginMode by option(
        envvar = "SOLR_PLUGIN_PLACEMENT_ENABLED",
        valueSourceKey = "solr.plugins.placement.mode",
        hidden = true,
    ).enum<PlacementPluginMode>()
        .default(PlacementPluginMode.Simple)

    private val remoteStreamingFeatureEnabled by option(
        envvar = "SOLR_FEATURE_REMOTESTREAMING_ENABLED",
        valueSourceKey = "solr.features.remoteStreaming.enabled",
        hidden = true,
    ).flag()

    private val isStreamBodyFeatureEnabled by option(
        envvar = "SOLR_FEATURE_STREAMBODY_ENABLED",
        valueSourceKey = "solr.features.streamBody.enabled",
        hidden = true,
    ).flag()

    private val advertisePort by option(
        envvar = "SOLR_PORT_ADVERTISE",
        valueSourceKey = "solr.port.advertise",
        hidden = true,
    ).int()
        .restrictTo(0, UShort.MAX_VALUE.toInt())

    private val embeddedZkHost by option(
        envvar = "SOLR_ZK_EMBEDDED_HOST",
        valueSourceKey = "solr.zk.embedded.host",
        hidden = true,
    )

    private val javaStackSize by option(
        envvar = "SOLR_JAVA_STACK_SIZE",
        valueSourceKey = "solr.java.stack.size",
        hidden = true,
    ).default("256k")

    private val securityManagerOptions by SecurityManagerOptions(serverDirectory = { solrContextOptions.serverDirectory })

    private val isHeapDumpEnabled by option(
        envvar = "SOLR_HEAP_DUMP_ENABLED",
        valueSourceKey = "solr.heap.dump.enabled",
        hidden = true,
    ).flag()

    private val heapDumpDir by option(
        envvar = "SOLR_HEAP_DUMP_DIR",
        valueSourceKey = "solr.heap.dump.dir",
        hidden = true,
    ).path(canBeDir = true, canBeFile = false)
        .defaultLazy { solrContextOptions.logsDirectory.resolve("dumps") }

    private val internalOptions by option(
        envvar = "SOLR_INTERNAL_OPTS",
        valueSourceKey = "solr.internal.options",
        hidden = true,
    ).multiple()

    private val authOptions by AuthOptions(::echo)

    private val deleteUnknownCores by option(
        envvar = "SOLR_DELETE_UNKNOWN_CORES",
        valueSourceKey = "solr.cores.deleteUnknown",
        hidden = true,
    ).flag()

    private val log4jProps by option(
        envvar = "LOG4J_PROPS",
        valueSourceKey = "solr.log4j.props",
        hidden = true,
    ).file(canBeFile = true, canBeDir = false, mustExist = true)

    private lateinit var errorFile: Path

    override suspend fun run() {
        echo("Start World!")
        if (isUserLimitChecksEnabled) checkUserLimits(UserLimits()) // TODO pass user limit defaults in UserLimits

        // Check current user if not force
        if (!force) PrivilegeChecker.isRootUser()
            .onSuccess { isRoot ->
                if (isRoot) {
                    // Exit if root and not --force used
                    echo("""[WARN] Starting Solr as the root user is a security risk and not considered best practice.""")
                    echo("Exiting.")
                    currentContext.exitProcess(1)
                }
            }

        // TODO Make sure port is not used
        checkCommands()
        checkFiles()

        // TODO Set working directory to solrServerDir
        // TODO solrServerDir/start.jar must exist

        prepareLogging()
        start()
    }

    /**
     * Checks if mandatory commands like java can be found in the current environment and exits if
     * they are not found.
     *
     * @param warnOptionals Echos warn messages for optional commands if `true`.
     */
    private suspend fun checkCommands(warnOptionals: Boolean = true) {
        CommandChecker.commandExists(javaOptions.javaExec)
            .onFailure {
                echo(
                    message = """Could not find java executable "${javaOptions.javaExec}".
                    | Please make sure if JAVA_HOME or SOLR_JAVA_HOME is set, that they point
                    | to the right directory. Alternatively, if these environment variables are
                    | not set, make sure that the "java" command can be executed. 
                    """.trimMargin(),
                    err = true,
                )
                currentContext.exitProcess(StatusCode.COMMAND_NOT_FOUND)
            }

        // TODO Check and warn about jstack
    }

    private suspend fun checkFiles() {
        val solrXmlExists = solrContextOptions.solrHome.resolve("solr.xml").toFile().exists()
        if (solrMode.isUserManaged && isSolrXmlRequired && !solrXmlExists) {
            echo(
                message =
                    """Solr home directory ${solrContextOptions.solrHome.absolutePathString()} must
                | contain a solr.xml file!
                """.trimMargin(),
                err = true,
            )
            currentContext.exitProcess(StatusCode.GENERAL_ERROR)
        }
    }

    private suspend fun prepareLogging(failOnError: Boolean = false) {
        val reservedPaths = ReservedPaths.asStringArray()
        require(
            ReservedPaths.asStringArray()
                .none { solrContextOptions.logsDirectory.endsWith(Path(it)) }) {
            "Logs directory is set to a reserved path. It cannot contain any of ${reservedPaths.joinToString()}."
        }
        solrContextOptions.logsDirectory.createDirectories()
        // TODO See how the %p parameter is populated and replace implementation accordingly
        errorFile = solrContextOptions.logsDirectory.resolve("jvm_crash_%p.log")

        // TODO If heap dump dir set, make writeable
    }

    /**
     * Starts Solr based on the configuration provided.
     */
    private suspend fun start() {
        val startArguments = arrayOf(
            "-server",
            *composeMemoryOptions(),
            *gcOptions.toTypedArray(),
            gcLogOptions,
            *composeAclsArguments(),
            *composeJmxArguments(),
            *composeSolrModeArguments(),
            "-Dsolr.log.dir=${solrContextOptions.logsDirectory.absolutePathString()}",
            "-Djetty.port=${connectionOptions.port}",
            "-DSTOP.PORT=$stopPort", // TODO Update STOP.PORT to solr.stop.port globally
            "-DSTOP.KEY=$stopKey", // TODO Update STOP.KEY to solr.stop.key globally
            *connectionOptions.composeHostArguments(),
            "-Duser.timezone=$timeZone",
            "-XX:-OmitStackTraceInFastThrow", // ensures stack traces in errors
            "-XX:+CrashOnOutOfMemoryError", // ensures that Solr crashes whenever OOME is thrown
            "-XX:ErrorFile=${errorFile.absolutePathString()}",
            "-Djetty.home=${solrContextOptions.serverDirectory.absolutePathString()}",
            *composeLog4JArguments(),
            *composePluginArguments(),
            *composeFeatureArguments(),
            *composeNetworkArguments(),
            "-Xss${javaStackSize}",
            "-DwaitForZk=$zkWait",
            "-Dsolr.data.home=${solrContextOptions.dataHome.absolutePathString()}", // TODO Is there a change data-home to be not set?
            "-Dsolr.deleteUnknownCores=$deleteUnknownCores",
            *securityOptions.composeSecurityArguments(),
            "--add-modules",
            "jdk.incubator.vector",
            *authOptions.composeAuthArguments(),
            *internalOptions.toTypedArray(),
            *composeHeapDumpArguments(),
            *securityManagerOptions.composeSecurityManagerOptions(),
            // TODO Solr AdminUI options
            // TODO Solr options
        )

        val command = if (foreground) arrayOf(
            javaOptions.javaExec,
            *startArguments,
            *jvmOptions.toTypedArray(),
            "-jar",
            "start.jar",
            *composeJettyArguments(),
            // TODO $SOLR_JETTY_ADDL_CONFIG
        ) else arrayOf(
            javaOptions.javaExec,
            *startArguments,
            *jvmOptions.toTypedArray(),
            "-Dsolr.log.muteconsole",
            "-jar",
            "start.jar",
            *composeJettyArguments(),
            // TODO $SOLR_JETTY_ADDL_CONFIG
            // TODO 1>"$SOLR_LOGS_DIR/solr-$SOLR_PORT-console.log" 2>&1 & echo $! > "$SOLR_PID_DIR/solr-$SOLR_PORT.pid"
        )

        if (foreground) CommandExecutor.executeInForeground(
            command = command,
            workingDir = solrContextOptions.serverDirectory,
        ) else {
            CommandExecutor.executeInBackground(
                command = command,
                workingDir = solrContextOptions.serverDirectory,
                logsDir = solrContextOptions.logsDirectory,
                pidDir = solrContextOptions.pidDirectory,
                identifier = connectionOptions.port.toString(),
            )

            // TODO Linux: Check for low entropy
            // TODO Wait for Solr to come online
            echo("Solr server started.")
        }
    }

    private fun composeMemoryOptions(): Array<String> {
        return memory?.let { arrayOf("-Xms${it.initial}", "-Xmx${it.max}") }
            ?: if (jvmMemoryOptions.isNotEmpty()) jvmMemoryOptions.toTypedArray()
            else with(MemoryAllocation()) { arrayOf("-Xms${initial}", "-Xmx${max}") }
    }

    private fun composeAclsArguments(): Array<String> {
        return arrayOf(
            "-Dsolr.jetty.inetaccess.includes=${aclAllowList.joinToString(",")}",
            "-Dsolr.jetty.inetaccess.excludes=${aclDenyList.joinToString(",")}"
        )
    }

    private fun composeJmxArguments(): Array<String> {
        if (!isRemoteJmxEnabled) return emptyArray()

        val arguments = mutableListOf<String>()

        arguments.add("-Dcom.sun.management.jmxremote")
        arguments.add("-Dcom.sun.management.jmxremote.local.only=false")
        arguments.add("-Dcom.sun.management.jmxremote.ssl=false")
        arguments.add("-Dcom.sun.management.jmxremote.authenticate=false")
        arguments.add("-Dcom.sun.management.jmxremote.port=$rmiPort")
        arguments.add("-Dcom.sun.management.jmxremote.rmi.port=$rmiPort")

        // TODO Check if this changes behavior if host is always set
        if (connectionOptions.host.isNotEmpty())
            arguments.add("-Djava.rmi.server.hostname=${connectionOptions.host}")

        return arguments.toTypedArray()
    }

    private fun composeSolrModeArguments(): Array<String> {
        if (solrMode.isUserManaged) return emptyArray()

        val arguments = mutableListOf<String>()
        arguments.add("-DzkClientTimeout=$zkClientTimeout")
        connectionOptions.zkHost?.let { arguments.add("-DzkHost=$it") } ?: run {
            if (connectionOptions.port > UShort.MAX_VALUE.toInt() - 1000) {
                echo(
                    message = """Zookeeper host is not set and Solr port is
                        | ${connectionOptions.port}, which would 
                        | result in an invalid embedded Zookeeper port!
                        """.trimMargin(),
                    err = true,
                )
                currentContext.exitProcess(StatusCode.GENERAL_ERROR)
            }

            if (verbose)
                echo("Configuring SolrCloud to launch an embedded Zookeeper using -DzkRun")
            arguments.add("-DzkRun")
        }

        if (zkCreateChRoot) arguments.add("-DcreateZkChroot=true")

        val bootstrapCollection =
            Path(solrContextOptions.solrHome.absolutePathString(), "collection1", "core.properties")
                .toFile()
                .exists()
        if (bootstrapCollection) {
            arguments.add("-Dbootstrap_confdir=./solr/collection1/conf")
            arguments.add("-Dcollection.configName=myconf")
            arguments.add("-DnumShards=1")
        }

        if (isSolrXmlRequired) arguments.add("-Dsolr.solrxml.required=true")

        return arguments.toTypedArray()
    }

    private fun composeJettyArguments(): Array<String> {
        val arguments = mutableListOf<String>()
        if (securityOptions.isSslEnabled) {
            arguments.add("--module=https")
            arguments.add("--lib=${solrContextOptions.serverDirectory}/solr-webapp/webapp/WEB-INF/lib/*")
            if (securityOptions.isSslReload) {
                arguments.add("--module=ssl-reload")
            }
        } else arguments.add("--module=http")

        if (isRequestLogEnabled) arguments.add("--module=requestlog")
        if (isGzipEnabled) arguments.add("--module=gzip")

        return arguments.toTypedArray()
    }

    private fun composeLog4JArguments(): Array<String> {
        val arguments = mutableListOf<String>()
        arguments.add("-Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager")
        log4jProps?.let { arguments.add("-Dlog4j.configurationFile=${it.absolutePath}") }

        return arguments.toTypedArray()
    }

    private fun composePluginArguments(): Array<String> {
        return if (isPlacementPluginEnabled)
            arrayOf("-Dsolr.placementplugin.default=${placementPluginMode.toString().lowercase()}")
        else emptyArray()
    }

    private fun composeFeatureArguments(): Array<String> {
        val arguments = mutableListOf<String>()
        if (remoteStreamingFeatureEnabled) arguments.add("-Dsolr.enableRemoteStreaming=true")
        if (isStreamBodyFeatureEnabled) arguments.add("-Dsolr.enableStreamBody=true")

        return arguments.toTypedArray()
    }

    private fun composeNetworkArguments(): Array<String> {
        val arguments = mutableListOf<String>()
        advertisePort?.let { arguments.add("-Dsolr.port.advertise=$it") }
        connectionOptions.jettyHost?.let { arguments.add("-Dsolr.jetty.host=$it") }
        embeddedZkHost?.let { arguments.add("-Dsolr.zk.embedded.host=$it") }

        return arguments.toTypedArray()
    }

    private fun composeHeapDumpArguments(): Array<String> {
        if (!isHeapDumpEnabled) return emptyArray()

        return arrayOf(
            "-XX:+HeapDumpOnOutOfMemoryError",
            "-XX:HeapDumpPath=${heapDumpDir.absolutePathString() + File.pathSeparator}solr-$(date +%s)-pid$$.hprof",
        )
    }
}
