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

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.http.isSuccess
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import org.apache.solr.cli.data.SystemData
import org.apache.solr.cli.data.SystemMode
import org.apache.solr.cli.domain.SolrMode
import org.apache.solr.cli.domain.SolrState
import org.apache.solr.cli.utils.Utils

internal object SolrStateAnalyzer {

    /**
     * Polls for the given [timeout] duration and at fetches at least once the [SolrState] before
     * returning.
     *
     * @param url URL of the Solr instance to check. Note that the URL may be extended to fetch
     * state-specific information, so no path suffix except the base path is allowed.
     * @param credentials Credentials to use for auth, if any.
     * @param timeout Timeout that interrupts the polling. A negative value will fetch the state
     * exactly once. Note that if a timeout occurs, one last fetch will be executed to support
     * "at least once" fetch behavior.
     * @param interval The interval to wait between polls.
     * @return A [SolrState] based on the response received / not received.
     */
    suspend fun getSolrState(
        url: String,
        credentials: String? = null,
        timeout: Duration = (-1).milliseconds,
        interval: Duration = 1.seconds,
    ): SolrState = Utils.getHttpClient(credentials).use { client ->
        val infoUrl = URLBuilder(url).apply {
            appendPathSegments("admin", "info", "system")
        }.build()

        try {
            withTimeoutOrNull(timeout) {
                while (true) {
                    val response = client.get(infoUrl)
                    when {
                        response.status.isSuccess() -> {
                            val data = response.body<SystemData>()
                            return@withTimeoutOrNull SolrState.Online(data.mode.toSolrMode())
                        }
                        response.status.isAuthError() ->
                            return@withTimeoutOrNull SolrState.AuthRequired
                    }

                    delay(interval)
                }
                @Suppress("UNREACHABLE_CODE")
                throw Error("Should not happen")
            } ?: run {
                // Run at least once, or one more time when timeout occurs
                val response = client.get(infoUrl)
                when {
                    response.status.isSuccess() -> {
                        val data = response.body<SystemData>()
                        SolrState.Online(data.mode.toSolrMode())
                    }
                    response.status.isAuthError() -> SolrState.AuthRequired
                    else -> SolrState.Offline
                }
            }
        } catch (exception: TimeoutCancellationException) {
            SolrState.Offline // Timeout occurred, server didn't come online
        }
    }

    private fun HttpStatusCode.isAuthError(): Boolean =
        this == HttpStatusCode.Unauthorized || this == HttpStatusCode.Forbidden

    private fun SystemMode.toSolrMode(): SolrMode = when(this) {
        SystemMode.Unknown -> SolrMode.Unknown
        SystemMode.SolrCloud -> SolrMode.Cloud
        SystemMode.Standalone -> SolrMode.UserManaged
    }
}
