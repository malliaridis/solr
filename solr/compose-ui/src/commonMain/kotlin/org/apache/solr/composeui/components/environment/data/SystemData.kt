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

package org.apache.solr.composeui.components.environment.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SystemData(
    val mode: SystemMode = SystemMode.Unknown,
    val zkHost: String = "",
    @SerialName("solr_home")
    val solrHome: String = "",
    @SerialName("core_root")
    val coreRoot: String = "",
    val lucene: Versions = Versions(),
    val jvm: JvmData = JvmData(),
    val security: SecurityConfig = SecurityConfig(),
    val system: SystemInformation = SystemInformation(),
    val node: String = "",
)
