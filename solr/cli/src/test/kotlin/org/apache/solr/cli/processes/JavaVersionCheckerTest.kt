package org.apache.solr.cli.processes

import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.io.files.FileNotFoundException
import org.apache.solr.cli.processes.JavaVersionChecker.getJavaMajorVersion

class JavaVersionCheckerTest {

    /**
     * The current java version from the runtime. We can assume that tests are run on JDK 17+
     * and therefore the first value is always the actual version.
     */
    private val currentJavaVersion = System.getProperty("java.version")
        .split('.')
        .first()
        .toInt()

    @Test
    fun getDefaultJavaMajorVersionReturnsSameAsSystemProperty() = runTest {
        val result = getJavaMajorVersion()

        assertTrue(result.isSuccess)
        assertEquals(currentJavaVersion, result.getOrNull())
    }

    @Test
    fun getJavaMajorVersionFailsWithMessage() = runTest {
        // Note that the path separator may change the slashes depending on the OS
        val invalidPath = Path("invalid/path/to/java").toString()
        val result = getJavaMajorVersion(invalidPath)

        assertTrue(result.isFailure)
        val exception = assertIs<FileNotFoundException>(result.exceptionOrNull())
        val message = assertNotNull(exception.message)
        assertContains(
            charSequence = message,
            other = "Could not find java executable",
        )
        assertContains(
            charSequence = message,
            other = invalidPath,
        )
    }
}
