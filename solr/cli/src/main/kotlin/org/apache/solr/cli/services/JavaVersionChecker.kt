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
import kotlinx.io.files.FileNotFoundException
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.apache.solr.cli.domain.OperatingSystem

object JavaVersionChecker {

    /**
     * Java executable with file extension in case of Windows OS.
     */
    private val javaExec = when (OperatingSystem.current) {
        OperatingSystem.Windows -> "java.exe"
        else -> "java"
    }

    /**
     * Path to the current java executable that is operating this CLI. This is used as fallback
     * if the user does not specify a java executable.
     */
    private val javaExecutable
        get() = Path(System.getProperty("java.home"), "bin", javaExec).toString()

    /**
     * Returns Java's major version.
     *
     * @param javaPath The path to the java executable to use.
     */
    suspend fun getJavaMajorVersion(javaPath: String = javaExecutable): Result<Int> =
        withContext(Dispatchers.IO) {
            try {
                // Verify that the java executable exists
                SystemFileSystem.resolve(Path(javaPath))

                // Run '/path/to/java -version' to get java verison output
                val processBuilder = ProcessBuilder(javaPath, "-version")
                // Input stream is for some reason null if error stream not redirected
                processBuilder.redirectErrorStream(true)
                val process = processBuilder.start()

                BufferedReader(InputStreamReader(process.inputStream)).use {
                    // First line of output is something like
                    // openjdk version "21.0.5" 2024-10-15 LTS
                    val line = it.readLine() ?: return@withContext Result.failure(
                        RuntimeException("""Could not find java command at "$javaPath".""")
                    )

                    """"(\d+)(?:\.(\d+))?""".toRegex().find(line)?.let { esult ->
                        val major = esult.groupValues[1].toInt()
                        return@withContext Result.success(
                            if (major == 1) esult.groupValues[2].toInt()
                            else major
                        )
                    }
                    return@withContext Result.failure(
                        RuntimeException("Could not determine java version from output.")
                    )
                }
            } catch (exception: FileNotFoundException) {
                return@withContext Result.failure(
                    FileNotFoundException("""Could not find java executable at "$javaPath".""")
                )
            } catch (exception: Exception) {
                return@withContext Result.failure(exception)
            }
        }
}
