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

package org.apache.solr.ui.views.configsets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.apache.solr.ui.domain.FileSyntax
import org.apache.solr.ui.generated.resources.Res
import org.apache.solr.ui.generated.resources.action_save_changes
import org.apache.solr.ui.generated.resources.save
import org.apache.solr.ui.views.components.SolrButton
import org.apache.solr.ui.views.components.SolrCard
import org.apache.solr.ui.views.files.FileEditor
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditSolrConfigDialog(
    solrConfig: String,
    onSave: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) = Dialog(
    onDismissRequest = onCancel,
    properties = DialogProperties(usePlatformDefaultWidth = false)
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(solrConfig)) }

    SolrCard(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        FileEditor(
            value = textFieldValue,
            onValueChange = { textFieldValue = it },
            syntax = FileSyntax.Xml,
            modifier = Modifier.weight(1f),
        )

        SolrButton(onClick = { onSave(textFieldValue.text) }) {
            Icon(painter = painterResource(Res.drawable.save), contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(Res.string.action_save_changes))
        }
    }
}
