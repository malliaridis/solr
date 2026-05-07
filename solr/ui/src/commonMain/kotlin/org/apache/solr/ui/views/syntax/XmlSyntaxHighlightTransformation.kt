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

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import org.apache.solr.ui.views.theme.healthyDark
import org.apache.solr.ui.views.theme.healthyLight
import org.apache.solr.ui.views.theme.onSurfaceVariantDark
import org.apache.solr.ui.views.theme.onSurfaceVariantLight
import org.apache.solr.ui.views.theme.primaryContainerDark
import org.apache.solr.ui.views.theme.primaryContainerLight
import org.apache.solr.ui.views.theme.primaryDark
import org.apache.solr.ui.views.theme.primaryLight
import org.apache.solr.ui.views.theme.recoveryDark
import org.apache.solr.ui.views.theme.recoveryLight
import org.apache.solr.ui.views.theme.replicaContainerDark
import org.apache.solr.ui.views.theme.replicaContainerLight
import org.apache.solr.ui.views.theme.replicaDark
import org.apache.solr.ui.views.theme.replicaLight
import org.apache.solr.ui.views.theme.secondaryDark
import org.apache.solr.ui.views.theme.secondaryLight
import org.apache.solr.ui.views.theme.tertiaryContainerDark
import org.apache.solr.ui.views.theme.tertiaryContainerLight
import org.apache.solr.ui.views.theme.tertiaryDark
import org.apache.solr.ui.views.theme.tertiaryLight
import org.apache.solr.ui.views.theme.warningContainerDark
import org.apache.solr.ui.views.theme.warningContainerLight

/**
 * A [VisualTransformation] that applies XML syntax highlighting to the input text.
 *
 * Colors are sourced from the Solr UI theme color palette and adapt to the current
 * light/dark theme via the [useDarkTheme] parameter.
 *
 * Highlighted elements:
 * - Tags (`<`, `>`, `/>`, `</`) — structural brackets
 * - Tag names (`<tagName`) — the element name after `<`
 * - Custom/unknown tag names — same slot as tag name
 * - Namespace prefixes (`ns:`) — prefix before the colon in names
 * - Attribute names — keys in `key="value"` pairs
 * - Attribute values — quoted strings in attributes
 * - Comments (`<!-- ... -->`)
 * - Entity references (`&amp;`, `&#123;`, etc.)
 * - Prologue / processing instructions (`<?...?>`)
 * - CDATA / Tag Data (`<![CDATA[...]]>`)
 * - Matched tag highlight (the innermost tag pair around the cursor) — caller-driven,
 *   passed as [matchedTagRanges]
 * - Injected language fragments (content inside CDATA treated as a distinct region)
 */
class XmlSyntaxHighlightTransformation(
    private val useDarkTheme: Boolean = false,
    /**
     * Optional list of character ranges that represent a "matched tag" pair.
     * These are highlighted with a distinct background. Since [VisualTransformation]
     * has no cursor awareness, the caller (e.g. the composable managing the text field)
     * is responsible for computing these ranges and passing them in.
     *
     * Example: if the cursor is inside `<foo>...</foo>`, pass the ranges of
     * both `<foo>` and `</foo>`.
     */
    private val matchedTagRanges: List<IntRange> = emptyList(),
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            text = highlight(text.text),
            offsetMapping = OffsetMapping.Identity,
        )
    }

    private fun highlight(text: String): AnnotatedString = buildAnnotatedString {
        append(text)

        fun applyStyle(style: SpanStyle, range: IntRange) {
            if (range.first >= 0 && range.last < text.length && range.first <= range.last) {
                addStyle(style, range.first, range.last + 1)
            }
        }

        fun applyStyle(style: SpanStyle, matchResult: MatchResult) =
            applyStyle(style, matchResult.range)

        val colors = if (useDarkTheme) DarkXmlColors else LightXmlColors

        // Collect ranges that are already colored as comments or CDATA so that
        // inner passes can skip overlapping regions.
        val skipRanges = mutableListOf<IntRange>()

        // ------------------------------------------------------------------
        // 1. Comments  <!-- ... -->   (highest priority)
        // ------------------------------------------------------------------
        COMMENT_REGEX.findAll(text).forEach { match ->
            applyStyle(colors.comment, match)
            skipRanges += match.range
        }

        // ------------------------------------------------------------------
        // 2. Prologue / Processing Instructions  <? ... ?>
        // ------------------------------------------------------------------
        PROLOGUE_REGEX.findAll(text).forEach { match ->
            if (skipRanges.none { match.range.first in it }) {
                applyStyle(colors.prologue, match)
                // Highlight the PI target name (first word after `<?`)
                val inner = match.value
                val targetStart = 2 // skip `<?`
                val targetEnd = inner.indexOfFirst { it.isWhitespace() || it == '?' }
                    .takeIf { it > targetStart } ?: inner.length - 2
                val absStart = match.range.first + targetStart
                val absEnd = match.range.first + targetEnd - 1
                applyStyle(colors.tagName, absStart..absEnd)
                skipRanges += match.range
            }
        }

        // ------------------------------------------------------------------
        // 3. CDATA sections  <![CDATA[ ... ]]>
        //    Outer delimiters = tagData style, inner content = injected style
        // ------------------------------------------------------------------
        CDATA_REGEX.findAll(text).forEach { match ->
            if (skipRanges.none { match.range.first in it }) {
                applyStyle(colors.tagData, match)
                // Inner content sits between `<![CDATA[` (9 chars) and `]]>` (3 chars)
                val innerStart = match.range.first + 9
                val innerEnd = match.range.last - 3
                if (innerStart <= innerEnd) {
                    applyStyle(colors.injectedLanguage, innerStart..innerEnd)
                }
                skipRanges += match.range
            }
        }

        // ------------------------------------------------------------------
        // 4. Full tags — opening, closing, self-closing
        // ------------------------------------------------------------------
        TAG_REGEX.findAll(text).forEach { match ->
            if (skipRanges.none { match.range.first in it }) {
                val fullRange = match.range
                val rawTag = match.value
                val isClosing = rawTag.startsWith("</")
                val isSelfClosing = rawTag.endsWith("/>")

                // 4a. Tag brackets
                val openBracket = if (isClosing) "</" else "<"
                val closeBracket = if (isSelfClosing) "/>" else ">"
                applyStyle(
                    colors.tag,
                    fullRange.first..(fullRange.first + openBracket.length - 1),
                )
                applyStyle(
                    colors.tag,
                    (fullRange.last - closeBracket.length + 1)..fullRange.last,
                )

                // 4b. Tag name — first word token after the opening bracket
                //     Works by scanning past `<` or `</` then reading until whitespace or `>`
                val nameSearchStart = openBracket.length
                val nameRelStart = rawTag.indexOfFirst { it.isLetterOrDigit() || it == '_' || it == ':' }
                    .takeIf { it >= nameSearchStart } ?: nameSearchStart
                val nameRelEnd = (nameRelStart until rawTag.length)
                    .firstOrNull { idx ->
                        rawTag[idx].let { c -> c.isWhitespace() || c == '>' || c == '/' }
                    }?.minus(1) ?: (rawTag.length - 1)

                val tagNameValue = rawTag.substring(nameRelStart, nameRelEnd + 1)
                val colonIdx = tagNameValue.indexOf(':')
                val absNameStart = fullRange.first + nameRelStart

                if (colonIdx >= 0) {
                    // namespace prefix
                    applyStyle(
                        colors.namespacePrefix,
                        absNameStart..(absNameStart + colonIdx - 1),
                    )
                    // colon
                    applyStyle(
                        colors.tag,
                        (absNameStart + colonIdx)..(absNameStart + colonIdx),
                    )
                    // local name
                    applyStyle(
                        colors.tagName,
                        (absNameStart + colonIdx + 1)..(absNameStart + tagNameValue.length - 1),
                    )
                } else {
                    applyStyle(
                        colors.tagName,
                        absNameStart..(absNameStart + tagNameValue.length - 1),
                    )
                }

                // 4c. Attributes — only in opening / self-closing tags
                if (!isClosing) {
                    // The attribute search region starts after the tag name
                    val attrRegionOffset = nameRelEnd + 1
                    val attrRegion = if (attrRegionOffset < rawTag.length)
                        rawTag.substring(attrRegionOffset) else ""

                    ATTRIBUTE_REGEX.findAll(attrRegion).forEach { attrMatch ->
                        // attrMatch.range is relative to attrRegion
                        val attrAbsStart = fullRange.first + attrRegionOffset + attrMatch.range.first

                        // --- Attribute name ---
                        // Group 1 value is the attribute name; locate it at the start of the match
                        val attrNameValue = attrMatch.groupValues[1]
                        // The name always starts at the beginning of the attribute match
                        val attrNameAbsStart = attrAbsStart
                        val attrNameAbsEnd = attrNameAbsStart + attrNameValue.length - 1
                        val attrColonIdx = attrNameValue.indexOf(':')

                        if (attrColonIdx >= 0) {
                            applyStyle(
                                colors.namespacePrefix,
                                attrNameAbsStart..(attrNameAbsStart + attrColonIdx - 1),
                            )
                            applyStyle(
                                colors.tag,
                                (attrNameAbsStart + attrColonIdx)..(attrNameAbsStart + attrColonIdx),
                            )
                            applyStyle(
                                colors.attributeName,
                                (attrNameAbsStart + attrColonIdx + 1)..attrNameAbsEnd,
                            )
                        } else {
                            applyStyle(colors.attributeName, attrNameAbsStart..attrNameAbsEnd)
                        }

                        // --- Attribute value (quoted string incl. quotes) ---
                        // Group 2 value is the full quoted value; find where it starts inside
                        // the match by scanning past `name =` with optional whitespace
                        val attrMatchStr = attrMatch.value
                        val valueRelIdx = attrMatchStr.indexOf(attrMatch.groupValues[2])
                        if (valueRelIdx >= 0) {
                            val valAbsStart = attrAbsStart + valueRelIdx
                            val valAbsEnd = valAbsStart + attrMatch.groupValues[2].length - 1
                            applyStyle(colors.attributeValue, valAbsStart..valAbsEnd)
                        }
                    }
                }
            }
        }

        // ------------------------------------------------------------------
        // 5. Entity references  &name;  &#digits;  &#xHex;
        // ------------------------------------------------------------------
        ENTITY_REGEX.findAll(text).forEach { match ->
            if (skipRanges.none { match.range.first in it }) {
                applyStyle(colors.entityReference, match)
            }
        }

        // ------------------------------------------------------------------
        // 6. Matched tag highlights (caller-provided ranges, applied last)
        // ------------------------------------------------------------------
        matchedTagRanges.forEach { range ->
            applyStyle(colors.matchedTag, range)
        }
    }

    // ======================================================================
    // Regex patterns
    // ======================================================================
    companion object {
        /** Full XML comment */
        val COMMENT_REGEX = Regex("<!--[\\s\\S]*?-->")

        /** Processing instruction / prologue */
        val PROLOGUE_REGEX = Regex("<\\?[\\s\\S]*?\\?>")

        /** CDATA section */
        val CDATA_REGEX = Regex("<!\\[CDATA\\[[\\s\\S]*?]]>")

        /**
         * Matches any opening, closing, or self-closing tag.
         * Does NOT match comments or CDATA (those are filtered via [skipRanges]).
         */
        val TAG_REGEX = Regex("</?[\\w:][\\s\\S]*?/?>")

        /** Attribute: group 1 = name (with optional ns prefix), group 2 = full quoted value */
        val ATTRIBUTE_REGEX = Regex("""([\w:]+)\s*=\s*("[^"]*"|'[^']*')""")

        /** Entity reference */
        val ENTITY_REGEX = Regex("&(?:#\\d+|#x[0-9a-fA-F]+|[\\w]+);")
    }
}

// ==========================================================================
// Color sets
// ==========================================================================

private data class XmlColors(
    val tag: SpanStyle,
    val tagName: SpanStyle,
    val attributeName: SpanStyle,
    val attributeValue: SpanStyle,
    val comment: SpanStyle,
    val entityReference: SpanStyle,
    val matchedTag: SpanStyle,
    val namespacePrefix: SpanStyle,
    val prologue: SpanStyle,
    val tagData: SpanStyle,
    val injectedLanguage: SpanStyle,
)

/**
 * Light theme XML highlight colors derived from the Solr UI palette.
 *
 * | Element            | Color choice & rationale                               |
 * |--------------------|--------------------------------------------------------|
 * | tag brackets       | primaryLight (deep red) – structural chrome            |
 * | tag name           | primaryContainerLight (vivid red-orange) – focal name  |
 * | attribute name     | tertiaryLight (dark amber) – warm, distinct            |
 * | attribute value    | secondaryLight (muted red-brown) – softer              |
 * | comment            | healthyLight (dark green) – universally "comment"      |
 * | entity reference   | replicaLight (teal/blue) – reference / link feel       |
 * | matched tag        | warningContainerLight bg (yellow) – highlight          |
 * | namespace prefix   | recoveryLight (olive) – prefix "qualifier"             |
 * | prologue           | tertiaryContainerLight (gold) – meta/directive         |
 * | tag data (CDATA)   | onSurfaceVariantLight (grey) – raw data                |
 * | injected fragment  | replicaContainerLight (medium teal) – injected region  |
 */
private val LightXmlColors = XmlColors(
    tag = SpanStyle(color = primaryLight),
    tagName = SpanStyle(color = primaryContainerLight),
    attributeName = SpanStyle(color = tertiaryLight),
    attributeValue = SpanStyle(color = secondaryLight),
    comment = SpanStyle(color = healthyLight),
    entityReference = SpanStyle(color = replicaLight),
    matchedTag = SpanStyle(background = warningContainerLight),
    namespacePrefix = SpanStyle(color = recoveryLight),
    prologue = SpanStyle(color = tertiaryContainerLight),
    tagData = SpanStyle(color = onSurfaceVariantLight),
    injectedLanguage = SpanStyle(color = replicaContainerLight),
)

/**
 * Dark theme XML highlight colors derived from the Solr UI palette.
 *
 * | Element            | Color choice & rationale                               |
 * |--------------------|--------------------------------------------------------|
 * | tag brackets       | primaryDark (soft salmon) – structural chrome          |
 * | tag name           | primaryContainerDark (vivid red) – focal name          |
 * | attribute name     | tertiaryDark (warm gold) – warm, distinct              |
 * | attribute value    | secondaryDark (light pink-salmon) – softer             |
 * | comment            | healthyDark (mint green) – universally "comment"       |
 * | entity reference   | replicaDark (sky blue) – reference / link feel         |
 * | matched tag        | warningContainerDark bg (amber) – highlight            |
 * | namespace prefix   | recoveryDark (yellow-green) – prefix "qualifier"       |
 * | prologue           | tertiaryContainerDark (dark gold) – meta/directive     |
 * | tag data (CDATA)   | onSurfaceVariantDark (mid grey) – raw data             |
 * | injected fragment  | replicaContainerDark (medium teal) – injected region   |
 */
private val DarkXmlColors = XmlColors(
    tag = SpanStyle(color = primaryDark),
    tagName = SpanStyle(color = primaryContainerDark),
    attributeName = SpanStyle(color = tertiaryDark),
    attributeValue = SpanStyle(color = secondaryDark),
    comment = SpanStyle(color = healthyDark),
    entityReference = SpanStyle(color = replicaDark),
    matchedTag = SpanStyle(background = warningContainerDark),
    namespacePrefix = SpanStyle(color = recoveryDark),
    prologue = SpanStyle(color = tertiaryContainerDark),
    tagData = SpanStyle(color = onSurfaceVariantDark),
    injectedLanguage = SpanStyle(color = replicaContainerDark),
)
