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

package org.apache.solr.cli.services

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class CommandCheckerTest {

    @Test
    fun invalidCommandNotExists() = runTest {
        val result = CommandChecker.commandExists("somerandomcommandthatdoesnotexist")

        assertTrue(result.isSuccess)
        assertFalse(result.getOrThrow())
    }

    @Test
    fun checkingEmptyCommandFails() = runTest {
        val result = CommandChecker.commandExists("")

        assertTrue(result.isFailure)
        assertIs<IllegalArgumentException>(result.exceptionOrNull())
    }

    @Test
    fun validCommandNotExists() = runTest {
        val result = CommandChecker.commandExists("java")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow())
    }

    @Test
    fun validCommandReturnsPath() = runTest {
        val result = CommandChecker.getCommandPath("java")

        assertTrue(result.isSuccess)
        val pathString = result.getOrNull()
        assertNotNull(pathString)
        assertTrue(pathString.isNotBlank())
        assertTrue(File(pathString).exists())
    }

    @Test
    fun invalidCommandReturnsNull() = runTest {
        val result = CommandChecker.getCommandPath("somerandomcommandthatdoesnotexist")

        assertTrue(result.isSuccess)
        val pathString = result.getOrNull()
        assertNull(pathString)
    }

    @Test
    fun gettingPathForEmptyCommandFails() = runTest {
        val result = CommandChecker.getCommandPath("")

        assertTrue(result.isFailure)
        assertIs<IllegalArgumentException>(result.exceptionOrNull())
    }
}
