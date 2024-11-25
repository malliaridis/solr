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

package org.apache.solr.cli

import java.io.File
import java.nio.file.Paths

/**
 * Collection of constant values for Solr.
 */
object Constants {

    // TODO Consider removing this value
    private val SOLR_SCRIPT = Paths.get("").toAbsolutePath().toString()

    // TODO Consider removing this value
    private val SOLR_TIP = File(SOLR_SCRIPT).parent

    // TODO Consider removing this value
    private val DEFAULT_SOLR_SERVER_DIR = File(SOLR_TIP, "server").absolutePath

    /**
     * Default number of nodes to create when starting Solr in cloud mode.
     */
    const val DEFAULT_NODE_COUNT = 2

    /**
     * Default number of seconds that the solr script will wait for Solr to stop gracefully.
     *
     * @see Environment.SOLR_STOP_WAIT
     */
    const val DEFAULT_SOLR_STOP_WAITMS = 180000L

    /**
     * Default number of seconds that the solr script will wait for Solr to start.
     *
     * @see Environment.SOLR_START_WAIT
     */
    const val DEFAULT_SOLR_START_WAIT = 180

    const val DEFAULT_SOLR_SCHEME = "http://"

    /**
     * Default Solr hostname that is used as fallback.
     */
    const val DEFAULT_SOLR_HOST = "127.0.0.1"

    /**
     * Default Solr port.
     */
    const val DEFAULT_SOLR_PORT = 8983

    /**
     * Default Solr URL.
     */
    const val DEFAULT_SOLR_URL = "$DEFAULT_SOLR_SCHEME$DEFAULT_SOLR_HOST:$DEFAULT_SOLR_PORT"

    /**
     * Default cloud ports of a two-node-cluster.
     */
    val DEFAULT_CLOUD_PORTS = intArrayOf(8983, 7574, 8984, 7575)

    /**
     * Default Zookeeper connection string.
     *
     * @see Environment.ZK_HOST
     */
    const val DEFAULT_ZK_HOST = "127.0.0.1:9983"

    /**
     * Default value for whether to automatically create a chroot path if Zookeeper host
     * does not have one.
     *
     * @see Environment.ZK_CREATE_CHROOT
     */
    const val DEFAULT_ZK_CREATE_CHROOT = false

    /**
     * Default ZooKeeper client timeout in milliseconds, in case of Solr cloud mode.
     *
     * @see Environment.ZK_CLIENT_TIMEOUT
     */
    const val DEFAULT_ZK_CLIENT_TIMEOUT = 30_000

    /**
     * Default Zookeeper connection timeout in seconds.
     *
     * @see Environment.SOLR_WAIT_FOR_ZK
     */
    const val DEFAULT_SOLR_WAIT_FOR_ZK = 30

    /**
     * Default value for whether Solr should delete cores that are not registered
     * in Zookeeper at startup.
     *
     * @see Environment.SOLR_DELETE_UNKNOWN_CORES
     */
    const val DEFAULT_SOLR_DELETE_UNKNOWN_CORES = false

    /**
     * Default timezone used by the CLI.
     *
     * @see Environment.SOLR_TIMEZONE
     */
    const val DEFAULT_SOLR_TIMEZONE = "UTC"

    /**
     * Default value for whether to activate the JMX RMI connector to allow remote JMX client
     * applications to monitor the JVM hosting Solr.
     *
     * @see Environment.ENABLE_REMOTE_JMX_OPTS
     */
    const val DEFAULT_ENABLE_REMOTE_JMX_OPTS = false

    /**
     * Default RMI_PORT to use for the JMX RMI connector.
     *
     * Defaults to [DEFAULT_SOLR_PORT] + 10.000
     */
    const val DEFAULT_RMI_PORT = DEFAULT_SOLR_PORT + 10000

    /**
     * Default Solr start options that are included as-is.
     */
    const val DEFAULT_SOLR_OPTS = ""

    /**
     * TODO Add documentation
     */
    const val DEFAULT_SOLR_CLUSTERING_ENABLED = false

    /**
     * TODO Add documentation
     */
    val DEFAULT_SOLR_PID_DIR = File(SOLR_TIP, "bin").absolutePath

    // TODO Consider removing this value
    val DEFAULT_SOLR_HOME = File(DEFAULT_SOLR_SERVER_DIR, "solr").absolutePath

    // TODO See if this is the correct path
    // TODO Consider removing this value
    val DEFAULT_SOLR_DATA_HOME = File(DEFAULT_SOLR_SERVER_DIR, "data").absolutePath

    /**
     * Default path to the log4j XML configuration file.
     *
     * @see Environment.LOG4J_PROPS
     */
    val DEFAULT_LOG4J_PROPS = File(DEFAULT_SOLR_SERVER_DIR, "resources")

    /**
     * Default log level to use in Solr.
     *
     * @see Environment.SOLR_LOG_LEVEL
     */
    const val DEFAULT_SOLR_LOG_LEVEL = "INFO"

    /**
     * TODO Consider removing this value
     *
     * @see Environment.SOLR_LOGS_DIR
     */
    val DEFAULT_SOLR_LOGS_DIR = File(DEFAULT_SOLR_SERVER_DIR, "logs").absolutePath

    /**
     * Default value for whether to enable jetty request log for all requests.
     *
     * @see Environment.SOLR_REQUESTLOG_ENABLED
     */
    const val DEFAULT_SOLR_REQUESTLOG_ENABLED = false

    /**
     * Default Solr IP allowlist for restricting access to Solr.
     *
     * @see Environment.SOLR_IP_ALLOWLIST
     */
    const val DEFAULT_SOLR_IP_ALLOWLIST = ""

    /**
     * Default Solr IP denylist for blocking access to Solr.
     */
    const val DEFAULT_SOLR_IP_DENYLIST = ""

    /**
     * The default network interface the Solr binds to.
     *
     * @see Environment.SOLR_JETTY_HOST
     */
    const val DEFAULT_SOLR_JETTY_HOST = "127.0.0.1"


    /**
     * The default network interface the Embedded ZK binds to.
     *
     * @see Environment.SOLR_ZK_EMBEDDED_HOST
     */
    const val DEFAULT_SOLR_ZK_EMBEDDED_HOST = "127.0.0.1"

    /**
     * Whether to enable HTTPS.
     *
     * @see Environment.SOLR_SSL_ENABLED
     */
    const val DEFAULT_SOLR_SSL_ENABLED = false

    /**
     * The default path to the SSL keystore.
     *
     * @see Environment.SOLR_SSL_KEY_STORE
     */
    const val DEFAULT_SOLR_SSL_KEY_STORE = ""

    /**
     * The default SSL keystore password.
     *
     * @see Environment.SOLR_SSL_KEY_STORE_PASSWORD
     */
    const val DEFAULT_SOLR_SSL_KEY_STORE_PASSWORD = ""

    /**
     * The default path to the SSL truststore.
     *
     * @see Environment.SOLR_SSL_TRUST_STORE
     */
    const val DEFAULT_SOLR_SSL_TRUST_STORE = ""

    /**
     * The default SSL truststore password.
     *
     * @see Environment.SOLR_SSL_TRUST_STORE_PASSWORD
     */
    const val DEFAULT_SOLR_SSL_TRUST_STORE_PASSWORD = ""

    /**
     * Default value for whether clients are required to authenticate.
     *
     * @see Environment.SOLR_SSL_NEED_CLIENT_AUTH
     */
    const val DEFAULT_SOLR_SSL_NEED_CLIENT_AUTH = false

    /**
     * Default value for whether clients can authenticate.
     *
     * @see Environment.SOLR_SSL_WANT_CLIENT_AUTH
     */
    const val DEFAULT_SOLR_SSL_WANT_CLIENT_AUTH = false

    /**
     * Default value for whether to verify client's hostname during SSL handshake.
     *
     * @see Environment.SOLR_SSL_CLIENT_HOSTNAME_VERIFICATION
     */
    const val DEFAULT_SOLR_SSL_CLIENT_HOSTNAME_VERIFICATION = false

    /**
     * Whether to enable peer name validation.
     *
     * @see Environment.SOLR_SSL_CHECK_PEER_NAME
     */
    const val DEFAULT_SOLR_SSL_CHECK_PEER_NAME = false

    /**
     * Default Solr keystore type.
     */
    const val DEFAULT_SOLR_SSL_KEY_STORE_TYPE = "PKCS12"

    /**
     * Default Solr SSL truststore type.
     */
    const val DEFAULT_SOLR_SSL_TRUST_STORE_TYPE = "PKCS12"

    /**
     * Default value for whether to enable SSL reload.
     */
    const val DEFAULT_SOLR_SSL_RELOAD_ENABLED = true
}
