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

import com.github.ajalt.clikt.core.BaseCliktCommand
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.solr.cli.EchoUtils.err
import org.apache.solr.cli.EchoUtils.info
import org.apache.solr.cli.EchoUtils.warn
import org.apache.solr.cli.domain.UserLimits
import org.apache.solr.cli.domain.UserLimits.UserLimitType
import org.apache.solr.cli.exceptions.CommandNotFoundException
import org.apache.solr.cli.services.CommandChecker.commandExists

internal object UserLimitsChecker {

    /**
     * Checks the user limits (ulimit) and echos warnings if any of the values is below [minLimits].
     *
     * @param minLimits The values to check against the user limits.
     */
    suspend fun BaseCliktCommand<*>.checkUserLimits(
        minLimits: UserLimits,
    ) = withContext(Dispatchers.IO) {
        getUserLimits().onSuccess { limits ->
            val warnOpenFiles =
                !limits.isOpenFilesUnlimited && limits.openFiles < minLimits.openFiles
            if (warnOpenFiles) warn(
                message = """Your open file limit is currently ${limits.openFiles}.
                    |  It should be set to ${minLimits.openFiles} to avoid operational disruption.
                """.trimMargin(),
            )

            val warnMaxProcesses =
                !limits.isMaxProcessesUnlimited && limits.maxProcesses < minLimits.maxProcesses
            if (warnMaxProcesses) warn(
                message = """Your max processes limit is currently ${limits.maxProcesses}.
                    |  It should be set to ${minLimits.maxProcesses} to avoid operational disruption.
                """.trimMargin(),
            )

            val warnVirtualMemory =
                !limits.isVirtualMemoryUnlimited && limits.virtualMemory < minLimits.virtualMemory
            if (warnVirtualMemory) warn(
                message = """Your virtual memory limit is currently ${limits.virtualMemory}.
                    |  It should be set to ${minLimits.virtualMemory} to avoid operational disruption.
                """.trimMargin(),
            )

            val warnMaxMemory =
                !limits.isMaxMemoryUnlimited && limits.maxMemory < minLimits.maxMemory
            if (warnMaxMemory) warn(
                message = """Your max max memory limit is currently ${limits.maxMemory}.
                    |  It should be set to ${minLimits.maxMemory} to avoid operational disruption.
                """.trimMargin(),
            )

            if (warnOpenFiles || warnMaxProcesses || warnVirtualMemory || warnMaxMemory)
                info("To suppress ulimit warnings, set solr.ulimit.checks to false")
        }.onFailure {
            warn("Could not check ulimits for processes and open files.")
            with(minLimits) {
                info(
                    message = """The recommended values are:
                    |  Open files:      ${if (isOpenFilesUnlimited) "unlimited" else openFiles}
                    |  Max processes:   ${if (isMaxProcessesUnlimited) "unlimited" else maxProcesses}
                    |  Virtual memory:  ${if (isVirtualMemoryUnlimited) "unlimited" else virtualMemory}
                    |  Max memory size: ${if (isMaxMemoryUnlimited) "unlimited" else maxMemory}
                """.trimMargin()
                )
            }
            info("To suppress ulimit warnings, set solr.ulimit.checks to false")
        }
    }

    /**
     * Retrieve the user limit values (ulimit).
     *
     * @return A result with the values if the ulimit command is available and the values were
     * loaded successfully.
     */
    suspend fun BaseCliktCommand<*>.getUserLimits(): Result<UserLimits> =
        withContext(Dispatchers.IO) {
            try {
                commandExists("ulimit")
                    .onSuccess { exists ->
                        if (!exists)return@withContext Result.failure(
                            CommandNotFoundException(command = "ulimit")
                        )
                    }
                    .onFailure { return@withContext Result.failure(it) }

                return@withContext Result.success(
                    UserLimits(
                        openFiles = getUserLimitFor(UserLimitType.OpenFiles),
                        maxProcesses = getUserLimitFor(UserLimitType.MaxProcesses),
                        virtualMemory = getUserLimitFor(UserLimitType.VirtualMemory),
                        maxMemory = getUserLimitFor(UserLimitType.MaxMemory),
                    )
                )
            } catch (exception: Exception) {
                err(message = "An error occurred while getting the user limits.", error = exception)
                return@withContext Result.failure(exception)
            }
        }

    private suspend fun getUserLimitFor(limitType: UserLimitType): Long =
        withContext(Dispatchers.IO) {
            val processBuilder = ProcessBuilder("bash", "-c", "ulimit -${limitType.flag}")
            val process = processBuilder.start()

            // Read and print the output
            val value: Long
            BufferedReader(InputStreamReader(process.inputStream)).use {
                val line = it.readLine()
                value = line.toLongOrNull() ?: -1
                // If there’s output, the command exists; otherwise, it doesn’t
                return@withContext value
            }
        }
}
