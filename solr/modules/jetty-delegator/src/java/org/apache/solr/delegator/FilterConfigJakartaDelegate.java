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

import static org.apache.solr.delegator.Delegates.servletContext;

import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import java.util.Enumeration;

class FilterConfigJakartaDelegate implements FilterConfig {

  final javax.servlet.FilterConfig delegate;

  public FilterConfigJakartaDelegate(javax.servlet.FilterConfig config) {
    delegate = config;
  }

  @Override
  public String getFilterName() {
    return delegate.getFilterName();
  }

  @Override
  public ServletContext getServletContext() {
    return servletContext(delegate.getServletContext());
  }

  @Override
  public String getInitParameter(String name) {
    return delegate.getInitParameter(name);
  }

  @Override
  public Enumeration<String> getInitParameterNames() {
    return delegate.getInitParameterNames();
  }
}
