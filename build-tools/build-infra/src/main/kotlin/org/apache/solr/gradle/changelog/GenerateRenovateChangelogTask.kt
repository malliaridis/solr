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

package org.apache.solr.gradle.changelog

import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteExisting
import kotlin.io.path.isDirectory
import kotlin.io.path.notExists
import kotlin.io.path.useDirectoryEntries
import kotlin.io.path.useLines
import kotlin.io.path.writeText
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml

abstract class GenerateRenovateChangelogTask : DefaultTask() {

    @get:Input
    @set:Option(option = "pr-number", description = "GitHub PR number")
    var prNumber: String = "0"

    @get:Input
    @set:Option(option = "pr-title", description = "GitHub PR title (from the Renovate bot)")
    var prTitle: String = ""

    @get:Input
    @set:Option(option = "changelog-dir", description = "Directory for changelog files (default: changelog/unreleased)")
    var changelogDirectory: String = Path(project.rootDir.absolutePath)
        .resolve("changelog")
        .resolve( "unreleased")
        .toAbsolutePath()
        .toString()

    @TaskAction
    fun run() {
        val prNumber = prNumber.toIntOrNull()
        requireNotNull(prNumber) { "pr-number must be a valid integer" }
        require(prNumber > 0) { "pr-number must be a positive integer" }
        require(prTitle.isNotBlank()) { "pr-title must not be blank" }
        require(prTitle.isNotBlank()) { "pr-title must not be blank" }

        // Delete any existing changelog files for this PR to ensure a clean slate
        // This prevents orphaned files when the PR title/slug changes
        deleteOldChangelogFiles(prNumber, changelogDirectory)

        // Parse the PR title
        val (changelogTitle, slug) = parsePrTitle(prTitle)

        // Generate filename
        val filename = "PR#$prNumber-$slug.yml"

        // Generate the new entry
        val entry = generateChangelogEntry(prNumber, changelogTitle)

        // Write the changelog file
        writeChangelogFile(filename, entry, changelogDirectory)
    }

    /**
     * Sanitize text to create a valid filename slug.
     *
     * - Convert to lowercase
     * - Replace dots, colons, slashes with dashes
     * - Replace other special chars with dashes
     * - Preserve word boundaries
     * - Truncate to [maxLength] while preserving word boundaries
     */
    private fun sanitizeSlug(text: String, maxLength: Int = 50): String {

        val sanitizedText = text
            // Convert to lowercase
            .lowercase()
            // Replace colons, slashes, dots with dashes
            .replace(regex = "[:/.]+".toRegex(), replacement = "-")
            // Replace other special characters with dashes
            .replace(regex = "[^a-z0-9\\s-]".toRegex(), replacement = "-")
            // Replace spaces with dashes
            .replace(regex = "\\s+".toRegex(), replacement = "-")
            // Replace multiple dashes with a single dash
            .replace(regex = "-+".toRegex(), replacement = "-")
            // Remove leading/trailing dashes
            .trim('-')

        return if (sanitizedText.length > maxLength) {
            sanitizedText
                // Truncate to max_length at the word boundary
                .substring(0, maxLength)
                // Find the last dash and truncate there
                .substringBeforeLast('-')
        } else sanitizedText
    }

    /**
     * Parse Renovate PR title to extract dependency name and version.
     *
     * Handles patterns like:
     * - "Update dependency org.junit.jupiter:junit-jupiter to v6"
     * - "Update dependency com.jayway.jsonpath:json-path to v2.10.0"
     * - "Update netty to v4.2.6.Final"
     * - "Update apache.kafka to v3.9.1"
     * - "Update actions/checkout action to v5"
     *
     * Note: The slug excludes the version number so the filename remains stable across version updates.
     *
     * @return Tuple of (title_for_changelog, dependency_slug_for_filename)
     */
    private fun parsePrTitle(title: String): Pair<String, String?> {
        // Pattern 1: "Update dependency {group}:{artifact} to {version}"
        val pattern1Matches = "Update dependency (.+?) to (.+?)(?:\\s*$|\\s*\\(|$)"
            .toRegex()
            .matchEntire(input = title)

        pattern1Matches?.let { match ->
            val dependencyName = match.groupValues[1]
            val version = match.groupValues[2].trim()
            val changelogTitle = "Update $dependencyName to $version"
            // Slug contains only the dependency name, not the version
            val slug = sanitizeSlug("Update $dependencyName")

            return changelogTitle to slug
        }

        // Pattern 2: "Update {owner}/{action} action to {version}"
        val pattern2Matches = "Update ([a-z0-9-]+/[a-z0-9-]+) action to (.+?)(?:\\s*$|\\s*\\(|$)"
            .toRegex()
            .matchEntire(input = title)

        pattern2Matches?.let { match ->
            val action = match.groupValues[1]
            val version = match.groupValues[2].trim()
            val changelogTitle = "Update $action action to $version"
            // Slug contains only the action name, not the version
            val slug = sanitizeSlug("Update $action action")

            return changelogTitle to slug
        }

        // Pattern 3: "Update {package} to {version}" (short form)
        val pattern3Matches = "Update ([a-z0-9\\-_.]+) to (.+?)(?:\\s*$|\\s*\\(|$)"
            .toRegex()
            .matchEntire(input = title)

        pattern3Matches?.let { match ->
            val packageString = match.groupValues[1]
            val version = match.groupValues[2].trim()
            val changelogTitle = "Update $packageString to $version"
            // Slug contains only the package name, not the version
            val slug = sanitizeSlug("Update $packageString")

            return changelogTitle to slug
        }

        // Fallback: use title as-is if no pattern matches
        return title to sanitizeSlug(title)
    }

    /**
     * Generate the changelog YAML entry dict.
     */
    private fun generateChangelogEntry(
        prNumber: Int,
        changelogTitle: String,
        prUrl: String? = null,
    ): Entry {
        val url = prUrl ?: "https://github.com/apache/solr/pull/$prNumber"

        return Entry(
            title = changelogTitle,
            type = "dependency_update",
            authors = listOf(Author(name = "solrbot")),
            links = listOf(Link(name = "PR#$prNumber", url = url)),
        )
    }

    /**
     * Find existing changelog file for this PR, returns path if exists.
     */
    private fun findExistingChangelogFile(
        prNumber: Int,
        changelogDirectory: String = "changelog/unreleased",
    ): String? {
        val pattern = "PR#$prNumber-*.yml"
        val path = Path(changelogDirectory)

        if (!path.isDirectory()) return null

        path.useDirectoryEntries(glob = pattern) { files ->
            return files.firstOrNull()?.toString()
        }
    }

    /**
     * Delete all existing changelog files for this PR number.
     *
     * This ensures we don't accumulate orphaned files when the PR title/slug changes.
     *
     * @return Number of files deleted
     */
    private fun deleteOldChangelogFiles(
        prNumber: Int,
        changelogDirectory: String = "changelog/unreleased",
    ): Int {
        val pattern = "PR#$prNumber-*.yml"
        val path = Path(changelogDirectory)

        if (!path.isDirectory()) return 0

        path.useDirectoryEntries(glob = pattern) { files ->
            return files.sumOf { file ->
                try {
                    file.deleteExisting()
                    1 as Int
                } catch (error: Exception) {
                    println("Warning: Could not delete file $file: $error")
                    0
                }
            }
        }
    }

    /**
     * Check if we need to update the changelog file.
     *
     * Updates if the title has changed (version was bumped).
     */
    private fun shouldUpdateChangelog(
        existingFile: String,
        newTitle: String,
    ): Boolean {
        if (existingFile.isEmpty() || Path(existingFile).notExists()) return false

        Path(existingFile).useLines { lines ->
            try {
                val entry: Entry = Yaml().load(lines.joinToString("\n"))
                return entry.title != newTitle
            } catch (error: Exception) {
                println("Warning: Could not read existing file $existingFile: $error")
                return false
            }
        }
    }

    /**
     * Write the changelog YAML file.
     */
    private fun writeChangelogFile(
        filename: String,
        entry: Entry,
        changelogDirectory: String = "changelog/unreleased",
    ) {
        val path = Path(changelogDirectory)
        path.createDirectories()

        val filePath = path.resolve(filename)

        // Use YAML dumper that preserves order and formatting
        val options = DumperOptions()
        options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK

        filePath.writeText(Yaml(options).dump(entry.toMap()))
        println(
            "Created/updated changelog file: ${filePath.toAbsolutePath()}"
        )
    }
}
