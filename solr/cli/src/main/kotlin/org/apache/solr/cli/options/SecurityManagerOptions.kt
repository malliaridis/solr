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

package org.apache.solr.cli.options

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

internal class SecurityManagerOptions(private val serverDir: () -> Path) : OptionGroup() {

    val isSecurityManagerEnabled by option(
        envvar = "SOLR_SECURITY_MANAGER_ENABLED",
        valueSourceKey = "solr.security.manager.enabled",
        hidden = true,
    ).flag()

    fun composeSecurityManagerOptions(): Array<String> {
        return if (!isSecurityManagerEnabled) emptyArray()
        else arrayOf(
            "-Djava.security.manager",
            "-Djava.security.policy=${
                Path(serverDir().absolutePathString(), "etc", "security.policy")
                    .absolutePathString()
            }",
            "-Djava.security.properties=${
                Path(serverDir().absolutePathString(), "etc", "security.properties")
                    .absolutePathString()
            }",
            "-Dsolr.internal.network.permission=*",
        )
    }
}
