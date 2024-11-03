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

import java.lang.invoke.MethodHandles;
import static org.apache.solr.delegator.Delegates.dynamicFilterRegistration;
import static org.apache.solr.delegator.Delegates.dynamicServletRegistration;
import static org.apache.solr.delegator.Delegates.filter;
import static org.apache.solr.delegator.Delegates.filterRegistration;
import static org.apache.solr.delegator.Delegates.jspConfigDescriptor;
import static org.apache.solr.delegator.Delegates.requestDispatcher;
import static org.apache.solr.delegator.Delegates.servlet;
import static org.apache.solr.delegator.Delegates.servletContext;
import static org.apache.solr.delegator.Delegates.servletRegistration;
import static org.apache.solr.delegator.Delegates.sessionCookieConfig;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ServletContextJavaxDelegate implements ServletContext {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  final jakarta.servlet.ServletContext delegate;

  public ServletContextJavaxDelegate(final jakarta.servlet.ServletContext context) {
    delegate = context;
  }

  @Override
  public String getContextPath() {
    return delegate.getContextPath();
  }

  @Override
  public ServletContext getContext(String uripath) {
    return servletContext(delegate.getContext(uripath));
  }

  @Override
  public int getMajorVersion() {
    return delegate.getMajorVersion();
  }

  @Override
  public int getMinorVersion() {
    return delegate.getMinorVersion();
  }

  @Override
  public int getEffectiveMajorVersion() {
    return delegate.getEffectiveMajorVersion();
  }

  @Override
  public int getEffectiveMinorVersion() {
    return delegate.getEffectiveMinorVersion();
  }

  @Override
  public String getMimeType(String file) {
    return delegate.getMimeType(file);
  }

  @Override
  public Set<String> getResourcePaths(String path) {
    return delegate.getResourcePaths(path);
  }

  @Override
  public URL getResource(String path) throws MalformedURLException {
    return delegate.getResource(path);
  }

  @Override
  public InputStream getResourceAsStream(String path) {
    return delegate.getResourceAsStream(path);
  }

  @Override
  public RequestDispatcher getRequestDispatcher(String path) {
    return requestDispatcher(delegate.getRequestDispatcher(path));
  }

  @Override
  public RequestDispatcher getNamedDispatcher(String name) {
    return requestDispatcher(delegate.getNamedDispatcher(name));
  }

  @Override
  public Servlet getServlet(String name) throws ServletException {
    try {
      return servlet(delegate.getServlet(name));
    } catch (jakarta.servlet.ServletException e) {
      throw new ServletException(e.getMessage(), e);
    }
  }

  @Override
  public Enumeration<Servlet> getServlets() {
    return new MappedEnumeration<>(delegate.getServlets(), Delegates::servlet);
  }

  @Override
  public Enumeration<String> getServletNames() {
    return delegate.getServletNames();
  }

  @Override
  public void log(String msg) {
    delegate.log(msg);
  }

  @Override
  public void log(Exception exception, String msg) {
    delegate.log(exception, msg);
  }

  @Override
  public void log(String message, Throwable throwable) {
    delegate.log(message, throwable);
  }

  @Override
  public String getRealPath(String path) {
    return delegate.getRealPath(path);
  }

  @Override
  public String getServerInfo() {
    return delegate.getServerInfo();
  }

  @Override
  public String getInitParameter(String name) {
    return delegate.getInitParameter(name);
  }

  @Override
  public Enumeration<String> getInitParameterNames() {
    return delegate.getInitParameterNames();
  }

  @Override
  public boolean setInitParameter(String name, String value) {
    return delegate.setInitParameter(name, value);
  }

  @Override
  public Object getAttribute(String name) {
    return delegate.getAttribute(name);
  }

  @Override
  public Enumeration<String> getAttributeNames() {
    return delegate.getAttributeNames();
  }

  @Override
  public void setAttribute(String name, Object object) {
    delegate.setAttribute(name, object);
  }

  @Override
  public void removeAttribute(String name) {
    delegate.removeAttribute(name);
  }

  @Override
  public String getServletContextName() {
    return delegate.getServletContextName();
  }

  @Override
  public ServletRegistration.Dynamic addServlet(String servletName, String className) {
    log.warn("Critical delegate method called.");
    return dynamicServletRegistration(delegate.addServlet(servletName, className));
  }

  @Override
  public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
    return dynamicServletRegistration(delegate.addServlet(servletName, servlet(servlet)));
  }

  @Override
  public ServletRegistration.Dynamic addServlet(
      String servletName, Class<? extends Servlet> servletClass) {
    throw new UnsupportedOperationException("Unsupported delegate method.");
    // TODO See if this can be implemented
  }

  @Override
  public ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) {
    return dynamicServletRegistration(delegate.addJspFile(servletName, jspFile));
  }

  @Override
  public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
    throw new UnsupportedOperationException("Unsupported delegate method.");
    // TODO See if this can be implemented
  }

  @Override
  public ServletRegistration getServletRegistration(String servletName) {
    return servletRegistration(delegate.getServletRegistration(servletName));
  }

  @Override
  public Map<String, ? extends ServletRegistration> getServletRegistrations() {
    return delegate.getServletRegistrations().entrySet().stream()
        .collect(
            Collectors.toMap(Map.Entry::getKey, entry -> servletRegistration(entry.getValue())));
  }

  @Override
  public FilterRegistration.Dynamic addFilter(String filterName, String className) {
    log.warn("Critical delegate method called.");
    return dynamicFilterRegistration(delegate.addFilter(filterName, className));
  }

  @Override
  public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
    return dynamicFilterRegistration(delegate.addFilter(filterName, filter(filter)));
  }

  @Override
  public FilterRegistration.Dynamic addFilter(
      String filterName, Class<? extends Filter> filterClass) {
    throw new UnsupportedOperationException("Unsupported delegate method.");
    // TODO See if this can be implemented
  }

  @Override
  public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
    throw new UnsupportedOperationException("Unsupported delegate method.");
    // TODO See if this can be implemented
  }

  @Override
  public FilterRegistration getFilterRegistration(String filterName) {
    return filterRegistration(delegate.getFilterRegistration(filterName));
  }

  @Override
  public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
    return delegate.getFilterRegistrations().entrySet().stream()
        .collect(
            Collectors.toMap(Map.Entry::getKey, entry -> filterRegistration(entry.getValue())));
  }

  @Override
  public SessionCookieConfig getSessionCookieConfig() {
    return sessionCookieConfig(delegate.getSessionCookieConfig());
  }

  @Override
  public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
    delegate.setSessionTrackingModes(
        sessionTrackingModes.stream()
            .map(Delegates::sessionTrackingMode)
            .collect(Collectors.toSet()));
  }

  @Override
  public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
    return delegate.getDefaultSessionTrackingModes().stream()
        .map(Delegates::sessionTrackingMode)
        .collect(Collectors.toSet());
  }

  @Override
  public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
    return delegate.getEffectiveSessionTrackingModes().stream()
        .map(Delegates::sessionTrackingMode)
        .collect(Collectors.toSet());
  }

  @Override
  public void addListener(String className) {
    log.warn("Critical delegate method called.");
    delegate.addListener(className);
  }

  @Override
  public <T extends EventListener> void addListener(T t) {
    delegate.addListener(t);
  }

  @Override
  public void addListener(Class<? extends EventListener> listenerClass) {
    throw new UnsupportedOperationException("Unsupported delegate method.");
    // TODO See if this can be implemented
  }

  @Override
  public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
    throw new UnsupportedOperationException("Unsupported delegate method.");
    // TODO See if this can be implemented
  }

  @Override
  public JspConfigDescriptor getJspConfigDescriptor() {
    return jspConfigDescriptor(delegate.getJspConfigDescriptor());
  }

  @Override
  public ClassLoader getClassLoader() {
    return delegate.getClassLoader();
  }

  @Override
  public void declareRoles(String... roleNames) {
    delegate.declareRoles(roleNames);
  }

  @Override
  public String getVirtualServerName() {
    return delegate.getVirtualServerName();
  }

  @Override
  public int getSessionTimeout() {
    return delegate.getSessionTimeout();
  }

  @Override
  public void setSessionTimeout(int sessionTimeout) {
    delegate.setSessionTimeout(sessionTimeout);
  }

  @Override
  public String getRequestCharacterEncoding() {
    return delegate.getRequestCharacterEncoding();
  }

  @Override
  public void setRequestCharacterEncoding(String encoding) {
    delegate.setRequestCharacterEncoding(encoding);
  }

  @Override
  public String getResponseCharacterEncoding() {
    return delegate.getResponseCharacterEncoding();
  }

  @Override
  public void setResponseCharacterEncoding(String encoding) {
    delegate.setResponseCharacterEncoding(encoding);
  }
}
