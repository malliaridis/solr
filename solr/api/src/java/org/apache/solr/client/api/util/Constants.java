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

package org.apache.solr.client.api.util;

public class Constants {
  private Constants() {
    /* Private ctor prevents instantiation */
  }

  public static final String INDEX_TYPE_PATH_PARAMETER = "indexType";
  public static final String INDEX_NAME_PATH_PARAMETER = "indexName";
  public static final String INDEX_PATH_PREFIX =
      "/{" + INDEX_TYPE_PATH_PARAMETER + ":cores|collections}/{" + INDEX_NAME_PATH_PARAMETER + "}";

  public static final String CORE_NAME_PATH_PARAMETER = "coreName";

  public static final String OMIT_FROM_CODEGEN_PROPERTY = "omitFromCodegen";
  public static final String GENERIC_ENTITY_PROPERTY = "genericEntity";

  public static final String BINARY_CONTENT_TYPE_V2 = "application/vnd.apache.solr.javabin";
}
