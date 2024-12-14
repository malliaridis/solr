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

package org.apache.solr.cli

internal object StatusCode {

    /**
     * Indicates that the command or program executed successfully without any errors.
     */
    const val SUCCESS = 0

    /**
     * A catch-all exit code for a variety of general errors. Often used when the command or program
     * encounters an error, but no specific exit code is available for the situation.
     */
    const val GENERAL_ERROR = 1

    /**
     * The command was found, but it could not be executed, possibly due to insufficient permissions
     * or other issues.
     */
    const val COMMAND_CANNOT_EXECUTE  = 126

    /**
     * The command was not found in the system's PATH, indicating that either the command does not
     * exist or the PATH variable is incorrectly set.
     */
    const val COMMAND_NOT_FOUND = 127
}