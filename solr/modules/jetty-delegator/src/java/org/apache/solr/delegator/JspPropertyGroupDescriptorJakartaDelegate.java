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

package org.apache.solr.delegator;

import jakarta.servlet.descriptor.JspPropertyGroupDescriptor;
import java.util.Collection;

public class JspPropertyGroupDescriptorJakartaDelegate implements JspPropertyGroupDescriptor {

  final javax.servlet.descriptor.JspPropertyGroupDescriptor delegate;

  JspPropertyGroupDescriptorJakartaDelegate(
      javax.servlet.descriptor.JspPropertyGroupDescriptor descriptor) {
    delegate = descriptor;
  }

  @Override
  public Collection<String> getUrlPatterns() {
    return delegate.getUrlPatterns();
  }

  @Override
  public String getElIgnored() {
    return delegate.getElIgnored();
  }

  @Override
  public String getPageEncoding() {
    return delegate.getPageEncoding();
  }

  @Override
  public String getScriptingInvalid() {
    return delegate.getScriptingInvalid();
  }

  @Override
  public String getIsXml() {
    return delegate.getIsXml();
  }

  @Override
  public Collection<String> getIncludePreludes() {
    return delegate.getIncludePreludes();
  }

  @Override
  public Collection<String> getIncludeCodas() {
    return delegate.getIncludeCodas();
  }

  @Override
  public String getDeferredSyntaxAllowedAsLiteral() {
    return delegate.getDeferredSyntaxAllowedAsLiteral();
  }

  @Override
  public String getTrimDirectiveWhitespaces() {
    return delegate.getTrimDirectiveWhitespaces();
  }

  @Override
  public String getDefaultContentType() {
    return delegate.getDefaultContentType();
  }

  @Override
  public String getBuffer() {
    return delegate.getBuffer();
  }

  @Override
  public String getErrorOnUndeclaredNamespace() {
    return delegate.getErrorOnUndeclaredNamespace();
  }
}
