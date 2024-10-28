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

import static org.apache.solr.delegator.Delegates.servletOutputStream;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

class ServletResponseJakartaDelegate implements ServletResponse {

  final javax.servlet.ServletResponse delegate;

  public ServletResponseJakartaDelegate(javax.servlet.ServletResponse response) {
    delegate = response;
  }

  @Override
  public String getCharacterEncoding() {
    return delegate.getCharacterEncoding();
  }

  @Override
  public String getContentType() {
    return delegate.getContentType();
  }

  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    return servletOutputStream(delegate.getOutputStream());
  }

  @Override
  public PrintWriter getWriter() throws IOException {
    return delegate.getWriter();
  }

  @Override
  public void setCharacterEncoding(String charset) {
    delegate.setCharacterEncoding(charset);
  }

  @Override
  public void setContentLength(int len) {
    delegate.setContentLength(len);
  }

  @Override
  public void setContentLengthLong(long len) {
    delegate.setContentLengthLong(len);
  }

  @Override
  public void setContentType(String type) {
    delegate.setContentType(type);
  }

  @Override
  public void setBufferSize(int size) {
    delegate.setBufferSize(size);
  }

  @Override
  public int getBufferSize() {
    return delegate.getBufferSize();
  }

  @Override
  public void flushBuffer() throws IOException {
    delegate.flushBuffer();
  }

  @Override
  public void resetBuffer() {
    delegate.resetBuffer();
  }

  @Override
  public boolean isCommitted() {
    return delegate.isCommitted();
  }

  @Override
  public void reset() {
    delegate.reset();
  }

  @Override
  public void setLocale(Locale loc) {
    delegate.setLocale(loc);
  }

  @Override
  public Locale getLocale() {
    return delegate.getLocale();
  }
}
