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

package org.apache.solr.composeui.components.environment.integration

import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import io.ktor.client.HttpClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.apache.solr.composeui.components.environment.EnvironmentComponent
import org.apache.solr.composeui.components.environment.store.EnvironmentStoreProvider
import org.apache.solr.composeui.utils.AppComponentContext
import org.apache.solr.composeui.utils.coroutineScope
import org.apache.solr.composeui.utils.map

/**
 * Default implementation of the [EnvironmentComponent].
 */
class DefaultEnvironmentComponent(
    componentContext: AppComponentContext,
    storeFactory: StoreFactory,
    httpClient: HttpClient,
) : EnvironmentComponent, AppComponentContext by componentContext {

    private val mainScope = coroutineScope(mainContext)

    private val store = instanceKeeper.getStore {
        EnvironmentStoreProvider(
            storeFactory = storeFactory,
            client = HttpEnvironmentStoreClient(httpClient),
            ioContext = ioContext,
        ).provide()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model = store.stateFlow.map(mainScope, environmentStateToModel)
}