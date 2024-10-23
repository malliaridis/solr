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

enum class OperatingSystem {

    /**
     * Fallback value of unknown operating systems.
     */
    Unknown,

    /**
     * Enum value for Windows systems.
     */
    Windows,

    /**
     * Enum value for MacOs systems.
     */
    MacOs,

    /**
     * Enum value for any other Unix/Linux systems.
     */
    Unix;

    companion object {

        val current: OperatingSystem
            get() {
                val os = System.getProperty("os.name").lowercase()
                return when {
                    os.contains("win") -> Windows
                    os.contains("mac") -> MacOs
                    os.contains("nux")
                            || os.contains("nix") -> Unix

                    else -> Unknown
                }
            }
    }
}
