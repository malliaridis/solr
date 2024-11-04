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
import com.github.ajalt.clikt.parameters.types.long
import kotlinx.coroutines.time.withTimeout
import kotlinx.coroutines.withTimeout
import org.apache.solr.cli.Environment
import org.apache.solr.cli.options.AuthOptions
import org.apache.solr.cli.options.JavaOptions
import org.apache.solr.cli.options.SecurityOptions
import org.apache.solr.cli.options.SolrContextOptions
import org.apache.solr.cli.options.StopOptions
import org.apache.solr.cli.processes.JavaExecutor

// TODO Consider stop command for remote server as well?
internal class StopCommand : SuspendingCliktCommand(name = "stop") {

    private val javaOptions by JavaOptions()

    private val contextOptions by SolrContextOptions()

    private val port by option("-p", "--port")
        .help("Specify the port the Solr HTTP listener is bound to.")
        .int()
        .default(
            value = Environment.SOLR_PORT,
            defaultForHelp = "Uses environment variable SOLR_PORT if present and falls back to 8983."
        )

    private val securityOptions by SecurityOptions(port = { port })

    private val authOptions by AuthOptions(echo = { echo(message = it) })

    private val stopOptions by StopOptions(port = { port })

    private val toolOptions by option(
        envvar = "SOLR_TOOL_OPTS",
        valueSourceKey = "solr.tool.options",
        hidden = true,
    ).multiple()

    private val all by option("--all")
        .help("Find and stop all running Solr servers on this host")

    private val force by option("-f", "--force")
        .help("Force option in case Solr is run as root.")
        .flag()

    private val verbose by option("--verbose")
        .help("Enable verbose command output.")
        .flag()

    private val waitTimeMs by option(
        envvar = "SOLR_STOP_WAIT",
        valueSourceKey = "solr.stop.waitMs",
    ).long()
        .default(180000)

    override suspend fun run() {
        val solrInstances = findSolrInstances()
        solrInstances.forEach { pid ->

            // TODO stopSolrInstance(port)
            // TODO Check if process still running or stop timed out
            // TODO killSolrProcess(pid)
        }
        stopSolrInstances(solrInstances)

        // TODO remove pid files from stopped solr instances if present

        TODO("Not yet implemented")

    }

    override fun helpEpilog(context: Context): String =
        "NOTE: To see if any Solr servers are running, do: solr status"

    private suspend fun findSolrInstances(): List<String> {
        TODO("Not yet implemented")
    }

    private suspend fun stopSolrInstance(port: Int, pid: String) {
        echo(
            message = """Sending stop command to Solr running on port $port ... waiting up to
                         $waitTimeMs seconds to allow Jetty process $pid to stop gracefully.
                         """.trimMargin(),
            err = true,
        )

        val stopArguments = arrayOf<String>(
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
        withTimeout(waitTimeMs) {
            JavaExecutor.executeInForeground(
                command = stopArguments,
                workingDir = contextOptions.serverDir,
            )
        }
        TODO("Not yet implemented")
    }

    private suspend fun killSolrProcess(pid: String) {
        TODO("Not yet implemented")
    }

    private suspend fun stopSolrInstances(solrInstances: List<String>) {
        TODO("Not yet implemented")
        /*

      DIR="$1"
      SOLR_PORT="$2"
      THIS_STOP_PORT="${STOP_PORT:-$((SOLR_PORT - 1000))}"
      STOP_KEY="$3"
      SOLR_PID="$4"

      if [ -n "$SOLR_PID"  ]; then
        echo -e "Sending stop command to Solr running on port $SOLR_PORT ... waiting up to $SOLR_STOP_WAIT seconds to allow Jetty process $SOLR_PID to stop gracefully."
        # shellcheck disable=SC2086
        "$JAVA" $SOLR_SSL_OPTS $AUTHC_OPTS ${SOLR_TOOL_OPTS:-} -jar "$DIR/start.jar" "STOP.PORT=$THIS_STOP_PORT" "STOP.KEY=$STOP_KEY" --stop || true
          (loops=0
          while true
          do
            # Check if a process is running with the specified PID.
            # -o stat will output the STAT, where Z indicates a zombie
            # stat='' removes the header (--no-headers isn't supported on all platforms)
            # Note the space after '$('. It is needed to avoid confusion with special bash eval syntax
            STAT=$( (ps -o stat='' -p "$SOLR_PID" || :) | tr -d ' ')
            if [[ "${STAT:-Z}" != "Z" ]]; then
              slept=$((loops * 2))
              if [ $slept -lt $SOLR_STOP_WAIT ]; then
                sleep 2
                loops=$((loops+1))
              else
                exit # subshell!
              fi
            else
              exit # subshell!
            fi
          done) &
        spinner $!
        rm -f "$SOLR_PID_DIR/solr-$SOLR_PORT.pid"
      else
        echo -e "No Solr nodes found to stop."
        exit 0
      fi

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
}
