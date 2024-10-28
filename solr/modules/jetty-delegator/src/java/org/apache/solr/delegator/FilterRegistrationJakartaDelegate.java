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

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterRegistration;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FilterRegistrationJakartaDelegate implements FilterRegistration {

  final javax.servlet.FilterRegistration delegate;

  FilterRegistrationJakartaDelegate(final javax.servlet.FilterRegistration registration) {
    delegate = registration;
  }

  @Override
  public void addMappingForServletNames(
      EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... servletNames) {
    EnumSet<javax.servlet.DispatcherType> mappedDispatcherTypes =
        dispatcherTypes.stream()
            .map(Delegates::dispatcherType)
            .collect(
                Collectors.toCollection(() -> EnumSet.noneOf(javax.servlet.DispatcherType.class)));
    delegate.addMappingForServletNames(mappedDispatcherTypes, isMatchAfter, servletNames);
  }

  @Override
  public Collection<String> getServletNameMappings() {
    return delegate.getServletNameMappings();
  }

  @Override
  public void addMappingForUrlPatterns(
      EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... urlPatterns) {
    EnumSet<javax.servlet.DispatcherType> mappedDispatcherTypes =
        dispatcherTypes.stream()
            .map(Delegates::dispatcherType)
            .collect(
                Collectors.toCollection(() -> EnumSet.noneOf(javax.servlet.DispatcherType.class)));
    delegate.addMappingForUrlPatterns(mappedDispatcherTypes, isMatchAfter, urlPatterns);
  }

  @Override
  public Collection<String> getUrlPatternMappings() {
    return delegate.getUrlPatternMappings();
  }

  @Override
  public String getName() {
    return delegate.getName();
  }

  @Override
  public String getClassName() {
    return delegate.getClassName();
  }

  @Override
  public boolean setInitParameter(String name, String value) {
    return delegate.setInitParameter(name, value);
  }

  @Override
  public String getInitParameter(String name) {
    return delegate.getInitParameter(name);
  }

  @Override
  public Set<String> setInitParameters(Map<String, String> initParameters) {
    return delegate.setInitParameters(initParameters);
  }

  @Override
  public Map<String, String> getInitParameters() {
    return delegate.getInitParameters();
  }
}
