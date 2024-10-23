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

package org.apache.solr.cli.utils

import org.apache.solr.cli.data.OperatingSystem

object Utils {

    /**
     * Searches for a locally running Solr instance and returns the PID if found.
     *
     * @param port The port the Solr instance is running on.
     * @return Process ID (PID) of the running Solr instance.
     */
    suspend fun findSolrPIDByPort(port: Int) : String? {
        TODO("Not yet implemented")
    }

    /**
     * Retrieves the Jetty port (jetty.port) from a running Solr instance.
     *
     * @param pid The process ID of the Solr instance to get the port for.
     * @return The Jetty port (value of `-Djetty.port`).
     */
    suspend fun getJettyPort(pid: String): Int? {
        TODO("Not yet implemented")
    }
}
