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

package org.apache.solr.cli.data

/**
 * User limits returned by the `ulimit` command. Negative values indicate unlimited / undefined
 * values.
 *
 * @property openFiles The maximum number of open file descriptors. Equivalent to `ulimit -n`.
 * @property maxProcesses The maximum number of processes available to a single user. Equivalent to `ulimit -u`.
 * @property virtualMemory The maximum amount of virtual memory available to the process. Equivalent to `ulimit -v`.
 * @property maxMemory The maximum resident set size. Equivalent to `ulimit -m`.
 */
data class UserLimits(
    val openFiles: Long = -1,
    val maxProcesses: Long = -1,
    val virtualMemory: Long = -1,
    val maxMemory: Long = -1,
) {

    val isOpenFilesUnlimited: Boolean
        get() = openFiles < 0

    val isMaxProcessesUnlimited: Boolean
        get() = maxProcesses < 0

    val isVirtualMemoryUnlimited: Boolean
        get() = virtualMemory < 0

    val isMaxMemoryUnlimited: Boolean
        get() = maxMemory < 0

    /**
     * Enum that holds the relevant user limit names with their corresponding `ulimit` flag.
     *
     * Note that some flags may not be supported by all platforms.
     */
    enum class UserLimitType(val flag: String) {
        OpenFiles(flag = "n"),
        MaxProcesses(flag = "u"),
        VirtualMemory(flag = "v"),
        MaxMemory(flag = "m"),
    }
}
