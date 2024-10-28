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

import static org.apache.solr.delegator.Delegates.servletConfig;
import static org.apache.solr.delegator.Delegates.servletRequest;
import static org.apache.solr.delegator.Delegates.servletResponse;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;

public class ServletJakartaDelegate implements Servlet {

  final javax.servlet.Servlet delegate;

  ServletJakartaDelegate(final javax.servlet.Servlet servlet) {
    delegate = servlet;
  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    try {
      delegate.init(servletConfig(config));
    } catch (javax.servlet.ServletException e) {
      throw new ServletException(e.getMessage(), e);
    }
  }

  @Override
  public ServletConfig getServletConfig() {
    return servletConfig(delegate.getServletConfig());
  }

  @Override
  public void service(ServletRequest req, ServletResponse res)
      throws ServletException, IOException {
    try {
      delegate.service(servletRequest(req), servletResponse(res));
    } catch (javax.servlet.ServletException e) {
      throw new ServletException(e.getMessage(), e);
    }
  }

  @Override
  public String getServletInfo() {
    return delegate.getServletInfo();
  }

  @Override
  public void destroy() {
    delegate.destroy();
  }
}
