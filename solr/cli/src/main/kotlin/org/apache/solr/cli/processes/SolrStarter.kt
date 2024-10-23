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

package org.apache.solr.cli.processes

import org.apache.solr.cli.enums.SolrMode

object SolrStarter {

    /**
     * Starts Solr based on the configuration provided.
     */
    suspend fun start(mode: SolrMode = SolrMode.Cloud) {
        TODO("Not yet implemented")

        /*
        "$JAVA" "${SOLR_START_OPTS[@]}" $SOLR_ADDL_ARGS -Dsolr.log.muteconsole \
        -jar start.jar "${SOLR_JETTY_CONFIG[@]}" $SOLR_JETTY_ADDL_CONFIG \
        1>"$SOLR_LOGS_DIR/solr-$SOLR_PORT-console.log" 2>&1 & echo $! > "$SOLR_PID_DIR/solr-$SOLR_PORT.pid"
         */
    }
}