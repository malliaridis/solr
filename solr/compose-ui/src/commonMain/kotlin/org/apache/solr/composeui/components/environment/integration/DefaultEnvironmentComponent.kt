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

import com.arkivanov.decompose.childContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import io.ktor.client.HttpClient
import org.apache.solr.composeui.components.environment.EnvironmentComponent
import org.apache.solr.composeui.utils.AppComponentContext

/**
 * Default implementation of the [EnvironmentComponent].
 */
class DefaultEnvironmentComponent(
    componentContext: AppComponentContext,
    storeFactory: StoreFactory,
    httpClient: HttpClient,
) : EnvironmentComponent, AppComponentContext by componentContext {

    override val javaProperties = DefaultJavaPropertiesComponent(
        componentContext = childContext("javaProperties"),
        storeFactory = storeFactory,
        httpClient = httpClient,
    )

    override val versions = DefaultVersionsComponent(
        componentContext = childContext("versions"),
        storeFactory = storeFactory,
        httpClient = httpClient,
    )

    override val commandLineArgs = DefaultCommandLineArgsComponent(
        componentContext = childContext("commandLineArgs"),
        storeFactory = storeFactory,
        httpClient = httpClient,
    )
}
