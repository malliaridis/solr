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

import static org.apache.solr.delegator.Delegates.asyncListener;
import static org.apache.solr.delegator.Delegates.servletContext;
import static org.apache.solr.delegator.Delegates.servletRequest;
import static org.apache.solr.delegator.Delegates.servletResponse;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

class AsyncContextJakartaDelegate implements AsyncContext {

  final javax.servlet.AsyncContext delegate;

  AsyncContextJakartaDelegate(final javax.servlet.AsyncContext asyncContext) {
    delegate = asyncContext;
  }

  @Override
  public ServletRequest getRequest() {
    return servletRequest(delegate.getRequest());
  }

  @Override
  public ServletResponse getResponse() {
    return servletResponse(delegate.getResponse());
  }

  @Override
  public boolean hasOriginalRequestAndResponse() {
    return delegate.hasOriginalRequestAndResponse();
  }

  @Override
  public void dispatch() {
    delegate.dispatch();
  }

  @Override
  public void dispatch(String path) {
    delegate.dispatch(path);
  }

  @Override
  public void dispatch(ServletContext context, String path) {
    delegate.dispatch(servletContext(context), path);
  }

  @Override
  public void complete() {
    delegate.complete();
  }

  @Override
  public void start(Runnable run) {
    delegate.start(run);
  }

  @Override
  public void addListener(AsyncListener listener) {
    delegate.addListener(asyncListener(listener));
  }

  @Override
  public void addListener(
      AsyncListener listener, ServletRequest servletRequest, ServletResponse servletResponse) {
    delegate.addListener(
        asyncListener(listener), servletRequest(servletRequest), servletResponse(servletResponse));
  }

  @Override
  public <T extends AsyncListener> T createListener(Class<T> clazz) throws ServletException {
    throw new UnsupportedOperationException("Unsupported delegate method.");
    // TODO See if this can be implemented;
  }

  @Override
  public void setTimeout(long timeout) {
    delegate.setTimeout(timeout);
  }

  @Override
  public long getTimeout() {
    return delegate.getTimeout();
  }
}
