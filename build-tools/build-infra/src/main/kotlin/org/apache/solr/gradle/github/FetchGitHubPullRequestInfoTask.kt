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

package org.apache.solr.gradle.github

import org.apache.solr.gradle.json
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

/**
 * This task fetches information about a GitHub pull request and stores them in a file.
 */
abstract class FetchGitHubPullRequestInfoTask : DefaultTask() {
    @get:Input
    @get:Optional
    @set:Option(option = "repo", description = "GitHub repository in the form owner/name (required)")
    var repo: String = ""

    @get:Input
    @get:Optional
    @set:Option(option = "fork", description = "GitHub fork owner of the PR (required)")
    var forkOwner: String = ""

    @get:Input
    @get:Optional
    @set:Option(option = "branch", description = "Branch name (optional). If omitted, uses current git branch.")
    var branch: String? = null

    @get:Input
    @get:Optional
    @set:Option(option = "github-token-env", description = "Env var name that contains GitHub token (default: GITHUB_TOKEN)")
    var githubTokenEnv: String = "GITHUB_TOKEN"

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    init {
        outputFile.convention(project.layout.buildDirectory.file("github/pr-info.json"))
    }

    override fun getDescription(): String = "Fetch pull request info from GitHub."

    @TaskAction
    fun run() {
        val repoValue = repo.trim()
        require(repoValue.isNotBlank()) { "Repo resolved to blank. Pass --repo explicitly." }
        require(repoValue.matches(Regex("^[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+$"))) {
            "--repo must be in the form owner/name, got: '$repoValue'"
        }
        val fork = forkOwner.trim()
        require(fork.isNotBlank()) { "Fork owner resolved to blank. Pass --fork explicitly." }

        val branchValue = (branch?.trim()?.trimMargin("refs/heads/").takeUnless { it.isNullOrBlank() } ?: currentGitBranch())
            .also { require(it.isNotBlank()) { "Branch resolved to blank. Pass --branch explicitly or ensure you're on a branch." } }

        val pr = fetchOpenPullRequest(
            repository = repoValue,
            branch = branchValue,
            forkOwner = fork,
            githubTokenEnv = githubTokenEnv,
        )

        if (pr == null) throw GradleException("No open PR found for $repo branch '$branch' (including forks).")

        outputFile.get().asFile.apply {
            parentFile.mkdirs()
            writeText(json.encodeToString(pr) + "\n")
        }

        logger.lifecycle("Found PR #${pr.number}: ${pr.title}")
        logger.lifecycle("Wrote: ${outputFile.get().asFile.absolutePath}")
    }
}
