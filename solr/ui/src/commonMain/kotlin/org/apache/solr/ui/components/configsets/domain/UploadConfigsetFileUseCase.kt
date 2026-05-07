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

package org.apache.solr.ui.components.configsets.domain

import org.apache.solr.ui.domain.configsets.Configset

/**
 * Use case for loading the available configsets.
 */
interface UploadConfigsetFileUseCase {
    /**
     * Default invocation for uploading a configset file.
     *
     * @param configsetName The name of the configset.
     * @param filePath The path to use for the file.
     * @param fileContent The new content of the file.
     * @param overrideExisting Whether to override the file if it already exists.
     */
    suspend operator fun invoke(
        configsetName: String,
        filePath: String,
        fileContent: ByteArray,
        overrideExisting: Boolean,
    ): Result<Unit>
}
