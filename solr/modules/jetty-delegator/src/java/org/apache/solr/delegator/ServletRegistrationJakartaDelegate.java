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

import jakarta.servlet.ServletRegistration;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServletRegistrationJakartaDelegate implements ServletRegistration {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  final javax.servlet.ServletRegistration delegate;

  ServletRegistrationJakartaDelegate(final javax.servlet.ServletRegistration registration) {
    delegate = registration;
  }

  @Override
  public Set<String> addMapping(String... urlPatterns) {
    return delegate.addMapping(urlPatterns);
  }

  @Override
  public Collection<String> getMappings() {
    return delegate.getMappings();
  }

  @Override
  public String getRunAsRole() {
    return delegate.getRunAsRole();
  }

  @Override
  public String getName() {
    return delegate.getName();
  }

  @Override
  public String getClassName() {
    log.warn("Critical delegate method called.");
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
