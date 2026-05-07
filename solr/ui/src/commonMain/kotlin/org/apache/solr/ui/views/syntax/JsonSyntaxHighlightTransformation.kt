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

package org.apache.solr.ui.views.syntax

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class JsonSyntaxHighlightTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val input = text.text
        val builder = AnnotatedString.Builder(input)

        val keyColor = Color(0xFF871094)
        val stringColor = Color(0xFF067D17)
        val numberColor = Color(0xFF1750EB)
        val booleanColor = Color(0xFF0033B3)

        // Keys
        Regex("\"(.*?)\"(?=\\s*:)").findAll(input).forEach {
            builder.addStyle(
                style = SpanStyle(color = keyColor),
                start = it.range.first,
                end = it.range.last + 1,
            )
        }

        // Strings
        Regex(":\\s*\"(.*?)\"").findAll(input).forEach {
            val start = it.range.first + it.value.indexOf('"')
            val end = it.range.last
            builder.addStyle(
                style = SpanStyle(color = stringColor),
                start = start,
                end = end + 1,
            )
        }

        // Numbers
        Regex("\\b\\d+(\\.\\d+)?\\b").findAll(input).forEach {
            builder.addStyle(
                style = SpanStyle(color = numberColor),
                start = it.range.first,
                end = it.range.last + 1,
            )
        }

        // Booleans / null
        Regex("\\b(true|false|null)\\b").findAll(input).forEach {
            builder.addStyle(
                style = SpanStyle(color = booleanColor),
                start = it.range.first,
                end = it.range.last + 1,
            )
        }

        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}
