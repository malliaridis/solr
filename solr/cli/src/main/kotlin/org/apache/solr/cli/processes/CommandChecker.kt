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

import com.github.ajalt.clikt.core.BaseCliktCommand
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Checker that holds functions for checking The existence of commands in various ways.
 */
object CommandChecker {

    /**
     * Checks whether a [command] exists.
     *
     * @param command The command to check.
     * @return Whether the command exists or not. Result fails with error if checking fails
     * with an error.
     */
    suspend fun BaseCliktCommand<*>.commandExists(command: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Run 'command -v' or 'which' with the command you want to check
            val processBuilder = ProcessBuilder(
                "bash", "-c",
                "command -v $command"
            )
            val process = processBuilder.start()

            BufferedReader(InputStreamReader(process.inputStream)).use {
                val line = it.readLine()
                // If there’s output, the command exists; otherwise, it doesn’t
                return@withContext Result.success(line != null && line.isNotEmpty())
            }
        } catch (exception: Exception) {
            echo(message = "An error occurred while checking the presence of a command", err = true)
            echo(message = exception.message, err = true)
            return@withContext Result.failure(exception)
        }
    }
}
