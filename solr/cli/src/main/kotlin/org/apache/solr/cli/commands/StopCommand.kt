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
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import org.apache.solr.cli.Constants
import org.apache.solr.cli.EchoUtils.debug
import org.apache.solr.cli.EchoUtils.err
import org.apache.solr.cli.EchoUtils.info
import org.apache.solr.cli.EchoUtils.warn
import org.apache.solr.cli.options.AuthOptions
import org.apache.solr.cli.options.JavaOptions
import org.apache.solr.cli.options.SecurityOptions
import org.apache.solr.cli.options.SolrContextOptions
import org.apache.solr.cli.options.StopOptions
import org.apache.solr.cli.services.CommandChecker
import org.apache.solr.cli.services.CommandExecutor
import org.apache.solr.cli.services.ProcessAnalyzer
import org.apache.solr.cli.services.ProcessKiller
import org.apache.solr.cli.utils.Utils

// TODO Consider stop command for remote server as well?
internal class StopCommand : SuspendingCliktCommand(name = "stop") {

    init {
        configureContext {
            this.echoMessage
        }
    }

    private val javaOptions by JavaOptions()

    private val contextOptions by SolrContextOptions()

    /**
     * [port] in stop command is used to calculate the [StopOptions.stopPort].
     *
     * TODO Check if [port]  and [StopOptions.stopPort] should be mutually exclusive here.
     */
    private val port by option(
        "-p", "--port",
        envvar = "SOLR_PORT",
        valueSourceKey = "solr.port",
    ).help("Specify the port the Solr HTTP listener is bound to.")
        .int()
        .restrictTo(0, UShort.MAX_VALUE.toInt())
        .default(Constants.DEFAULT_SOLR_PORT)

    private val securityOptions by SecurityOptions(port = { port })

    // TODO See if these auth options are required for the stop commmand
    private val authOptions by AuthOptions(this)

    private val stopOptions by StopOptions(port = { port })

    private val toolOptions by option(
        envvar = "SOLR_TOOL_OPTS",
        valueSourceKey = "solr.tool.options",
        hidden = true,
    ).multiple()

    private val all by option("--all")
        .help("Find and stop all running Solr servers on this host")
        .flag()

    private val force by option("-f", "--force")
        .help("Force option in case Solr is run as root.")
        .flag()

    private val verbose by option("--verbose")
        .help("Enable verbose command output.")
        .flag()

    override suspend fun run() {
        if (all) {
            val solrProcesses = ProcessAnalyzer.findProcesses("java", "start.jar")
                .getOrNull()
            if (solrProcesses.isNullOrEmpty()) {
                info("No Solr processes found.")
                currentContext.exitProcess(0)
                return
            }
            solrProcesses.forEach { pid ->
                val jettyPort = Utils.getJettyPort(pid)
                debug(verbose) { "Solr process found with port $jettyPort" }

                stopSolrInstance(pid)
            }
        } else {
            val pid = Utils.findSolrPIDByPort(port) ?: run {
                info("No Solr process for port $port found.")
                currentContext.exitProcess(0)
                return
            }

            debug(verbose) { "Solr process for port $port found, PID: $pid" }
            stopSolrInstance(pid)
        }
    }

    override fun helpEpilog(context: Context): String =
        "NOTE: To see if any Solr servers are running, do: solr status"

    private suspend fun stopSolrInstance(pid: Long) {
        debug(verbose) {
            """Sending stop command to Solr running with stop port ${stopOptions.stopPort}
            | ... waiting up to ${stopOptions.waitTimeMs} seconds to allow Jetty process $pid to
            | stop gracefully.
            |""".trimMargin()
        }

        // Try to stop solr gracefully
        val stopArguments = arrayOf(
            javaOptions.javaExec,
            *securityOptions.composeSecurityArguments(),
            *authOptions.composeAuthArguments(),
            *toolOptions.toTypedArray(),
            "-jar",
            "start.jar",
            "STOP.PORT=${stopOptions.stopPort}",
            "STOP.KEY=${stopOptions.stopKey}",
            "--stop"
        )

        val result = CommandExecutor.executeInForeground(
            command = stopArguments,
            workingDir = contextOptions.serverDirectory,
        )

        if (result.isSuccess && hasStopped(pid)) {
//            if(isTimedOut) {
//                // TODO Cleanup pid file that may not have been deleted yet
//            }
            // TODO Exit process successfully.
            info("Solr process with PID $pid has been stopped.")
        }

        // Process has not stopped, likely due to timeout
        threadDump(pid)
        killSolrProcess(pid)

        if (hasStopped(pid)) {
            // TODO Successfully stopped process by killing it
        } else {
            // TODO Failed to forcefully stop process, exit with error
        }
    }

    /**
     * Threaddumps with jstack or jattach (if available) the process provided.
     *
     * @param pid Process ID of a java process.
     */
    private suspend fun threadDump(pid: Long) {
        val jstackExists = CommandChecker.commandExists(javaOptions.jstackExec).getOrNull() == true

        if (jstackExists) {
            debug(verbose) { "Solr process $pid is still running; jstacking it now." }
            CommandExecutor.executeInForeground(
                command = arrayOf(javaOptions.jstackExec, "$pid"),
            )
            return
        }

        // If jstack does not exist continue with jattach
        val jattachExists = CommandChecker.commandExists("jattach").getOrNull() == true

        if (jattachExists) {
            debug(verbose) { "Solr process $pid is still running; jattach threaddumping it now." }
            CommandExecutor.executeInForeground(command = arrayOf("jattach", "$pid", "threaddump"))
        }
    }

    /**
     * Forcefully stops the process with the given [pid].
     *
     * @param pid Process ID of the process to kill.
     */
    private suspend fun killSolrProcess(pid: Long) {
        warn(message = "Solr process $pid is still running; forcefully killing it now.")

        val result = ProcessKiller.killProcess(pid)

        if (result.isSuccess) warn(message = "Killed process with PID $pid.")
        else err(message = "Failed to kill process with PID $pid.")
    }

    private suspend fun removePidFile() {
        TODO("Not yet implemented")
        // val pidFile = Path.of(pidDirectory.absolutePathString(), "solr-$port.pid").toFile()
        // TODO Implement process for deleting file / PID file
    }

    private suspend fun hasStopped(pid: Long, waitMs: Long = -1): Boolean {
        val hasStopped = withTimeoutOrNull(waitMs) {
            do {
                delay(500)
            } while (ProcessAnalyzer.getProcessState(pid).getOrNull()?.isRunning != true)
            return@withTimeoutOrNull true
        } ?: false

        // Check one last time, in case waitMs is negative, it will be checked exactly once
        if (!hasStopped) return ProcessAnalyzer.getProcessState(pid).getOrNull()?.isRunning == true
        return true
    }

    /*

      DIR="$1"
      SOLR_PORT="$2"
      THIS_STOP_PORT="${STOP_PORT:-$((SOLR_PORT - 1000))}"
      STOP_KEY="$3"
      SOLR_PID="$4"

      # Note the space after '$('. It is needed to avoid confusion with special bash eval syntax
      STAT=$( (ps -o stat='' -p "$SOLR_PID" || :) | tr -d ' ')
      if [[ "${STAT:-Z}" != "Z" ]]; then
        if [ -n "${JSTACK:-}" ]; then
          echo -e "Solr process $SOLR_PID is still running; jstacking it now."
          $JSTACK "$SOLR_PID"
        elif [ "$JATTACH" != "" ]; then
          echo -e "Solr process $SOLR_PID is still running; jattach threaddumping it now."
          $JATTACH "$SOLR_PID" threaddump
        fi
        echo -e "Solr process $SOLR_PID is still running; forcefully killing it now."
        kill -9 "$SOLR_PID"
        echo "Killed process $SOLR_PID"
        rm -f "$SOLR_PID_DIR/solr-$SOLR_PORT.pid"
        sleep 10
      fi

      # Note the space after '$('. It is needed to avoid confusion with special bash eval syntax
      STAT=$( (ps -o stat='' -p "$SOLR_PID" || :) | tr -d ' ')
      if [ "${STAT:-}" == "Z" ]; then
        # This can happen if, for example, you are running Solr inside a docker container with multiple processes
        # rather than running it is as the only service. The --init flag on docker avoids that particular problem.
        echo -e "Solr process $SOLR_PID has terminated abnormally. Solr has exited but a zombie process entry remains."
        exit 1
      elif [ -n "${STAT:-}" ]; then
        echo "ERROR: Failed to kill previous Solr Java process $SOLR_PID ... script fails."
        exit 1
      fi
     */
}
