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

import jakarta.servlet.SessionCookieConfig;

public class SessionCookieConfigJakartaDelegate implements SessionCookieConfig {

  final javax.servlet.SessionCookieConfig delegate;

  public SessionCookieConfigJakartaDelegate(final javax.servlet.SessionCookieConfig config) {
    delegate = config;
  }

  @Override
  public void setName(String name) {
    delegate.setName(name);
  }

  @Override
  public String getName() {
    return delegate.getName();
  }

  @Override
  public void setDomain(String domain) {
    delegate.setDomain(domain);
  }

  @Override
  public String getDomain() {
    return delegate.getDomain();
  }

  @Override
  public void setPath(String path) {
    delegate.setPath(path);
  }

  @Override
  public String getPath() {
    return delegate.getPath();
  }

  @Override
  public void setComment(String comment) {
    delegate.setComment(comment);
  }

  @Override
  public String getComment() {
    return delegate.getComment();
  }

  @Override
  public void setHttpOnly(boolean httpOnly) {
    delegate.setHttpOnly(httpOnly);
  }

  @Override
  public boolean isHttpOnly() {
    return delegate.isHttpOnly();
  }

  @Override
  public void setSecure(boolean secure) {
    delegate.setSecure(secure);
  }

  @Override
  public boolean isSecure() {
    return delegate.isSecure();
  }

  @Override
  public void setMaxAge(int maxAge) {
    delegate.setMaxAge(maxAge);
  }

  @Override
  public int getMaxAge() {
    return delegate.getMaxAge();
  }
}
