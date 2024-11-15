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

import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.solr.cli.data.OperatingSystem

object ProcessKiller {

    suspend fun killProcess(pid: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val process = ProcessBuilder(*getKillCommand(pid))
                .inheritIO() // Redirects I/O so Ctrl+C works
                .start()

            // Wait for the process to complete
            process.waitFor()
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    private fun getKillCommand(pid: Long): Array<String> =
        when (OperatingSystem.current) {
            OperatingSystem.Windows -> arrayOf("taskkill", "/F", "/PID", "$pid")
            else -> arrayOf("kill", "-9", "$pid")
        }
}