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

package org.apache.solr.ui.views.files

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.apache.solr.ui.domain.FileSyntax
import org.apache.solr.ui.views.syntax.JsonSyntaxHighlightTransformation
import org.apache.solr.ui.views.syntax.XmlSyntaxHighlightTransformation

@Composable
fun FileEditor(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    syntax: FileSyntax,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    val lines = value.text.split("\n")

    // Calculate current line index from cursor
    val cursorPosition = value.selection.start
    val currentLine = remember(value.text, cursorPosition) {
        value.text.take(cursorPosition).count { it == '\n' }
    }

    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)),
    ) {
        Column(
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .verticalScroll(scrollState)
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)),
        ) {
            lines.forEachIndexed { index, _ ->
                val isCurrent = index == currentLine
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isCurrent) MaterialTheme.colorScheme.surfaceDim else Color.Transparent,
                        )
                        .padding(horizontal = 8.dp),
                ) {
                    Text(
                        text = "${index + 1}",
                        softWrap = false,
                        textAlign = TextAlign.End,
                        fontFamily = FontFamily.Monospace,
                        color = (
                            if (isCurrent) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                            ).copy(alpha = 0.38f),
                        fontSize = 12.sp,
                        lineHeight = 20.sp,
                    )
                }
            }
        }
        val horizontalScrollState = rememberScrollState()
        var contentWidth by remember { mutableIntStateOf(0) }
        var containerWidth by remember { mutableIntStateOf(0) }

        // Editor with highlighted current line
        Box(
            modifier = Modifier
                .weight(1f)
                .onSizeChanged { containerWidth = it.width }
                .horizontalScroll(horizontalScrollState)
                .verticalScroll(scrollState),
        ) {
            // Draw highlighted line background
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { contentWidth = it.width },
            ) {
                lines.forEachIndexed { index, line ->
                    val isCurrent = index == currentLine

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (isCurrent) MaterialTheme.colorScheme.surfaceDim else Color.Transparent,
                            ),
                    ) {
                        Text(
                            text = line.ifEmpty { " " },
                            color = Color.Transparent,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                }
            }

            // BasicTextField draws its own current-line highlight via drawBehind
            val lineHeightPx = with(LocalDensity.current) { 20.sp.toPx() }
            val highlightColor = MaterialTheme.colorScheme.surfaceDim
            val fullWidth = with(LocalDensity.current) { maxOf(contentWidth, containerWidth).toDp() }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.None,
                    ),
                ),
                modifier = Modifier
                    .width(fullWidth)
                    .padding(horizontal = 2.dp)
                    .drawBehind {
                        val top = currentLine * lineHeightPx
                        drawRect(
                            color = highlightColor,
                            topLeft = Offset(x = 0f, y = top),
                            size = size.copy(height = lineHeightPx),
                        )
                    },
                visualTransformation = when (syntax) {
                    FileSyntax.Json -> JsonSyntaxHighlightTransformation()
                    FileSyntax.Xml -> XmlSyntaxHighlightTransformation()
                    FileSyntax.Text -> VisualTransformation.None
                },
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
            )
        }
    }
}
