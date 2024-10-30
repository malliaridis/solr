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

import org.apache.solr.composeui.components.environment.CommandLineArgsComponent
import org.apache.solr.composeui.components.environment.JavaPropertiesComponent
import org.apache.solr.composeui.components.environment.VersionsComponent
import org.apache.solr.composeui.stores.nodes.NodesStore

internal val nodesStoreStateToVersionsModel: (NodesStore.State) -> VersionsComponent.Model = {
    VersionsComponent.Model(
        solrSpec = it.systemData.lucene.solrSpecVersion,
        solrImpl = it.systemData.lucene.solrImplVersion,
        luceneSpec = it.systemData.lucene.luceneSpecVersion,
        luceneImpl = it.systemData.lucene.luceneImplVersion,
        jvmName = it.systemData.jvm.name,
        jvmVersion = it.systemData.jvm.version,
    )
}

internal val nodesStoreStateToJavaPropsModel: (NodesStore.State) -> JavaPropertiesComponent.Model = {
    JavaPropertiesComponent.Model(
        properties = it.javaProperties,
    )
}

internal val nodesStoreStateToCommandLineArgsModel: (NodesStore.State) -> CommandLineArgsComponent.Model = {
    CommandLineArgsComponent.Model(
        arguments = it.systemData.jvm.jmx.commandLineArgs,
    )
}
