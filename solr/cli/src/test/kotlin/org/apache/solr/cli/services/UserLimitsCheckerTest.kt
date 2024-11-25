package org.apache.solr.cli.services

import com.github.ajalt.clikt.core.parse
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.apache.solr.cli.TestCliCommand
import org.apache.solr.cli.services.UserLimitsChecker.getUserLimits
import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.condition.OS

class UserLimitsCheckerTest {

    @Test
    @DisabledOnOs(OS.WINDOWS, disabledReason = "ulimit not applicable on Windows OS.")
    fun simpleCheckUserLimitsSucceeds() = runTest {
        TestCliCommand.parse(emptyList())
        with(TestCliCommand) {
            val result = getUserLimits()
            assertTrue(result.isSuccess)
        }
    }
}
