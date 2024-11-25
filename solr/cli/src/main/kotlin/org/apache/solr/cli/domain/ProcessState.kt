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

package org.apache.solr.cli.domain

/**
 * This enum holds various process states we are interested in.
 *
 * This is neither a complete list nor accurately represent the process states of on OS, but it is
 * sufficient for our use cases.
 */
internal enum class ProcessState {

    /**
     * When the state of a process could not be determined, either because it was not found or as a
     * fallback.
     */
    Unknown,

    /**
     * The running process state indicates a process that is doing some work.
     */
    Running,

    /**
     * The stopped state indicates that a process has been found and was stopped.
     */
    Stopped,

    /**
     * The zombie state (not relevant on Windows) indicates that the process has completed execution
     * but still has an entry in the process table.
     */
    Zombie;

    val isRunning: Boolean
        get() = this == Running || this == Zombie
}
