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
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import kotlin.io.path.absolutePathString

internal class JavaOptions : OptionGroup(
    name = "Java Options",
    help = "Options that configure the java environment."
) {

    val javaHome by option(
        envvar = "JAVA_HOME",
        valueSourceKey = "java.home",
        hidden = true,
    ).path()

    val solrJavaHome by option(
        envvar = "SOLR_JAVA_HOME",
        valueSourceKey = "solr.java.home",
        hidden = true,
    ).path(canBeFile = false, canBeDir = true)

    val javaExec by option(hidden = true)
        .defaultLazy {
            val javaDir = solrJavaHome ?: javaHome ?: return@defaultLazy "java"
            javaDir.resolve("bin/java").absolutePathString()
        }

    val jstackExec by option(hidden = true)
        .defaultLazy {
            val javaDir = solrJavaHome ?: javaHome ?: return@defaultLazy "jstack"
            javaDir.resolve("bin/jstack").absolutePathString()
        }
}
