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

package org.apache.solr.cli.commands

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.file
import java.io.File
import org.apache.solr.cli.domain.StartExample

class ExampleCommand : SuspendingCliktCommand(name = "example") {

    private val serverDir by option(
        "--server-dir",
        envvar = "SOLR_SERVER_DIR",
        valueSourceKey = "solr.server.dir",
    ).help("Path to the Solr server directory.")
        .file(canBeFile = false, canBeDir = true)
    // TODO Consider suing ./server as default
    // TODO Make required and use defaults

    // TODO Move example to ExampleCommand
    private val example by option("-e", "--example")
        .help("Name of the example to launch, one of: cloud, techproducts, schemaless, films.")
        .enum<StartExample>(ignoreCase = true)

    // TODO Move exampleDir to be part of ExampleCommand
    private val exampleDir by option("--example-dir")
        .help("Path to the Solr example directory; if not provided, \${serverDir}/../example is expected to exist.")
        .file(canBeFile = false, canBeDir = true)
        // TODO Uncomment .defaultLazy
        // .defaultLazy { File(serverDir.parent, "example") }
    // TODO Test how library behaves if exampleDir default is file and not dir

    // TODO Move option to ExampleCommand
    // TODO Consider replacing with --prompt and use --no-prompt behavior by default
    private val noPrompt by option("--no-prompt")
        .help("Don't prompt for input; accept all defaults when running examples that accept user input.")
        .flag()

    override suspend fun run() {
        echo("Example World!")
        // TODO Not yet implemented
    }

    private fun runCloudExample() {

    }

    private fun runUserManagedExample() {
        initializeExampleDirectory()
    }

    /**
     * Initializes the example directory by checking and creating all necessary files in the server
     * directory.
     */
    private fun initializeExampleDirectory() {
        val solrXml = File(serverDir, "solr/solr.xml")
        require(solrXml.isFile) {
            "Could not find solr.xml at ${solrXml.absolutePath}. Please specify a valid --server-dir."
        }
        TODO("Not yet implemented.")
    }
}
