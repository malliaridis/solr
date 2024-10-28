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

import static org.apache.solr.delegator.Delegates.servletRequest;
import static org.apache.solr.delegator.Delegates.servletResponse;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;

public class RequestDispatcherJakartaDelegate implements RequestDispatcher {

  final javax.servlet.RequestDispatcher delegate;

  RequestDispatcherJakartaDelegate(final javax.servlet.RequestDispatcher dispatcher) {
    delegate = dispatcher;
  }

  @Override
  public void forward(ServletRequest request, ServletResponse response)
      throws ServletException, IOException {
    try {
      delegate.forward(servletRequest(request), servletResponse(response));
    } catch (javax.servlet.ServletException e) {
      throw new ServletException(e.getMessage(), e);
    }
  }

  @Override
  public void include(ServletRequest request, ServletResponse response)
      throws ServletException, IOException {
    try {
      delegate.include(servletRequest(request), servletResponse(response));
    } catch (javax.servlet.ServletException e) {
      throw new ServletException(e.getMessage(), e);
    }
  }
}
