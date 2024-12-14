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

package org.apache.solr.cli

import com.github.ajalt.clikt.core.BaseCliktCommand
import com.github.ajalt.mordant.rendering.Theme

internal object EchoUtils {

    fun BaseCliktCommand<*>.debug(
        verbose: Boolean,
        trailingNewline: Boolean = true,
        message: () -> Any?,
    ) {
        if (verbose) echo(Theme.Default.muted(message().toString()), trailingNewline)
    }

    fun BaseCliktCommand<*>.success(message: Any?, trailingNewline: Boolean = true) =
        echo(Theme.Default.success(message.toString()), trailingNewline)

    fun BaseCliktCommand<*>.info(message: Any?, trailingNewline: Boolean = true) =
        echo(Theme.Default.info(message.toString()), trailingNewline)

    fun BaseCliktCommand<*>.warn(message: Any?, trailingNewline: Boolean = true) =
        echo(Theme.Default.warning(message.toString()), trailingNewline)

    /**
     * Named err to avoid conflict with [kotlin.error].
     */
    fun BaseCliktCommand<*>.err(
        error: Throwable? = null,
        message: Any? = error,
        trailingNewline: Boolean = true,
        err: Boolean = true,
    ) {
        if (error != null) {
            echo(Theme.Default.danger(message.toString()), trailingNewline, err)
            echo(Theme.Default.danger(error.message.toString()), trailingNewline, err)
        }
    }

}
