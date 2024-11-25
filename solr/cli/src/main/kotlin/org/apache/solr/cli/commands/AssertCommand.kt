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
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.groups.single
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import com.github.ajalt.clikt.parameters.types.long
import com.github.ajalt.clikt.parameters.types.path
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.getOwner
import kotlin.io.path.notExists
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.milliseconds
import org.apache.solr.cli.domain.SolrState
import org.apache.solr.cli.options.CommonOptions.credentialsOption
import org.apache.solr.cli.options.CommonOptions.verboseOption
import org.apache.solr.cli.services.PrivilegeChecker
import org.apache.solr.cli.services.SolrStateAnalyzer

class AssertCommand : SuspendingCliktCommand(name = "assert") {

    private val currentUser = System.getProperty("user.name")

    private val rootAssertion by option()
        .help("Assert that the current user is or is not a root user.")
        .switch(mapOf("--root" to true, "--not-root" to false))

    private val isStartedOption = option("--started", metavar = "url")
        .help("Assert that Solr is running on a certain URL (supports timeout).")

    private val isNotStartedOption = option("--not-started", metavar = "url")
        .help("Assert that Solr is not running anymore on a certain URL (supports timeout).")

    private val isCloudStartedOption = option("--cloud", metavar = "url")
        .help("Assert that Solr is running in cloud mode on a certain URL (supports timeout).")

    private val isUserManagedStartedOption = option("--user-managed", metavar = "url")
        .help("Assert that Solr is running in user-managed mode on a certain URL (supports timeout).")

    private val runningAssertion by mutuallyExclusiveOptions(
        isNotStartedOption.convert { RunningAssertion.NotStarted(it) },
        isStartedOption.convert { RunningAssertion.Started(it) },
        isCloudStartedOption.convert { RunningAssertion.CloudStarted(it) },
        isUserManagedStartedOption.convert { RunningAssertion.UserManagedStarted(it) },
    ).single()

    private val directoryExistsOption = option("--exists")
        .help("Assert that the directory exists.")
        .path()

    private val directoryNotExistsOption = option("--not-exists")
        .help("Assert that the directory does NOT exist.")
        .path()

    private val directoryExistenceAssertion by mutuallyExclusiveOptions(
        option1 = directoryExistsOption.convert { ExistenceAssertion.Exists(it) },
        option2 = directoryNotExistsOption.convert { ExistenceAssertion.NotExists(it) },
    ).single()

    private val sameUserPath by option("--same-user")
        .help("Assert that we run as same user that owns the directory.")
        .path(mustExist = true)

    private val message by option("--message")
        .help("Override the assertion message in case of failure.")

    private val timeout by option("--timeout", metavar = "ms")
        .help("Timeout in ms for commands supporting a timeout.")
        .long()
        .default(1000)

    private val useExitCode by option("--exit-code")
        .help("Exit with an exit code instead of an assertion message in case of failure.")
        .flag()

    private val credentials by credentialsOption

    private val isVerbose by verboseOption

    override suspend fun run() {
        runAssertions().onSuccess { failedTests -> exitProcess(failedTests) }
            .onFailure { exception ->
                echo(
                    message = if (isVerbose) exception else exception.message ?: throw exception,
                    err = true
                )
                exitProcess(100)
            }
    }

    private suspend fun runAssertions(): Result<Int> = try {
        var failures = 0
        rootAssertion?.let { shouldRoot ->
            val isRoot = PrivilegeChecker.isRootUser().getOrThrow()
            if (shouldRoot && !isRoot) failures += incOrFail("Must run as root user")
            else if (!shouldRoot && isRoot) failures += incOrFail("Not allowed to run as root user")
        }

        directoryExistenceAssertion?.let { assertion ->
            when (assertion) {
                is ExistenceAssertion.Exists -> if (assertion.path.notExists()) {
                    failures += incOrFail("Directory ${assertion.path} does not exist.")
                }

                is ExistenceAssertion.NotExists -> if (assertion.path.exists()) {
                    failures += incOrFail("Directory ${assertion.path} exists.")
                }
            }
        }

        sameUserPath?.let {
            failures += if (it.exists()) {
                val pathOwner = it.getOwner()?.name
                if (currentUser == pathOwner) 0
                else incOrFail("Must run as user $pathOwner. We are $currentUser")
            } else incOrFail("Directory $it does not exist.")
        }

        runningAssertion?.let  { assertion ->
            failures += when (assertion) {
                is RunningAssertion.NotStarted -> {
                    val state = SolrStateAnalyzer.getSolrState(
                        url = assertion.url,
                        credentials = credentials,
                        timeout = timeout.milliseconds,
                    )
                    if (!state.isOnline) 0 else 1
                }

                is RunningAssertion.Started -> {
                    val state = SolrStateAnalyzer.getSolrState(
                        url = assertion.url,
                        credentials = credentials,
                        timeout = timeout.milliseconds,
                    )
                    if (state.isOnline) 0 else 1
                }

                is RunningAssertion.CloudStarted -> {
                    val state = SolrStateAnalyzer.getSolrState(
                        url = assertion.url,
                        credentials = credentials,
                        timeout = timeout.milliseconds,
                    )
                    if (state is SolrState.Online && state.mode.isCloud) 0 else 1
                }

                is RunningAssertion.UserManagedStarted -> {
                    val state = SolrStateAnalyzer.getSolrState(
                        url = assertion.url,
                        credentials = credentials,
                        timeout = timeout.milliseconds,
                    )
                    if (state is SolrState.Online && state.mode.isUserManaged) 0 else 1
                }
            }
        }

        Result.success(failures)
    } catch (exception: Throwable) {
        Result.failure(exception)
    }

    private fun incOrFail(message: String): Int {
        return if (useExitCode) 1
        else throw AssertionError(message)
    }

    /**
     * Helper class for mutually exclusive running state assertion.
     */
    private sealed interface RunningAssertion {

        val url: String

        data class NotStarted(override val url: String) : RunningAssertion

        data class Started(override val url: String) : RunningAssertion

        data class CloudStarted(override val url: String) : RunningAssertion

        data class UserManagedStarted(override val url: String) : RunningAssertion
    }

    /**
     * Helper class for mutually exclusive assertion of a directory existence.
     */
    private sealed interface ExistenceAssertion {

        val path: Path

        data class Exists(override val path: Path) : ExistenceAssertion

        data class NotExists(override val path: Path) : ExistenceAssertion
    }
}
