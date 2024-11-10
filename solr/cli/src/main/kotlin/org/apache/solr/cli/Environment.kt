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

/**
 * Object that holds all environment variables.
 *
 * Note that some of the environment variables are scoped to the Solr instance that is being
 * interacted with and require a restart of that instance in order of changes to be applied.
 */
object Environment {

    private val env = System.getenv()

    /**
     * Solr hostname that is used by the CLI. This value is usually set in the environment
     * variables to override the hostname for production environments and control the hostname
     * that is exposed to cluster state.
     *
     * Environment variable: SOLR_HOST
     */
    val SOLR_HOST = env.getOrDefault("SOLR_HOST", Constants.DEFAULT_SOLR_HOST)

    /**
     * This controls the number of seconds that the solr script will wait for Solr to stop
     * gracefully. If the graceful stop fails, the script will forcibly stop Solr.
     *
     * Environment variable: SOLR_STOP_WAIT
     */
    val SOLR_STOP_WAIT = env.getOrDefault(
        "SOLR_STOP_WAIT",
        Constants.DEFAULT_SOLR_STOP_WAITMS.toString(),
    ).toInt()

    /**
     * This controls the number of seconds that the solr script will wait for Solr to start.
     * If the start fails, the script will give up waiting and display the last few lines
     * of the logfile.
     *
     * Environment variable: SOLR_START_WAIT
     */
    val SOLR_START_WAIT = env.getOrDefault(
        "SOLR_START_WAIT",
        Constants.DEFAULT_SOLR_START_WAIT.toString(),
    ).toInt()

    /**
     * The port Solr binds to.
     *
     * Environment variable: SOLR_PORT
     */
    val SOLR_PORT = env.getOrDefault(
        "SOLR_PORT",
        Constants.DEFAULT_SOLR_PORT.toString(),
    ).toInt()

    /**
     * The ZooKeeper connection string if using an external ZooKeeper ensemble
     * e.g., host1:2181,host2:2181/chroot
     *
     * Leave empty if using user-managed mode.
     *
     * Environment variable: ZK_HOST
     */
    val ZK_HOST: String = env.getOrDefault("ZK_HOST", Constants.DEFAULT_ZK_HOST)

    /**
     * Whether to automatically create a chroot path if Zookeeper host does not have one.
     *
     * Environment variable: ZK_CREATE_CHROOT
     */
    val ZK_CREATE_CHROOT = env.getOrDefault(
        "ZK_CREATE_CHROOT",
        Constants.DEFAULT_ZK_CREATE_CHROOT.toString(),
    ).toBoolean()

    /**
     * The ZooKeeper client timeout, in case of Solr cloud mode.
     *
     * Scope: Start command
     *
     * Environment variable: ZK_CLIENT_TIMEOUT
     */
    val ZK_CLIENT_TIMEOUT = env.getOrDefault(
        "ZK_CLIENT_TIMEOUT",
        Constants.DEFAULT_ZK_CLIENT_TIMEOUT.toString(),
    ).toInt()

    /**
     * Connection time in seconds for Solr to establish a connection Zookeeper before running
     * into a timeout.
     *
     * Scope: Start command
     *
     * Environment variable: SOLR_WAIT_FOR_ZK
     */
    val SOLR_WAIT_FOR_ZK = env.getOrDefault(
        "SOLR_WAIT_FOR_ZK",
        Constants.DEFAULT_SOLR_WAIT_FOR_ZK.toString(),
    ).toInt()

    /**
     * Whether Solr should delete cores that are not registered in Zookeeper at startup.
     *
     * By default Solr will log a warning for cores that are not registered in Zookeeper at startup
     * but otherwise ignore them. This protects against misconfiguration (e.g., connecting to the
     * wrong Zookeeper instance or chroot). However, you need to manually delete the cores if
     * they are no longer required. Set to `true` to have Solr automatically delete unknown cores.
     *
     * Scope: Start command
     *
     * Environment variable: SOLR_DELETE_UNKNOWN_CORES
     */
    val SOLR_DELETE_UNKNOWN_CORES = env.getOrDefault(
        "SOLR_DELETE_UNKNOWN_CORES",
        Constants.DEFAULT_SOLR_DELETE_UNKNOWN_CORES.toString(),
    ).toBoolean()

    /**
     * Timezone to use during CLI execution.
     *
     * Environment variable: SOLR_TIMEZONE
     */
    val SOLR_TIMEZONE = env.getOrDefault("SOLR_TIMEZONE", Constants.DEFAULT_SOLR_TIMEZONE)

    /**
     * Whether to activate the JMX RMI connector to allow remote JMX client applications
     * to monitor the JVM hosting Solr; if `false` it disables this behavior.
     *
     * `false` is recommended in production environments.
     *
     * Scope: Start command
     *
     * Environment variable: ENABLE_REMOTE_JMX_OPTS
     */
    val ENABLE_REMOTE_JMX_OPTS = env.getOrDefault(
        "ENABLE_REMOTE_JMX_OPTS",
        Constants.DEFAULT_ENABLE_REMOTE_JMX_OPTS.toString(),
    ).toBoolean()

    /**
     * The RMI_PORT to use for the JMX RMI connector.
     *
     * Scope: Start command
     *
     * Environment variable: RMI_PORT
     */
    val RMI_PORT = env.getOrDefault("RMI_PORT", Constants.DEFAULT_RMI_PORT.toString()).toInt()

    /**
     * Solr start options that are included as-is. The value is concatenated by the
     * --jvm-opts value of the start command if both provided.
     *
     * TODO See which takes precedence.
     *
     * Scope: Start command
     *
     * Environment variable: SOLR_OPTS
     *
     * Examples are:
     * SOLR_OPTS="$SOLR_OPTS -Dsolr.autoSoftCommit.maxTime=3000"
     * SOLR_OPTS="$SOLR_OPTS -Dsolr.autoCommit.maxTime=60000"
     */
    val SOLR_OPTS = env.getOrDefault("SOLR_OPTS", Constants.DEFAULT_SOLR_OPTS)

    /**
     * Whether to enable the Solr clustering component on start.
     *
     * Scope: Start command
     *
     * Environment variable: SOLR_CLUSTERING_ENABLED
     */
    val SOLR_CLUSTERING_ENABLED = env.getOrDefault(
        "SOLR_CLUSTERING_ENABLED",
        Constants.DEFAULT_SOLR_CLUSTERING_ENABLED.toString(),
    ).toBoolean()

    /**
     * Location where the bin/solr script will save PID files for running instances.
     *
     * Directory where PID files should be created.
     */
    val SOLR_PID_DIR = env.getOrDefault("SOLR_PID_DIR", Constants.DEFAULT_SOLR_PID_DIR)

    /**
     * Path to the Solr home directory. This is the directory where Solr will store cores
     * and their data.
     *
     * By default, Solr will use server/solr. If solr.xml is not stored in Zookeeper,
     * this directory must contain solr.xml.
     */
    val SOLR_HOME = env.getOrDefault("SOLR_HOME", Constants.DEFAULT_SOLR_HOME)

    /**
     * Path to a directory that Solr will use as root for data folders for each core.
     *
     * If not set, defaults to <instance_dir>/data. Overridable per core through 'dataDir'
     * core property.
     * TODO See if this needs to be generated or be a function instead
     */
    val SOLR_DATA_HOME = env.getOrDefault("SOLR_DATA_HOME", Constants.DEFAULT_SOLR_DATA_HOME)

    /**
     * Path to the log4j XML configuration file.
     *
     * May be overridden if you want to customize the log settings and file appender location
     * by providing a different log4j2.xml file.
     *
     * Scope: Start command
     */
    val LOG4J_PROPS = env.getOrDefault("LOG4J_PROPS", Constants.DEFAULT_LOG4J_PROPS)

    /**
     * Log level to use in Solr.
     *
     * Valid values are: ALL, TRACE, DEBUG, INFO, WARN, ERROR, FATAL, OFF.
     *
     * This is an alternative way to configure the rootLogger in log4j.xml
     *
     * Scope: Start command
     *
     * Environment variable: SOLR_LOG_LEVEL
     */
    val SOLR_LOG_LEVEL = env.getOrDefault("SOLR_LOG_LEVEL", Constants.DEFAULT_SOLR_LOG_LEVEL)

    /**
     * Location where Solr should write logs to. Absolute or relative to Solr home directory.
     *
     * Environment variable: SOLR_LOGS_DIR
     */
    val SOLR_LOGS_DIR = env.getOrDefault("SOLR_LOGS_DIR", Constants.DEFAULT_SOLR_LOGS_DIR)

    /**
     * Whether to enable jetty request log for all requests.
     *
     * Environment variable: SOLR_REQUESTLOG_ENABLED
     */
    val SOLR_REQUESTLOG_ENABLED = env.getOrDefault(
        "SOLR_REQUESTLOG_ENABLED",
        Constants.DEFAULT_SOLR_REQUESTLOG_ENABLED.toString(),
    ).toBoolean()

    /**
     * Restrict access to Solr by IP address.
     *
     * Specify a comma-separated list of addresses or networks, for example:
     *
     * 127.0.0.1, 192.168.0.0/24, [::1], [2000:123:4:5::]/64
     *
     * Environment variable: SOLR_IP_ALLOWLIST
     */
    val SOLR_IP_ALLOWLIST =
        env.getOrDefault("SOLR_IP_ALLOWLIST", Constants.DEFAULT_SOLR_IP_ALLOWLIST)

    /**
     * Block access to Solr from specific IP addresses.
     *
     * Specify a comma-separated list of addresses or networks, for example:
     *
     * 127.0.0.1, 192.168.0.0/24, [::1], [2000:123:4:5::]/64
     *
     * Environment variable: SOLR_IP_DENYLIST
     */
    val SOLR_IP_DENYLIST =
        env.getOrDefault("SOLR_IP_DENYLIST", Constants.DEFAULT_SOLR_IP_DENYLIST)

    /**
     * The network interface the Solr binds to. To prevent administrators from accidentally
     * exposing Solr more widely than intended, this defaults to 127.0.0.1.
     * Administrators should think carefully about their deployment environment and set this value
     * as narrowly as required before going to production. In environments where security
     * is not a concern, 0.0.0.0 can be used to allow Solr to accept connections on all
     * network interfaces.
     *
     * Environment variable: SOLR_JETTY_HOST
     */
    val SOLR_JETTY_HOST =
        env.getOrDefault("SOLR_JETTY_HOST", Constants.DEFAULT_SOLR_JETTY_HOST)

    /**
     * The network interface the Embedded ZK binds to.
     *
     * Environment variable: SOLR_ZK_EMBEDDED_HOST
     */
    val SOLR_ZK_EMBEDDED_HOST =
        env.getOrDefault("SOLR_ZK_EMBEDDED_HOST", Constants.DEFAULT_SOLR_ZK_EMBEDDED_HOST)

    /**
     * Whether to enable HTTPS. It is implicitly `true` if you set [SOLR_SSL_KEY_STORE].
     * Use this config to enable https module with custom jetty configuration.
     *
     * Environment variable: SOLR_SSL_ENABLED
     */
    val SOLR_SSL_ENABLED = env.getOrDefault(
        "SOLR_SSL_ENABLED",
        Constants.DEFAULT_SOLR_SSL_ENABLED.toString(),
    ).toBoolean()

    /**
     * The path to the SSL keystore.
     *
     * Environment variable: SOLR_SSL_KEY_STORE
     *
     * Example: etc/solr-ssl.keystore.p12
     */
    val SOLR_SSL_KEY_STORE =
        env.getOrDefault("SOLR_SSL_KEY_STORE", Constants.DEFAULT_SOLR_SSL_KEY_STORE)

    /**
     * The SSL keystore password.
     *
     * Environment variable: SOLR_SSL_KEY_STORE_PASSWORD
     *
     * Example: secret
     */
    val SOLR_SSL_KEY_STORE_PASSWORD = env.getOrDefault(
        "SOLR_SSL_KEY_STORE_PASSWORD",
        Constants.DEFAULT_SOLR_SSL_KEY_STORE_PASSWORD,
    )

    /**
     * The path to the SSL truststore.
     *
     * Environment variable: SOLR_SSL_TRUST_STORE
     */
    val SOLR_SSL_TRUST_STORE =
        env.getOrDefault("SOLR_SSL_TRUST_STORE", Constants.DEFAULT_SOLR_SSL_TRUST_STORE)

    /**
     * The SSL truststore password.
     *
     * Environment variable: SOLR_SSL_TRUST_STORE_PASSWORD
     *
     * Example: secret
     */
    val SOLR_SSL_TRUST_STORE_PASSWORD = env.getOrDefault(
        "SOLR_SSL_TRUST_STORE_PASSWORD",
        Constants.DEFAULT_SOLR_SSL_TRUST_STORE_PASSWORD,
    )

    /**
     * Whether clients are required to authenticate.
     *
     * Environment variable: SOLR_SSL_NEED_CLIENT_AUTH
     */
    val SOLR_SSL_NEED_CLIENT_AUTH = env.getOrDefault(
        "SOLR_SSL_NEED_CLIENT_AUTH",
        Constants.DEFAULT_SOLR_SSL_NEED_CLIENT_AUTH.toString(),
    ).toBoolean()

    /**
     * Whether clients can authenticate (but not require).
     *
     * Environment variable: SOLR_SSL_WANT_CLIENT_AUTH
     */
    val SOLR_SSL_WANT_CLIENT_AUTH = env.getOrDefault(
        "SOLR_SSL_WANT_CLIENT_AUTH",
        Constants.DEFAULT_SOLR_SSL_WANT_CLIENT_AUTH.toString(),
    ).toBoolean()

    /**
     * Whether to verify client's hostname during SSL handshake
     */
    val SOLR_SSL_CLIENT_HOSTNAME_VERIFICATION = env.getOrDefault(
        "SOLR_SSL_CLIENT_HOSTNAME_VERIFICATION",
        Constants.DEFAULT_SOLR_SSL_CLIENT_HOSTNAME_VERIFICATION.toString(),
    ).toBoolean()

    /**
     * Whether to enable peer name validation.
     *
     * SSL Certificates contain host/ip "peer name" information that is validated by default.
     *
     * Setting this to `false` can be useful to disable these checks when re-using a certificate
     * on many hosts. This will also be used for the default value of whether SNI Host checking
     * should be enabled.
     *
     * Environment variable: SOLR_SSL_CHECK_PEER_NAME
     */
    val SOLR_SSL_CHECK_PEER_NAME = env.getOrDefault(
        "SOLR_SSL_CHECK_PEER_NAME",
        Constants.DEFAULT_SOLR_SSL_CHECK_PEER_NAME.toString(),
    ).toBoolean()

    /**
     * Solr keystore type.
     */
    val SOLR_SSL_KEY_STORE_TYPE =
        env.getOrDefault("SOLR_SSL_KEY_STORE_TYPE", Constants.DEFAULT_SOLR_SSL_KEY_STORE_TYPE)

    /**
     * Solr SSL truststore type.
     */
    val SOLR_SSL_TRUST_STORE_TYPE =
        env.getOrDefault("SOLR_SSL_TRUST_STORE_TYPE", Constants.DEFAULT_SOLR_SSL_TRUST_STORE_TYPE)

    /**
     * Whether to enable SSL reload.
     */
    val SOLR_SSL_RELOAD_ENABLED =
        env.getOrDefault("SOLR_SSL_RELOAD_ENABLED", Constants.DEFAULT_SOLR_SSL_RELOAD_ENABLED)

    /**
     * Path to the SSL client keystore. Overrides [SOLR_SSL_KEY_STORE] for HTTP clients.
     */
    val SOLR_SSL_CLIENT_KEY_STORE =
        env.getOrDefault("SOLR_SSL_CLIENT_KEY_STORE", SOLR_SSL_KEY_STORE)

    /**
     * Password of the SSL client keystore. Overrides [SOLR_SSL_KEY_STORE_PASSWORD]
     * for HTTP clients.
     */
    val SOLR_SSL_CLIENT_KEY_STORE_PASSWORD =
        env.getOrDefault("SOLR_SSL_CLIENT_KEY_STORE_PASSWORD", SOLR_SSL_KEY_STORE_PASSWORD)

    /**
     * Path to the SSL client truststore. Overrides [SOLR_SSL_TRUST_STORE] for HTTP clients.
     */
    val SOLR_SSL_CLIENT_TRUST_STORE =
        env.getOrDefault("SOLR_SSL_CLIENT_TRUST_STORE", SOLR_SSL_TRUST_STORE)

    /**
     * Password of the SSL client truststore. Overrides [SOLR_SSL_TRUST_STORE_PASSWORD]
     * for HTTP clients.
     */
    val SOLR_SSL_CLIENT_TRUST_STORE_PASSWORD =
        env.getOrDefault("SOLR_SSL_CLIENT_TRUST_STORE_PASSWORD", SOLR_SSL_TRUST_STORE_PASSWORD)

    /**
     * The SSL keystore type used for HTTP clients. Overrides [SOLR_SSL_KEY_STORE_TYPE].
     */
    val SOLR_SSL_CLIENT_KEY_STORE_TYPE =
        env.getOrDefault("SOLR_SSL_CLIENT_KEY_STORE_TYPE", SOLR_SSL_KEY_STORE_TYPE)

    /**
     * The SSL truststore type used for HTTP clients. Overrides [SOLR_SSL_TRUST_STORE_TYPE].
     */
    val SOLR_SSL_CLIENT_TRUST_STORE_TYPE =
        env.getOrDefault("SOLR_SSL_CLIENT_TRUST_STORE_TYPE", SOLR_SSL_TRUST_STORE_TYPE)

    /*

    # Sets path of Hadoop credential provider (hadoop.security.credential.provider.path property) and
    # enables usage of credential store.
    # Credential provider should store the following keys:
    # * solr.jetty.keystore.password
    # * solr.jetty.truststore.password
    # Set the two below if you want to set specific store passwords for HTTP client
    # * javax.net.ssl.keyStorePassword
    # * javax.net.ssl.trustStorePassword
    # More info: https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-common/CredentialProviderAPI.html
    #SOLR_HADOOP_CREDENTIAL_PROVIDER_PATH=localjceks://file/home/solr/hadoop-credential-provider.jceks
    #SOLR_OPTS=" -Dsolr.ssl.credential.provider.chain=hadoop"

    # Settings for authentication
    # Please configure only one of SOLR_AUTHENTICATION_CLIENT_BUILDER or SOLR_AUTH_TYPE parameters
    #SOLR_AUTHENTICATION_CLIENT_BUILDER="org.apache.solr.client.solrj.impl.PreemptiveBasicAuthClientBuilderFactory"
    #SOLR_AUTH_TYPE="basic"
    #SOLR_AUTHENTICATION_OPTS="-Dbasicauth=solr:SolrRocks"

    # Settings for ZK ACL
    #SOLR_ZK_CREDS_AND_ACLS="-DzkACLProvider=org.apache.solr.common.cloud.DigestZkACLProvider \
    #  -DzkCredentialsProvider=org.apache.solr.common.cloud.DigestZkCredentialsProvider \
    #  -DzkCredentialsInjector=org.apache.solr.common.cloud.VMParamsZkCredentialsInjector \
    #  -DzkDigestUsername=admin-user -DzkDigestPassword=CHANGEME-ADMIN-PASSWORD \
    #  -DzkDigestReadonlyUsername=readonly-user -DzkDigestReadonlyPassword=CHANGEME-READONLY-PASSWORD"
    #SOLR_OPTS="$SOLR_OPTS $SOLR_ZK_CREDS_AND_ACLS"

    # optionally, you can use a Java properties file by using 'zkDigestCredentialsFile'
    #...
    #   -DzkDigestCredentialsFile=/path/to/zkDigestCredentialsFile.properties
    #...

    # Use a custom injector to inject ZK credentials into DigestZkACLProvider
    # -DzkCredentialsInjector expects a class implementing org.apache.solr.common.cloud.ZkCredentialsInjector
    # ...
    #   -DzkCredentialsInjector=fully.qualified.class.CustomInjectorClassName"
    # ...

    # Jetty GZIP module enabled by default
    #SOLR_GZIP_ENABLED=true

    # Settings for common system values that may cause operational imparement when system defaults are used.
    # Solr can use many processes and many file handles. On modern operating systems the savings by leaving
    # these settings low is minuscule, while the consequence can be Solr instability. To turn these checks off, set
    # SOLR_ULIMIT_CHECKS=false either here or as part of your profile.

    # Different limits can be set in solr.in.sh or your profile if you prefer as well.
    #SOLR_RECOMMENDED_OPEN_FILES=
    #SOLR_RECOMMENDED_MAX_PROCESSES=
    #SOLR_ULIMIT_CHECKS=

    # When running Solr in non-cloud mode and if planning to do distributed search (using the "shards" parameter), the
    # list of hosts needs to be defined in an allow-list or Solr will forbid the request. The allow-list can be configured
    # in solr.xml, or if you are using the OOTB solr.xml, can be specified using the system property "solr.allowUrls".
    # Alternatively host checking can be disabled by using the system property "solr.disable.allowUrls"
    #SOLR_OPTS="$SOLR_OPTS -Dsolr.allowUrls=http://localhost:8983,http://localhost:8984"

    # For a visual indication in the Admin UI of what type of environment this cluster is, configure
    # a -Dsolr.environment property below. Valid values are prod, stage, test, dev, with an optional
    # label or color, e.g. -Dsolr.environment=test,label=Functional+test,color=brown
    #SOLR_OPTS="$SOLR_OPTS -Dsolr.environment=prod"

    # Specifies the path to a common library directory that will be shared across all cores.
    # Any JAR files in this directory will be added to the search path for Solr plugins.
    # If the specified path is not absolute, it will be relative to `$SOLR_HOME`.
    #SOLR_OPTS="$SOLR_OPTS -Dsolr.sharedLib=/path/to/lib"

    # Runs solr in java security manager sandbox. This can protect against some attacks.
    # Runtime properties are passed to the security policy file (server/etc/security.policy)
    # You can also tweak via standard JDK files such as ~/.java.policy, see https://s.apache.org/java8policy
    # This is experimental! It may not work at all with Hadoop/HDFS features.
    #SOLR_SECURITY_MANAGER_ENABLED=true
    # This variable provides you with the option to disable the Admin UI. if you uncomment the variable below and
    # change the value to true. The option is configured as a system property as defined in SOLR_START_OPTS in the start
    # scripts.
    # SOLR_ADMIN_UI_DISABLED=false

    # Solr is by default allowed to read and write data from/to SOLR_HOME and a few other well defined locations
    # Sometimes it may be necessary to place a core or a backup on a different location or a different disk
    # This parameter lets you specify file system path(s) to explicitly allow. The special value of '*' will allow any path
    #SOLR_OPTS="$SOLR_OPTS -Dsolr.allowPaths=/mnt/bigdisk,/other/path"

    # Solr can attempt to take a heap dump on out of memory errors. To enable this, uncomment the line setting
    # SOLR_HEAP_DUMP below. Heap dumps will be saved to SOLR_LOG_DIR/dumps by default. Alternatively, you can specify any
    # other directory, which will implicitly enable heap dumping. Dump name pattern will be solr-[timestamp]-pid[###].hprof
    # When using this feature, it is recommended to have an external service monitoring the given dir.
    # If more fine grained control is required, you can manually add the appropriate flags to SOLR_OPTS
    # See https://docs.oracle.com/en/java/javase/11/troubleshoot/command-line-options1.html
    # You can test this behavior by setting SOLR_HEAP=25m
    #SOLR_HEAP_DUMP=true
    #SOLR_HEAP_DUMP_DIR=/var/log/dumps

    # Before version 9.0, Solr required a copy of solr.xml file in $SOLR_HOME. Now Solr will use a default file if not found.
    # To restore the old behavior, set the variable below to true
    #SOLR_SOLRXML_REQUIRED=false

    # Some previous versions of Solr use an outdated log4j dependency. If you are unable to use at least log4j version 2.15.0
    # then enable the following setting to address CVE-2021-44228
    # SOLR_OPTS="$SOLR_OPTS -Dlog4j2.formatMsgNoLookups=true"

    # The bundled plugins in the "modules" folder can easily be enabled as a comma-separated list in SOLR_MODULES variable
    # SOLR_MODULES=extraction,ltr

    # Configure the default replica placement plugin to use if one is not configured in cluster properties
    # See https://solr.apache.org/guide/solr/latest/configuration-guide/replica-placement-plugins.html for details
    #SOLR_PLACEMENTPLUGIN_DEFAULT=simple

    # Solr internally doesn't use cookies other than for modules such as Kerberos/Hadoop Auth. If you don't need any of those
    # And you don't need them for an external system (such as a load balancer), you can disable the use of a CookieStore with:
    # SOLR_OPTS="$SOLR_OPTS -Dsolr.http.disableCookies=true"

     */
    // TODO The following JVM options should be removed, since there is already
    //  jvm-options in StartCommand that should be used instead for configuring the variables manually.
    // - SOLR_HEAP
    // - SOLR_JAVA_MEM
    // - GC_LOG_OPTS
    // - GC_LOG_OPTS
    // - GC_TUNE

}
