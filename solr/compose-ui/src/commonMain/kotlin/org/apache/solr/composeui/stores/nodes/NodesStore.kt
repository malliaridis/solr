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

package org.apache.solr.composeui.stores.nodes

import com.arkivanov.mvikotlin.core.store.Store
import org.apache.solr.composeui.data.JavaProperty
import org.apache.solr.composeui.data.SystemData
import org.apache.solr.composeui.stores.nodes.NodesStore.Intent
import org.apache.solr.composeui.stores.nodes.NodesStore.State

/**
 * State store interface of the nodes API.
 *
 * Implementations of this state store manage information of all nodes.
 */
interface NodesStore : Store<Intent, State, Nothing> {

    /**
     * Intent for interacting with the node store.
     */
    sealed interface Intent {

        /**
         * Intent for requesting the system data.
         */
        data object FetchSystemData: Intent

        /**
         * Intent for requesting the system's version information.
         */
        data object FetchVersions: Intent

        /**
         * Intent for retrieving java properties.
         */
        data object FetchJavaProperties: Intent
    }

    /**
     * State class that holds the data of the [NodesStore].
     */
    data class State(
        val systemData: SystemData = SystemData(),
        val javaProperties: List<JavaProperty> = emptyList(),
    )
}
