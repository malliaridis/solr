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

package org.apache.solr.ui.domain.files

import kotlin.time.Instant

/**
 * File entry as represented by a file explorer.
 *
 * @property path Path relative to context, e.g. "lang/stopwords_en.txt"
 * @property content Current in-memory content (the working copy state)
 * @property originalContent Content as fetched from API (the base)
 * @property lastModified When the user last edited it locally
 * @property fetchedAt When it was originally fetched from Solr
 * @property zkVersion Experimental ZK stat.version at fetch time, for conflict detection.
 * @property encoding How to interpret the bytes for the editor
 * @property changeState Derived, but cached here for convenience
 */
data class FileEntry(
    val path: String,
    val content: ByteArray,
    val originalContent: ByteArray,
    val lastModified: Instant,
    val fetchedAt: Instant,
    val zkVersion: Int,
    val encoding: FileEncoding,
    val changeState: ChangeState = ChangeState.Unchanged,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as FileEntry

        if (zkVersion != other.zkVersion) return false
        if (path != other.path) return false
        if (!content.contentEquals(other.content)) return false
        if (!originalContent.contentEquals(other.originalContent)) return false
        if (lastModified != other.lastModified) return false
        if (fetchedAt != other.fetchedAt) return false
        if (encoding != other.encoding) return false
        if (changeState != other.changeState) return false

        return true
    }

    override fun hashCode(): Int {
        var result = zkVersion
        result = 31 * result + path.hashCode()
        result = 31 * result + content.contentHashCode()
        result = 31 * result + originalContent.contentHashCode()
        result = 31 * result + lastModified.hashCode()
        result = 31 * result + fetchedAt.hashCode()
        result = 31 * result + encoding.hashCode()
        result = 31 * result + changeState.hashCode()
        return result
    }
}
