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

package org.apache.solr.cli.processes

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.solr.cli.domain.FileExtensions
import org.apache.solr.cli.domain.FileExtensions.glob
import org.apache.solr.cli.domain.OperatingSystem
import org.apache.solr.cli.domain.ProcessState
import org.apache.solr.cli.domain.SolrProcess
import org.apache.solr.cli.exceptions.ProcessNotFoundException
import org.apache.solr.cli.utils.Utils

/**
 * Object that holds functions for fetching process information.
 */
internal object ProcessAnalyzer {

    /**
     * Lookup processes by filter keywords.
     *
     * @param keywords List of keywords to use for filtering.
     * @return A list of process IDs of all Solr processes found.
     */
    suspend fun findProcesses(vararg keywords: String): Result<List<Long>> {
        val command = getProcessesCommand(keywords)
        return withContext(Dispatchers.IO) {
            try {
                val process = ProcessBuilder(*command).start()

                BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                    val lines = reader.lines().toList()
                        .filter(String::isNotBlank)
                        .mapNotNull { it.split("=").getOrNull(1) }
                    val ports = lines.map(String::toLong)
                    return@withContext Result.success(ports)
                }
            } catch (exception: Exception) {
                return@withContext Result.failure(exception)
            }
        }
    }

    /**
     * Fetches the state of a process with a given ID.
     *
     * @param pid Process ID to look for.
     * @return A result with the process state.
     */
    suspend fun getProcessState(pid: Long): Result<ProcessState> {
        TODO("Not yet implemented")
        /*
        STAT=$( (ps -o stat='' -p "$SOLR_PID" || :) | tr -d ' ')
        if STAT is not empty and contains Z -> Zombie thread
        else if STAT not emtpy -> Not stopped
        else if STAT empty --> Stopped
         */
    }

    /**
     * Retrieve the arguments of the process with the given [pid].
     *
     * @param pid Process ID of the process to get the argument from.
     * @return A list of arguments if the process was found.
     */
    suspend fun getProcessArguments(pid: Long): Result<List<String>> {
        val command = getProcessArgumentsCommand(pid)

        return withContext(Dispatchers.IO) {
            try {
                val process = ProcessBuilder(*command).start()

                BufferedReader(InputStreamReader(process.inputStream)).use {
                    val line = it.lines().toList().firstOrNull(String::isNotBlank)
                        ?: throw ProcessNotFoundException(pid)

                    // If there’s output, the process exists; otherwise, it doesn’t
                    return@withContext Result.success(
                        line.split(" ")
                            .filter(String::isNotBlank)
                    )
                }
            } catch (exception: Exception) {
                return@withContext Result.failure(exception)
            }
        }
    }

    /**
     * Get a platform-specific command for fetching a list of Solr process IDs.
     *
     * @param keywords List of keywords to use for filtering
     * @return A list of strings representing a process command for fetching the Solr process IDs.
     */
    private fun getProcessesCommand(keywords: Array<out String>): Array<String> =
        when (OperatingSystem.current) {
            OperatingSystem.Windows -> arrayOf(
                "wmic",
                "process",
                "where",
                // filter by keywords and exclude wmic (this) process
                (keywords.map { "CommandLine like '%$it%'" } + "not CommandLine like '%wmic%'")
                    .joinToString(
                        separator = " and ",
                        prefix = "\"",
                        postfix = "\"",
                    ),
                "get",
                "processid", // get only process ids
                "/format:list", // output will be ProcessId=[PID] and blank lines
            )

            else -> arrayOf(
                "ps", "auxww", // get the list of processes
                *keywords.map { listOf("|", "grep", "-F", "'$it'") }
                    .flatten()
                    .toTypedArray(),
                "|", "awk", "'{print $2}'") // get the parent's process ID (PPID)
        }

    /**
     * Get a platform-specific command that fetches the command line of a process with a given PID
     * including all arguments passed to it.
     *
     * @param pid The process ID of the process to get the command line from.
     * @return A list of strings representing a process command for fetching the details.
     */
    private fun getProcessArgumentsCommand(pid: Long): Array<String> =
        when (OperatingSystem.current) {
            OperatingSystem.Windows -> arrayOf(
                "wmic", // wmic is deprecated
                "process",
                "where",
                "ProcessID=$pid",
                "get",
                "commandline",
                "/format:list",
            )

            else -> arrayOf("ps", "-fww", "-p $pid")
        }

    /**
     * Scans the given [directory] for PID files and returns a collection of [SolrProcess]es.
     *
     * @return A collection of [SolrProcess]es for each PID file found.
     */
    suspend fun getProcessesByPidFiles(directory: Path): Collection<SolrProcess> =
        directory.listDirectoryEntries(FileExtensions.PID.glob).mapNotNull { pidFile ->
            // TODO See if windows PID files need different treatment
            val pid = pidFile.toFile().readLines().first().toLong()
            return@mapNotNull Utils.getSolrProcessByPid(pid)
        }
}
