package org.apache.solr.cli.processes

import com.github.ajalt.clikt.core.parse
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.apache.solr.cli.TestCliCommand
import org.apache.solr.cli.processes.UserLimitsChecker.getUserLimits

class UserLimitsCheckerTest {

    @Test
    fun simpleCheckUserLimitsSucceeds() = runTest {
        TestCliCommand.parse(emptyList())
        with(TestCliCommand) {
            val result = getUserLimits()
            assertTrue(result.isSuccess)
        }
    }

    @Test
    fun getUserLimits() {
    }
}
