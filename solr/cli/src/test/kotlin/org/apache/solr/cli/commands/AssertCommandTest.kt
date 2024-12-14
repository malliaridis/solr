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

import com.github.ajalt.clikt.command.test
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.context
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.apache.solr.cli.Environment.SOLR_PORT
import org.apache.solr.cli.StatusCode
import org.apache.solr.cli.TestTimeouts.deltaTime
import org.apache.solr.cli.TestTimeouts.responseTime
import org.apache.solr.cli.TestTimeouts.startTimeout
import org.apache.solr.cli.TestTimeouts.testTimeout
import org.apache.solr.cli.TestUtils.runWithTimeout
import org.junit.jupiter.api.io.TempDir

class AssertCommandTest {

    @field:TempDir
    lateinit var tempFolder: File

    private val startCommand = StartCommand().context {
        exitProcess = { throw CliktError(statusCode = it) }
    }

    private val stopCommand = StopCommand().context {
        exitProcess = { throw CliktError(statusCode = it) }
    }

    private val assertCommand = AssertCommand().context {
        exitProcess = { throw CliktError(statusCode = it) }
    }

    @AfterTest
    fun teardown(): Unit = runBlocking {
        stopCommand.test(
            argv = listOf("--port", SOLR_PORT.toString()),
            envvars = getEnvVars(),
        )
    }

    @Test
    @Ignore
    fun testAssertionsInUserManagedMode() = runTest(timeout = testTimeout) {
        val startResult = runWithTimeout(startTimeout) {
            startCommand.test(
                argv = listOf("--user-managed"),
                envvars = getEnvVars(),
            )
        }
        assertEquals(
            expected = StatusCode.SUCCESS,
            actual = startResult.statusCode,
            message = "Solr should start in user-managed mode",
        )

        val startedResult = runWithTimeout(startTimeout + deltaTime) {
            assertCommand.test(
                argv = listOf(
                    "--started",
                    "http://127.0.0.1:${SOLR_PORT}",
                    "--timeout",
                    "${startTimeout.inWholeMilliseconds}",
                ),
                envvars = getEnvVars(),
            )
        }
        assertEquals(
            expected = StatusCode.SUCCESS,
            actual = startedResult.statusCode,
            message = "assert --started should be successful",
        )

        val isUserManagedResult = runWithTimeout(responseTime) {
            assertCommand.test(
                argv = listOf(
                    "--user-managed",
                    "http://127.0.0.1:${SOLR_PORT}/solr",
                    "--timeout",
                    "${startTimeout.inWholeMilliseconds}",
                ),
                envvars = getEnvVars(),
            )
        }
        assertEquals(
            expected = StatusCode.SUCCESS,
            actual = isUserManagedResult.statusCode,
            message = "assert --user-managed should be successful",
        )

        // TODO See if first assertion is relevant / exists
        assertContains(
            charSequence = isUserManagedResult.stdout,
            other = "needn't include Solr's context-root"
        )
        assertTrue(actual = isUserManagedResult.stderr.isEmpty())

        val isCloudResult = runWithTimeout(responseTime) {
            assertCommand.test(
                argv = listOf("--cloud", "http://127.0.0.1:${SOLR_PORT}"),
                envvars = getEnvVars(),
            )
        }
        assertNotEquals(
            illegal = StatusCode.SUCCESS,
            actual = isCloudResult.statusCode,
            message = "assert --cloud should not succeed",
        )
        assertContains(
            charSequence = isCloudResult.stderr,
            other = "ERROR: Solr is not running in cloud mode"
        )
    }

    @Test
    fun testAssertionsInCloudMode() = runTest(timeout = testTimeout) {
        val startResult = startCommand.test(
            argv = emptyList(),
            envvars = getEnvVars(),
        )
        assertEquals(
            expected = StatusCode.SUCCESS,
            actual = startResult.statusCode,
            message = "Solr should start in cloud mode",
        )

        val startedResult = assertCommand.test(
            argv = listOf(
                "--started",
                "http://127.0.0.1:${SOLR_PORT}",
                "--timeout",
                "${startTimeout.inWholeMilliseconds}",
            ),
            envvars = getEnvVars(),
        )
        assertEquals(
            expected = StatusCode.SUCCESS,
            actual = startedResult.statusCode,
            message = "assert --started should be successful",
        )

        val isCloudResult = runWithTimeout(responseTime) {
            assertCommand.test(
                argv = listOf("--cloud", "http://127.0.0.1:${SOLR_PORT}"),
                envvars = getEnvVars(),
            )
        }
        assertEquals(
            expected = StatusCode.SUCCESS,
            actual = isCloudResult.statusCode,
            message = "assert --cloud should be successful",
        )
        assertTrue(isCloudResult.stderr.isEmpty(), "assert --cloud should ont contain errors")

        val isUserManagedResult = runWithTimeout(responseTime) {
            assertCommand.test(
                argv = listOf("--user-managed", "http://127.0.0.1:${SOLR_PORT}"),
                envvars = getEnvVars(),
            )
        }
        assertNotEquals(
            illegal = StatusCode.SUCCESS,
            actual = isCloudResult.statusCode,
            message = "assert --user-managed should not succeed",
        )
        // TODO Review this assertion
        assertContains(
            charSequence = isUserManagedResult.stderr,
            other = "needn't include Solr's context-root",
            message = "assert --user-managed should contain context-root message"
        )
        assertContains(
            charSequence = isUserManagedResult.stderr,
            other = "ERROR: Solr is not running in user-managed mode",
            message = "assert --user-managed should contain context-root message"
        )
    }

    @Test
    fun testTimeouts() = runTest(timeout = testTimeout) {
        val notStartedResult = runWithTimeout(responseTime + deltaTime) {
            assertCommand.test(
                argv = listOf(
                    "--not-started",
                    "http://127.0.0.1:${SOLR_PORT}",
                    "--timeout",
                    "${responseTime.inWholeMilliseconds}",
                ),
                envvars = getEnvVars(),
            )
        }
        assertEquals(expected = 0, actual = notStartedResult.statusCode)

        val startedResult = runWithTimeout(responseTime + deltaTime) {
            assertCommand.test(
                argv = listOf(
                    "--started",
                    "http://127.0.0.1:${SOLR_PORT}",
                    "--timeout",
                    "${responseTime.inWholeMilliseconds}",
                ),
                envvars = getEnvVars(),
            )
        }
        assertNotEquals(illegal = 0, actual = startedResult.statusCode)
    }

    /**
     * Get environment variables that contain a different temporary directory for SOLR_HOME for each
     * test.
     */
    private fun getEnvVars() = mapOf("SOLR_HOME" to tempFolder.absolutePath)
}
