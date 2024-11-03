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

import static org.apache.solr.delegator.Delegates.emptyRoleSemantic;
import static org.apache.solr.delegator.Delegates.transportGuarantee;

import java.util.Collection;
import javax.servlet.HttpMethodConstraintElement;
import javax.servlet.ServletSecurityElement;
import javax.servlet.annotation.ServletSecurity;

public class ServletSecurityElementJavaxDelegate extends ServletSecurityElement {

  final jakarta.servlet.ServletSecurityElement delegate;

  public ServletSecurityElementJavaxDelegate(jakarta.servlet.ServletSecurityElement element) {
    delegate = element;
  }

  @Override
  public Collection<HttpMethodConstraintElement> getHttpMethodConstraints() {
    return delegate.getHttpMethodConstraints().stream()
        .map(Delegates::httpMethodConstraintElement)
        .toList();
  }

  @Override
  public Collection<String> getMethodNames() {
    return delegate.getMethodNames();
  }

  @Override
  public String[] getRolesAllowed() {
    return delegate.getRolesAllowed();
  }

  @Override
  public ServletSecurity.EmptyRoleSemantic getEmptyRoleSemantic() {
    return emptyRoleSemantic(delegate.getEmptyRoleSemantic());
  }

  @Override
  public ServletSecurity.TransportGuarantee getTransportGuarantee() {
    return transportGuarantee(delegate.getTransportGuarantee());
  }
}
