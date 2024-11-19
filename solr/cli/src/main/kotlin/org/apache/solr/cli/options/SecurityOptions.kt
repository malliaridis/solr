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

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.boolean
import com.github.ajalt.clikt.parameters.types.path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

internal class SecurityOptions(
    private val port: () -> Int,
    private val isSecurityManagerEnabled: () -> Boolean = { false },
) : OptionGroup() {

    val sslKeyStore by option(
        envvar = "SOLR_SSL_KEY_STORE",
        valueSourceKey = "solr.ssl.keyStore",
        hidden = true,
    ).path(mustExist = true) // TODO Is this a path or file?

    val sslKeyStorePassword by option(
        envvar = "SOLR_SSL_KEY_STORE_PASSWORD",
        valueSourceKey = "solr.ssl.keyStorePassword",
        hidden = true,
    )

    val sslKeyStoreType by option(
        envvar = "SOLR_SSL_KEY_STORE_TYPE",
        valueSourceKey = "solr.ssl.keyStoreType",
        hidden = true,
    )

    val sslCheckPeerName by option(
        envvar = "SOLR_SSL_CHECK_PEER_NAME",
        valueSourceKey = "solr.ssl.checkPeerName",
    ).boolean()

    val sslTrustStore by option(
        envvar = "SOLR_SSL_TRUST_STORE",
        valueSourceKey = "solr.ssl.trustStore",
        hidden = true,
    ).path(mustExist = true) // TODO Is this a path or file?

    val sslTrustStorePassword by option(
        envvar = "SOLR_SSL_TRUST_STORE_PASSWORD",
        valueSourceKey = "solr.ssl.trustStorePassword",
        hidden = true,
    )

    val sslTrustStoreType by option(
        envvar = "SOLR_SSL_TRUST_STORE_TYPE",
        valueSourceKey = "solr.ssl.trustStoreType",
        hidden = true,
    )

    val verifySslClientHostname by option(
        envvar = "SOLR_SSL_CLIENT_HOSTNAME_VERIFICATION",
        valueSourceKey = "solr.ssl.client.hostnameVerification",
        hidden = true,
    ).boolean()
        .default(true)

    val sslClientNeedAuth by option(
        envvar = "SOLR_SSL_CLIENT_NEED_AUTH",
        valueSourceKey = "solr.ssl.client.needAuth",
        hidden = true,
    ).boolean()

    val sslClientWantAuth by option(
        envvar = "SOLR_SSL_CLIENT_WANT_AUTH",
        valueSourceKey = "solr.ssl.client.wantAuth",
        hidden = true,
    ).boolean()

    val sslClientKeyStore by option(
        envvar = "SOLR_SSL_CLIENT_KEY_STORE",
        valueSourceKey = "solr.ssl.client.keyStore",
        hidden = true,
    ).path(mustExist = true) // TODO Is this a path or file?

    val sslClientKeyStorePassword by option(
        envvar = "SOLR_SSL_CLIENT_KEY_STORE_PASSWORD",
        valueSourceKey = "solr.ssl.client.keyStorePassword",
        hidden = true,
    )

    val sslClientKeyStoreType by option(
        envvar = "SOLR_SSL_CLIENT_KEY_STORE_TYPE",
        valueSourceKey = "solr.ssl.client.keyStoreType",
        hidden = true,
    )

    val sslClientTrustStore by option(
        envvar = "SOLR_SSL_CLIENT_KTRUST_STORE",
        valueSourceKey = "solr.ssl.client.trustStore",
        hidden = true,
    ).path(mustExist = true) // TODO Is this a path or file?

    val sslClientTrustStorePassword by option(
        envvar = "SOLR_SSL_CLIENT_TRUST_STORE_PASSWORD",
        valueSourceKey = "solr.ssl.client.trustStorePassword",
        hidden = true,
    )

    val sslClientTrustStoreType by option(
        envvar = "SOLR_SSL_CLIENT_TRUST_STORE_TYPE",
        valueSourceKey = "solr.ssl.client.trustStoreType",
        hidden = true,
    )

    val isSslEnabled by option(
        "--ssl-enabled",
        envvar = "SOLR_SSL_ENABLED",
        valueSourceKey = "solr.ssl.enabled",
        hidden = true,
    ).flag()

    val isSslReload by option(
        "--ssl-reload",
        envvar = "SOLR_SSL_RELOAD",
        valueSourceKey = "solr.ssl.reload",
        hidden = true,
    ).flag("--disable-ssl-reload", default = true)

    fun composeSecurityArguments(): Array<String> {
        if (!isSslEnabled) return emptyArray()
        val arguments = mutableListOf<String>()
        arguments.add("-Dsolr.keyStoreReload.enabled=$isSslReload")
        sslKeyStore?.let {
            arguments.add("-Dsolr.jetty.keystore=${it.toAbsolutePath()}")
            if (isSslReload && isSecurityManagerEnabled()) {
                // In this case we need to allow reads from the parent directory of the keystore
                arguments.add("-Dsolr.jetty.keystoreParentPath=${it.parent.toAbsolutePath()}")
            }
        }
        // TODO Export keystore password if sslKeyStorePassword is set
        sslKeyStoreType?.let { arguments.add("-Dsolr.jetty.keystore.type=$it") }

        sslTrustStore?.let { arguments.add("-Dsolr.jetty.truststore=$it") }
        // TODO Export truststore password if sslTrustStorePassword is set
        sslTrustStoreType?.let { arguments.add("-Dsolr.jetty.truststore.type=$it") }
        if (verifySslClientHostname)
            arguments.add("-Dsolr.jetty.ssl.verifyClientHostName=HTTPS")

        sslClientNeedAuth?.let { arguments.add("-Dsolr.jetty.ssl.needClientAuth=$it") }
        sslClientWantAuth?.let { arguments.add("-Dsolr.jetty.ssl.wantClientAuth=$it") }

        sslClientKeyStore?.let {
            arguments.add("-Djavax.net.ssl.keyStore=${it.toAbsolutePath()}")
            // TODO Export SOLR_SSL_CLIENT_KEY_STORE_PASSWORD if sslClientKeyStorePassword is set
            sslClientKeyStoreType?.let { arguments.add("-Djavax.net.ssl.keyStoreType=$it") }

            if (isSslReload && isSecurityManagerEnabled()) {
                // In this case we need to allow reads from the parent directory of the keystore
                arguments.add("-Djavax.net.ssl.keyStoreParentPath=${it.toAbsolutePath()}")
            }
        } ?: run {
            sslKeyStore?.let { arguments.add("-Djavax.net.ssl.keyStore=${it.toAbsolutePath()}") }
            sslKeyStoreType?.let { arguments.add("-Djavax.net.ssl.keyStoreType=$it") }
            // TODO Is the security manager check here not relevant for parent directory?
        }
        sslCheckPeerName?.let {
            arguments.add("-Dsolr.ssl.checkPeerName=$sslCheckPeerName")
            arguments.add("-Dsolr.jetty.ssl.sniHostCheck=$sslCheckPeerName")
        }

        sslClientTrustStore?.let {
            arguments.add("-Djavax.net.ssl.trustStore=${it.toAbsolutePath()}")
            // TODO Export SOLR_SSL_CLIENT_TRUST_STORE_PASSWORD if sslClientTrustStorePassword is set
            sslClientTrustStoreType?.let { arguments.add("-Djavax.net.ssl.trustStoreType=$it") }

            // TODO Is the security manager check here not relevant for parent directory?
        } ?: run {
            sslTrustStore?.let { arguments.add("-Djavax.net.ssl.trustStore=${it.toAbsolutePath()}") }
            sslTrustStoreType?.let { arguments.add("-Djavax.net.ssl.trustStoreType=$it") }
            // TODO Is the security manager check here not relevant for parent directory?
        }

        // If using SSL and solr.jetty.https.port not set explicitly, use the jetty.port
        arguments.add("-Dsolr.jetty.https.port=$port")

        return arguments.toTypedArray()
    }
}
