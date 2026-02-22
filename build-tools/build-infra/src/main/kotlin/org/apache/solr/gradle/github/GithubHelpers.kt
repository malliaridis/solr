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

import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import org.apache.solr.gradle.json
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException

/**
 * Helper function for retrieving the current branch name.
 */
internal fun DefaultTask.currentGitBranch(): String {
    val stdout = ByteArrayOutputStream()
    val result = project.exec {
        workingDir = project.rootDir
        commandLine("git", "rev-parse", "--abbrev-ref", "HEAD")
        standardOutput = stdout
        errorOutput = ByteArrayOutputStream()
        isIgnoreExitValue = true
    }
    if (result.exitValue != 0) {
        throw GradleException("Failed to detect current git branch. Pass --branch explicitly.")
    }
    return stdout.toString().trim()
}

/**
 * Fetches the information of an open pull request.
 *
 * @param repository The repository from which to fetch the pull request information.
 * @param branch The branch name to match.
 * @param forkOwner The fork owner to match.
 * @param githubTokenEnv The environment variable that holds the GitHub API token to use for
 * authentication, in case rate-limits apply.
 */
internal fun fetchOpenPullRequest(
    repository: String,
    branch: String,
    forkOwner: String,
    githubTokenEnv: String? = null,
): PullRequestInfo? {
    val (owner, name) = repository.split("/", limit = 2)
    val githubToken = System.getenv(githubTokenEnv).orEmpty().trim()

    val client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()

    fun request(uri: URI): HttpRequest.Builder =
        HttpRequest.newBuilder(uri)
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "gradle-fetch-pr-info-task")
            .apply {
                if (githubToken.isNotEmpty()) header("Authorization", "Bearer $githubToken")
            }

    val uri = URI("https://api.github.com/repos/$owner/$name/pulls?state=open&head=$forkOwner:$branch")
    val resp = client.send(request(uri).GET().build(), HttpResponse.BodyHandlers.ofString())

    if (resp.statusCode() !in 200..299) {
        throw GradleException(
            "GitHub API request failed (${resp.statusCode()}). " +
                "Set $githubTokenEnv in your environment to avoid rate limits. " +
                "Response: ${resp.body().take(500)}"
        )
    }

    val items = json.decodeFromString<Array<PullRequestItem>>(resp.body()).toList()

    // Match by branch name regardless of fork owner
    val match = items.firstOrNull { it.head.ref == branch }
    if (match != null) {
        return PullRequestInfo(
            repo = repository,
            branch = branch,
            number = match.number,
            title = match.title,
            url = match.htmlUrl
        )
    }

    return null
}
