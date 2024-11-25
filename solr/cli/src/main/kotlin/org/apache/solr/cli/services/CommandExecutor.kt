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

package org.apache.solr.cli.services

import java.nio.file.Path
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Collection of functions that allow the execution of commands.
 */
object CommandExecutor {

    /**
     * Launches the [command] inside the given [workingDir] in a managed IO coroutine.
     *
     * This is more of a "managed" execution, rather than a "foreground" execution in terms of
     * threads and coroutines.
     *
     * @param command Command arguments to pass to the process builder.
     * @param workingDir Working directory in which the process should be executed.
     * @see executeInBackground
     */
    suspend fun executeInForeground(
        command: Array<String>,
        workingDir: Path? = null,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val process = with(ProcessBuilder(*command)) {
                if (workingDir != null) directory(workingDir.toFile())
                inheritIO() // Redirects I/O so Ctrl+C works
                start()
            }

            // Wait for the process to complete
            process.waitFor()
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    /**
     * Launches the [command] inside the given [workingDir] in global scope.
     *
     * The coroutine launched in GlobalScope is not subject to the principle of structured
     * concurrency, and therefore is not managed by the CLI anymore once executed. Any output is
     * redirected to a file inside [logsDir]. The PID file is stored in [pidDir].
     *
     * If an [identifier] is provided, it will be used as a file name suffix for the logs and PID
     * file.
     *
     * @param command Command arguments to pass to the process builder.
     * @param workingDir Working directory in which the process should be executed.
     * @param logsDir Directory where the generated log file should be stored.
     * @param pidDir Directory where the generated PID file should be stored.
     * @param identifier Unique identifier to use as file name suffix for log and PID files.
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun executeInBackground(
        command: Array<String>,
        workingDir: Path? = null,
        logsDir: Path? = null,
        pidDir: Path? = null,
        identifier: String? = null,
    ) = GlobalScope.launch {
        try {
            val logFile = logsDir?.let { it.resolve("solr${identifier ?: "-$it"}-console.log").toFile() }
            val pidFile = pidDir?.let { it.resolve("solr${identifier ?: "-$it"}.pid").toFile() }

            val process = with(ProcessBuilder(*command)) {
                if(workingDir != null) directory(workingDir.toFile())
                // Set output and error redirection to the log file
                redirectOutput(logFile)
                redirectErrorStream(true)
                start()
            }

            pidFile?.writeText(process.pid().toString())
        } catch (_: Exception) {
            // ignore any errors
        }
    }
}
