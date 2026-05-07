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

package org.apache.solr.ui.domain.configsets

import org.apache.solr.ui.domain.files.FileChange
import org.apache.solr.ui.domain.files.FileEntry
import org.apache.solr.ui.domain.files.WorkingCopyStatus

/**
 * @property baseRevision ZK version / timestamp of the fetch
 * @property files Map of paths to current in-memory state of files
 * @property changes computed diff vs. base
 */
data class ConfigsetWorkingCopy(
    val configsetName: String,
    val baseRevision: String = "",
    val files: Map<String, FileEntry> = emptyMap(),
    val changes: List<FileChange> = emptyList(),
    val status: WorkingCopyStatus = WorkingCopyStatus.CLEAN,
) {
    companion object {
        fun fromConfigsetDetails(configset: ConfigsetDetails) = ConfigsetWorkingCopy(
            configsetName = configset.configsetName,
            baseRevision = configset.baseRevision,
            files = configset.files,
        )
    }
}
