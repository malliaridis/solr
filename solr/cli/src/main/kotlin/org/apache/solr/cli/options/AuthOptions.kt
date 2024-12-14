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

package org.apache.solr.cli.options

import com.github.ajalt.clikt.core.BaseCliktCommand
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import org.apache.solr.cli.EchoUtils.warn
import org.apache.solr.cli.domain.AuthType

internal class AuthOptions(
    private val cliCommand: BaseCliktCommand<*>,
) : OptionGroup(
    name = "Authentcication options",
    help = "Options controlling the authentication configuration when starting a Solr instance"
) {

    val authType by option(
        envvar = "SOLR_AUTH_TYPE",
        valueSourceKey = "solr.auth.type",
        hidden = true,
    ).enum<AuthType>()

    val authOptions by option(
        envvar = "SOLR_AUTHENTICATION_OPTS",
        valueSourceKey = "solr.auth.options",
        hidden = true,
    ).multiple()

    val authClientBuilder by option(
        envvar = "SOLR_AUTHENTICATION_CLIENT_BUILDER",
        valueSourceKey = "solr.auth.client.builder",
        hidden = true,
    )

    fun composeAuthArguments(suppressWarnings: Boolean = false): Array<String> {
        if(
            !suppressWarnings
            && this@AuthOptions.authType == null
            && this@AuthOptions.authOptions.isNotEmpty()
        ) cliCommand.warn(
            """SOLR_AUTHENTICATION_OPTS environment variable configured without
            | associated SOLR_AUTH_TYPE variable. Please configure SOLR_AUTH_TYPE environment
            | variable with the authentication type to be used. Currently supported authentication
            | types are [kerberos, basic]
            """.trimMargin()
        )

        if (
            !suppressWarnings
            && this@AuthOptions.authType != null
            && this@AuthOptions.authClientBuilder != null
        ) cliCommand.warn(
            """SOLR_AUTHENTICATION_CLIENT_BUILDER and SOLR_AUTH_TYPE environment
            | variables are configured together. Use SOLR_AUTH_TYPE environment variable to
            | configure authentication type to be used. Currently supported authentication types
            | are [kerberos, basic]. The value of SOLR_AUTHENTICATION_CLIENT_BUILDER environment
            | variable will be ignored.
            """.trimMargin()
        )

        val arguments = mutableListOf<String>()

        when (this@AuthOptions.authType) {
            AuthType.Basic -> "org.apache.solr.client.solrj.impl.PreemptiveBasicAuthClientBuilderFactory"
            AuthType.Kerberos -> "org.apache.solr.client.solrj.impl.Krb5HttpClientBuilder"
            null -> this@AuthOptions.authClientBuilder
        }?.let { arguments.add("-Dsolr.httpclient.builder.factory=$it") }

        arguments.addAll(this@AuthOptions.authOptions)

        return arguments.toTypedArray()
    }
}