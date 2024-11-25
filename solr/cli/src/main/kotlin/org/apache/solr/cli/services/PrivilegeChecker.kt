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

import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.solr.cli.domain.OperatingSystem

internal object PrivilegeChecker {

    /**
     * Checks if the current user is root / admin.
     */
    suspend fun isRootUser(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            when (OperatingSystem.current) {
                OperatingSystem.Windows -> isRoot()
                else -> isRoot()
            }
        } catch (exception: Exception) {
            return@withContext Result.failure(exception)
        }
    }

    /**
     * Windows-specific check for whether the user is an admin user.
     */
    private suspend fun isAdmin() = withContext(Dispatchers.IO) {
        val process = ProcessBuilder("whoami", "/groups").start()
        BufferedReader(InputStreamReader(process.inputStream)).use {
            return@withContext Result.success(
                it.lines().anyMatch { line: String -> line.contains("S-1-5-32-544") }
            )
        }
    }

    /**
     * Unix/Linux-specific check for whether the user is a root user.
     */
    private suspend fun isRoot() = withContext(Dispatchers.IO) {
        val process = ProcessBuilder("id", "-u").start()
        BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
            return@withContext Result.success("0" == reader.readLine())
        }
    }
}
