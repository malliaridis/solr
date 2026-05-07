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

package org.apache.solr.ui.components.configsets.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.solr.ui.components.configsets.domain.LoadConfigsetDetailsUseCase
import org.apache.solr.ui.components.configsets.domain.UploadConfigsetFileUseCase
import org.apache.solr.ui.domain.configsets.ConfigsetWorkingCopy
import org.apache.solr.ui.domain.files.FileChange
import org.apache.solr.ui.utils.AppDispatchers

/**
 * The configset editor viewmodel holds a working copy of the currently selected configset.
 *
 * It is used to manage a working copy of the configset that can be applied.
 */
class ConfigsetEditorViewModel(
    private val configsetName: String,
    private val loadConfigsetDetailsUseCase: LoadConfigsetDetailsUseCase,
    private val uploadConfigsetFileUseCase: UploadConfigsetFileUseCase,
    private val dispatchers: AppDispatchers,
) : ViewModel() {
    val uiState: StateFlow<ConfigsetEditorUiState>
        field = MutableStateFlow<ConfigsetEditorUiState>(ConfigsetEditorUiState.Loading)

    init {
        loadConfigsetDetails()
    }

    /**
     * Refreshes the configset details by reloading the details without dropping the current
     * modifications.
     */
    fun refreshConfigsetDetails() {
        // TODO Not yet implemented
    }

    /**
     * Add a new file change to the state.
     *
     * @param change The change to add.
     */
    fun addFileChange(change: FileChange) {
        // TODO Not yet implemented
    }

    /**
     * Updates an existing file change.
     *
     * @param change The change to update containing the new changes.
     */
    fun updateFileChange(change: FileChange) {
        // TODO Not yet implemented
    }

    /**
     * Removes a specific file change.
     *
     * @param change The change to remove.
     */
    fun removeFileChange(change: FileChange) {
        // TODO Not yet implemented
    }

    /**
     * Applies the changes that are currently stored in the UI state.
     */
    fun applyChanges() {
        // TODO Not yet implemented
    }

    private fun loadConfigsetDetails() = viewModelScope.launch {
        withContext(dispatchers.io) {
            loadConfigsetDetailsUseCase(configsetName)
        }.onSuccess { configset ->
            uiState.update {
                when (it) {
                    is ConfigsetEditorUiState.Error -> TODO()
                    is ConfigsetEditorUiState.Loading -> ConfigsetEditorUiState.Ready(
                        workingCopy = ConfigsetWorkingCopy.fromConfigsetDetails(configset),
                    )
                    is ConfigsetEditorUiState.Ready -> it.copy(
                        workingCopy = it.workingCopy.copy(
                            files = configset.files,
                            baseRevision = configset.baseRevision,
                            // TODO Make a diff and update the current
                            //  changes based on the new base configset details
                            // This may render some changes redundant
                        )
                    )
                }
            }
        }
        // TODO Handle failures
    }
}

sealed class ConfigsetEditorUiState {
    data object Loading : ConfigsetEditorUiState()
    data class Ready(val workingCopy: ConfigsetWorkingCopy) : ConfigsetEditorUiState()
    data class Error(val message: String) : ConfigsetEditorUiState()
}
