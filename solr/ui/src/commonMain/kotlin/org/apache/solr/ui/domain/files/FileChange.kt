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

sealed interface FileChange {
    /**
     * Relative to the current context path (e.g. to configset root).
     */
    val path: String

    data class Modified(
        override val path: String,
        val originalContent: ByteArray,
        val newContent: ByteArray,
    ) : FileChange {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Modified

            if (path != other.path) return false
            if (!originalContent.contentEquals(other.originalContent)) return false
            if (!newContent.contentEquals(other.newContent)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = path.hashCode()
            result = 31 * result + originalContent.contentHashCode()
            result = 31 * result + newContent.contentHashCode()
            return result
        }
    }

    data class Added(
        override val path: String,
        val content: ByteArray,
    ) : FileChange {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Added

            if (path != other.path) return false
            if (!content.contentEquals(other.content)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = path.hashCode()
            result = 31 * result + content.contentHashCode()
            return result
        }
    }

    data class Deleted(
        override val path: String,
        val originalContent: ByteArray,
    ) : FileChange {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Deleted

            if (path != other.path) return false
            if (!originalContent.contentEquals(other.originalContent)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = path.hashCode()
            result = 31 * result + originalContent.contentHashCode()
            return result
        }
    }

    data class Renamed(
        override val path: String,
        val newPath: String,
        val content: ByteArray,
    ) : FileChange {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Renamed

            if (path != other.path) return false
            if (newPath != other.newPath) return false
            if (!content.contentEquals(other.content)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = path.hashCode()
            result = 31 * result + newPath.hashCode()
            result = 31 * result + content.contentHashCode()
            return result
        }
    }
}
