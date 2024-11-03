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

public class Delegates {

  public static jakarta.servlet.ServletRequest servletRequest(
      javax.servlet.ServletRequest request) {
    if (request instanceof ServletRequestJavaxDelegate) {
      return ((ServletRequestJavaxDelegate) request).delegate;
    } else {
      return new ServletRequestJakartaDelegate(request);
    }
  }

  public static javax.servlet.ServletRequest servletRequest(
      jakarta.servlet.ServletRequest request) {
    if (request instanceof ServletRequestJakartaDelegate) {
      return ((ServletRequestJakartaDelegate) request).delegate;
    } else {
      return new ServletRequestJavaxDelegate(request);
    }
  }

  public static jakarta.servlet.ServletResponse servletResponse(
      javax.servlet.ServletResponse response) {
    if (response instanceof ServletResponseJavaxDelegate) {
      return ((ServletResponseJavaxDelegate) response).delegate;
    } else {
      return new ServletResponseJakartaDelegate(response);
    }
  }

  public static javax.servlet.ServletResponse servletResponse(
      jakarta.servlet.ServletResponse response) {
    if (response instanceof ServletResponseJakartaDelegate) {
      return ((ServletResponseJakartaDelegate) response).delegate;
    } else {
      return new ServletResponseJavaxDelegate(response);
    }
  }

  public static jakarta.servlet.Filter filter(javax.servlet.Filter filter) {
    if (filter instanceof FilterJavaxDelegate) {
      return ((FilterJavaxDelegate) filter).delegate;
    } else {
      return new FilterJakartaDelegate(filter);
    }
  }

  public static javax.servlet.Filter filter(jakarta.servlet.Filter filter) {
    if (filter instanceof FilterJakartaDelegate) {
      return ((FilterJakartaDelegate) filter).delegate;
    } else {
      return new FilterJavaxDelegate(filter);
    }
  }

  public static jakarta.servlet.FilterChain filterChain(javax.servlet.FilterChain chain) {
    if (chain instanceof FilterChainJavaxDelegate) {
      return ((FilterChainJavaxDelegate) chain).delegate;
    } else {
      return new FilterChainJakartaDelegate(chain);
    }
  }

  public static javax.servlet.FilterChain filterChain(jakarta.servlet.FilterChain chain) {
    if (chain instanceof FilterChainJakartaDelegate) {
      return ((FilterChainJakartaDelegate) chain).delegate;
    } else {
      return new FilterChainJavaxDelegate(chain);
    }
  }

  public static jakarta.servlet.AsyncContext asyncContext(javax.servlet.AsyncContext context) {
    if (context instanceof AsyncContextJavaxDelegate) {
      return ((AsyncContextJavaxDelegate) context).delegate;
    } else {
      return new AsyncContextJakartaDelegate(context);
    }
  }

  public static javax.servlet.AsyncContext asyncContext(jakarta.servlet.AsyncContext context) {
    if (context instanceof AsyncContextJakartaDelegate) {
      return ((AsyncContextJakartaDelegate) context).delegate;
    } else {
      return new AsyncContextJavaxDelegate(context);
    }
  }

  public static jakarta.servlet.ServletContext servletContext(
      javax.servlet.ServletContext context) {
    if (context instanceof ServletContextJavaxDelegate) {
      return ((ServletContextJavaxDelegate) context).delegate;
    } else {
      return new ServletContextJakartaDelegate(context);
    }
  }

  public static javax.servlet.ServletContext servletContext(
      jakarta.servlet.ServletContext context) {
    if (context instanceof ServletContextJakartaDelegate) {
      return ((ServletContextJakartaDelegate) context).delegate;
    } else {
      return new ServletContextJavaxDelegate(context);
    }
  }

  public static jakarta.servlet.AsyncListener asyncListener(javax.servlet.AsyncListener listener) {
    if (listener instanceof AsyncListenerJavaxDelegate) {
      return ((AsyncListenerJavaxDelegate) listener).delegate;
    } else {
      return new AsyncListenerJakartaDelegate(listener);
    }
  }

  public static javax.servlet.AsyncListener asyncListener(jakarta.servlet.AsyncListener listener) {
    if (listener instanceof AsyncListenerJakartaDelegate) {
      return ((AsyncListenerJakartaDelegate) listener).delegate;
    } else {
      return new AsyncListenerJavaxDelegate(listener);
    }
  }

  public static jakarta.servlet.AsyncEvent asyncEvent(javax.servlet.AsyncEvent event) {
    if (event instanceof AsyncEventJavaxDelegate) {
      return ((AsyncEventJavaxDelegate) event).delegate;
    } else {
      return new AsyncEventJakartaDelegate(event);
    }
  }

  public static javax.servlet.AsyncEvent asyncEvent(jakarta.servlet.AsyncEvent event) {
    if (event instanceof AsyncEventJakartaDelegate) {
      return ((AsyncEventJakartaDelegate) event).delegate;
    } else {
      return new AsyncEventJavaxDelegate(event);
    }
  }

  public static jakarta.servlet.FilterConfig filterConfig(javax.servlet.FilterConfig config) {
    if (config instanceof FilterConfigJavaxDelegate) {
      return ((FilterConfigJavaxDelegate) config).delegate;
    } else {
      return new FilterConfigJakartaDelegate(config);
    }
  }

  public static javax.servlet.FilterConfig filterConfig(jakarta.servlet.FilterConfig config) {
    if (config instanceof FilterConfigJakartaDelegate) {
      return ((FilterConfigJakartaDelegate) config).delegate;
    } else {
      return new FilterConfigJavaxDelegate(config);
    }
  }

  public static jakarta.servlet.ServletInputStream servletInputStream(
      javax.servlet.ServletInputStream inputStream) {
    if (inputStream instanceof ServletInputStreamJavaxDelegate) {
      return ((ServletInputStreamJavaxDelegate) inputStream).delegate;
    } else {
      return new ServletInputStreamJakartaDelegate(inputStream);
    }
  }

  public static javax.servlet.ServletInputStream servletInputStream(
      jakarta.servlet.ServletInputStream inputStream) {
    if (inputStream instanceof ServletInputStreamJakartaDelegate) {
      return ((ServletInputStreamJakartaDelegate) inputStream).delegate;
    } else {
      return new ServletInputStreamJavaxDelegate(inputStream);
    }
  }

  public static jakarta.servlet.ServletOutputStream servletOutputStream(
      javax.servlet.ServletOutputStream outputStream) {
    if (outputStream instanceof ServletOutputStreamJavaxDelegate) {
      return ((ServletOutputStreamJavaxDelegate) outputStream).delegate;
    } else {
      return new ServletOutputStreamJakartaDelegate(outputStream);
    }
  }

  public static javax.servlet.ServletOutputStream servletOutputStream(
      jakarta.servlet.ServletOutputStream outputStream) {
    if (outputStream instanceof ServletOutputStreamJakartaDelegate) {
      return ((ServletOutputStreamJakartaDelegate) outputStream).delegate;
    } else {
      return new ServletOutputStreamJavaxDelegate(outputStream);
    }
  }

  public static jakarta.servlet.ReadListener readListener(javax.servlet.ReadListener listener) {
    if (listener instanceof ReadListenerJavaxDelegate) {
      return ((ReadListenerJavaxDelegate) listener).delegate;
    } else {
      return new ReadListenerJakartaDelegate(listener);
    }
  }

  public static javax.servlet.ReadListener readListener(jakarta.servlet.ReadListener listener) {
    if (listener instanceof ReadListenerJakartaDelegate) {
      return ((ReadListenerJakartaDelegate) listener).delegate;
    } else {
      return new ReadListenerJavaxDelegate(listener);
    }
  }

  public static jakarta.servlet.WriteListener writeListener(javax.servlet.WriteListener listener) {
    if (listener instanceof WriteListenerJavaxDelegate) {
      return ((WriteListenerJavaxDelegate) listener).delegate;
    } else {
      return new WriteListenerJakartaDelegate(listener);
    }
  }

  public static javax.servlet.WriteListener writeListener(jakarta.servlet.WriteListener listener) {
    if (listener instanceof WriteListenerJakartaDelegate) {
      return ((WriteListenerJakartaDelegate) listener).delegate;
    } else {
      return new WriteListenerJavaxDelegate(listener);
    }
  }

  public static jakarta.servlet.RequestDispatcher requestDispatcher(
      javax.servlet.RequestDispatcher dispatcher) {
    if (dispatcher instanceof RequestDispatcherJavaxDelegate) {
      return ((RequestDispatcherJavaxDelegate) dispatcher).delegate;
    } else {
      return new RequestDispatcherJakartaDelegate(dispatcher);
    }
  }

  public static javax.servlet.RequestDispatcher requestDispatcher(
      jakarta.servlet.RequestDispatcher dispatcher) {
    if (dispatcher instanceof RequestDispatcherJakartaDelegate) {
      return ((RequestDispatcherJakartaDelegate) dispatcher).delegate;
    } else {
      return new RequestDispatcherJavaxDelegate(dispatcher);
    }
  }

  @SuppressWarnings("qualified-case-label")
  public static jakarta.servlet.DispatcherType dispatcherType(javax.servlet.DispatcherType type) {
    return switch (type) {
      case FORWARD -> jakarta.servlet.DispatcherType.FORWARD;
      case INCLUDE -> jakarta.servlet.DispatcherType.INCLUDE;
      case REQUEST -> jakarta.servlet.DispatcherType.REQUEST;
      case ASYNC -> jakarta.servlet.DispatcherType.ASYNC;
      case ERROR -> jakarta.servlet.DispatcherType.ERROR;
    };
  }

  @SuppressWarnings("qualified-case-label")
  public static javax.servlet.DispatcherType dispatcherType(jakarta.servlet.DispatcherType type) {
    return switch (type) {
      case FORWARD -> javax.servlet.DispatcherType.FORWARD;
      case INCLUDE -> javax.servlet.DispatcherType.INCLUDE;
      case REQUEST -> javax.servlet.DispatcherType.REQUEST;
      case ASYNC -> javax.servlet.DispatcherType.ASYNC;
      case ERROR -> javax.servlet.DispatcherType.ERROR;
    };
  }

  public static jakarta.servlet.Servlet servlet(javax.servlet.Servlet servlet) {
    if (servlet instanceof ServletJavaxDelegate) {
      return ((ServletJavaxDelegate) servlet).delegate;
    } else {
      return new ServletJakartaDelegate(servlet);
    }
  }

  public static javax.servlet.Servlet servlet(jakarta.servlet.Servlet servlet) {
    if (servlet instanceof ServletJakartaDelegate) {
      return ((ServletJakartaDelegate) servlet).delegate;
    } else {
      return new ServletJavaxDelegate(servlet);
    }
  }

  public static jakarta.servlet.ServletConfig servletConfig(javax.servlet.ServletConfig config) {
    if (config instanceof ServletConfigJavaxDelegate) {
      return ((ServletConfigJavaxDelegate) config).delegate;
    } else {
      return new ServletConfigJakartaDelegate(config);
    }
  }

  public static javax.servlet.ServletConfig servletConfig(jakarta.servlet.ServletConfig config) {
    if (config instanceof ServletConfigJakartaDelegate) {
      return ((ServletConfigJakartaDelegate) config).delegate;
    } else {
      return new ServletConfigJavaxDelegate(config);
    }
  }

  public static jakarta.servlet.SessionTrackingMode sessionTrackingMode(
      javax.servlet.SessionTrackingMode mode) {
    return switch (mode) {
      case COOKIE -> jakarta.servlet.SessionTrackingMode.COOKIE;
      case URL -> jakarta.servlet.SessionTrackingMode.URL;
      case SSL -> jakarta.servlet.SessionTrackingMode.SSL;
    };
  }

  public static javax.servlet.SessionTrackingMode sessionTrackingMode(
      jakarta.servlet.SessionTrackingMode mode) {
    return switch (mode) {
      case COOKIE -> javax.servlet.SessionTrackingMode.COOKIE;
      case URL -> javax.servlet.SessionTrackingMode.URL;
      case SSL -> javax.servlet.SessionTrackingMode.SSL;
    };
  }

  public static jakarta.servlet.SessionCookieConfig sessionCookieConfig(
      javax.servlet.SessionCookieConfig config) {
    if (config instanceof SessionCookieConfigJavaxDelegate) {
      return ((SessionCookieConfigJavaxDelegate) config).delegate;
    } else {
      return new SessionCookieConfigJakartaDelegate(config);
    }
  }

  public static javax.servlet.SessionCookieConfig sessionCookieConfig(
      jakarta.servlet.SessionCookieConfig config) {
    if (config instanceof SessionCookieConfigJakartaDelegate) {
      return ((SessionCookieConfigJakartaDelegate) config).delegate;
    } else {
      return new SessionCookieConfigJavaxDelegate(config);
    }
  }

  public static jakarta.servlet.ServletRegistration servletRegistration(
      javax.servlet.ServletRegistration registration) {
    if (registration instanceof ServletRegistrationJavaxDelegate) {
      return ((ServletRegistrationJavaxDelegate) registration).delegate;
    } else {
      return new ServletRegistrationJakartaDelegate(registration);
    }
  }

  public static javax.servlet.ServletRegistration servletRegistration(
      jakarta.servlet.ServletRegistration registration) {
    if (registration instanceof ServletRegistrationJakartaDelegate) {
      return ((ServletRegistrationJakartaDelegate) registration).delegate;
    } else {
      return new ServletRegistrationJavaxDelegate(registration);
    }
  }

  public static jakarta.servlet.ServletRegistration.Dynamic dynamicServletRegistration(
      javax.servlet.ServletRegistration.Dynamic registration) {
    if (registration instanceof ServletRegistrationDynamicJavaxDelegate) {
      return ((ServletRegistrationDynamicJavaxDelegate) registration).delegate;
    } else {
      return new ServletRegistrationDynamicJakartaDelegate(registration);
    }
  }

  public static javax.servlet.ServletRegistration.Dynamic dynamicServletRegistration(
      jakarta.servlet.ServletRegistration.Dynamic registration) {
    if (registration instanceof ServletRegistrationDynamicJakartaDelegate) {
      return ((ServletRegistrationDynamicJakartaDelegate) registration).delegate;
    } else {
      return new ServletRegistrationDynamicJavaxDelegate(registration);
    }
  }

  public static jakarta.servlet.FilterRegistration.Dynamic dynamicFilterRegistration(
      javax.servlet.FilterRegistration.Dynamic registration) {
    if (registration instanceof FilterRegistrationDynamicJavaxDelegate) {
      return ((FilterRegistrationDynamicJavaxDelegate) registration).delegate;
    } else {
      return new FilterRegistrationDynamicJakartaDelegate(registration);
    }
  }

  public static javax.servlet.FilterRegistration.Dynamic dynamicFilterRegistration(
      jakarta.servlet.FilterRegistration.Dynamic registration) {
    if (registration instanceof FilterRegistrationDynamicJakartaDelegate) {
      return ((FilterRegistrationDynamicJakartaDelegate) registration).delegate;
    } else {
      return new FilterRegistrationDynamicJavaxDelegate(registration);
    }
  }

  public static jakarta.servlet.ServletSecurityElement servletSecurityElement(
      javax.servlet.ServletSecurityElement element) {
    if (element instanceof ServletSecurityElementJavaxDelegate) {
      return ((ServletSecurityElementJavaxDelegate) element).delegate;
    } else {
      return new ServletSecurityElementJakartaDelegate(element);
    }
  }

  public static javax.servlet.ServletSecurityElement servletSecurityElement(
      jakarta.servlet.ServletSecurityElement element) {
    if (element instanceof ServletSecurityElementJakartaDelegate) {
      return ((ServletSecurityElementJakartaDelegate) element).delegate;
    } else {
      return new ServletSecurityElementJavaxDelegate(element);
    }
  }

  public static jakarta.servlet.descriptor.JspConfigDescriptor jspConfigDescriptor(
      javax.servlet.descriptor.JspConfigDescriptor descriptor) {
    if (descriptor instanceof JspConfigDescriptorJavaxDelegate) {
      return ((JspConfigDescriptorJavaxDelegate) descriptor).delegate;
    } else {
      return new JspConfigDescriptorJakartaDelegate(descriptor);
    }
  }

  public static javax.servlet.descriptor.JspConfigDescriptor jspConfigDescriptor(
      jakarta.servlet.descriptor.JspConfigDescriptor descriptor) {
    if (descriptor instanceof JspConfigDescriptorJakartaDelegate) {
      return ((JspConfigDescriptorJakartaDelegate) descriptor).delegate;
    } else {
      return new JspConfigDescriptorJavaxDelegate(descriptor);
    }
  }

  public static jakarta.servlet.FilterRegistration filterRegistration(
      javax.servlet.FilterRegistration registration) {
    if (registration instanceof FilterRegistrationJavaxDelegate) {
      return ((FilterRegistrationJavaxDelegate) registration).delegate;
    } else {
      return new FilterRegistrationJakartaDelegate(registration);
    }
  }

  public static javax.servlet.FilterRegistration filterRegistration(
      jakarta.servlet.FilterRegistration registration) {
    if (registration instanceof FilterRegistrationJakartaDelegate) {
      return ((FilterRegistrationJakartaDelegate) registration).delegate;
    } else {
      return new FilterRegistrationJavaxDelegate(registration);
    }
  }

  public static jakarta.servlet.MultipartConfigElement multipartConfigElement(
      javax.servlet.MultipartConfigElement element) {
    if (element instanceof MultipartConfigElementJavaxDelegate) {
      return ((MultipartConfigElementJavaxDelegate) element).delegate;
    } else {
      return new MultipartConfigElementJakartaDelegate(element);
    }
  }

  public static javax.servlet.MultipartConfigElement multipartConfigElement(
      jakarta.servlet.MultipartConfigElement element) {
    if (element instanceof MultipartConfigElementJakartaDelegate) {
      return ((MultipartConfigElementJakartaDelegate) element).delegate;
    } else {
      return new MultipartConfigElementJavaxDelegate(element);
    }
  }

  public static jakarta.servlet.descriptor.TaglibDescriptor taglibDescriptor(
      javax.servlet.descriptor.TaglibDescriptor descriptor) {
    if (descriptor instanceof TaglibDescriptorJavaxDelegate) {
      return ((TaglibDescriptorJavaxDelegate) descriptor).delegate;
    } else {
      return new TaglibDescriptorJakartaDelegate(descriptor);
    }
  }

  public static javax.servlet.descriptor.TaglibDescriptor taglibDescriptor(
      jakarta.servlet.descriptor.TaglibDescriptor descriptor) {
    if (descriptor instanceof TaglibDescriptorJakartaDelegate) {
      return ((TaglibDescriptorJakartaDelegate) descriptor).delegate;
    } else {
      return new TaglibDescriptorJavaxDelegate(descriptor);
    }
  }

  public static jakarta.servlet.descriptor.JspPropertyGroupDescriptor jspPropertyGroupDescriptor(
      javax.servlet.descriptor.JspPropertyGroupDescriptor descriptor) {
    if (descriptor instanceof JspPropertyGroupDescriptorJavaxDelegate) {
      return ((JspPropertyGroupDescriptorJavaxDelegate) descriptor).delegate;
    } else {
      return new JspPropertyGroupDescriptorJakartaDelegate(descriptor);
    }
  }

  public static javax.servlet.descriptor.JspPropertyGroupDescriptor jspPropertyGroupDescriptor(
      jakarta.servlet.descriptor.JspPropertyGroupDescriptor descriptor) {
    if (descriptor instanceof JspPropertyGroupDescriptorJakartaDelegate) {
      return ((JspPropertyGroupDescriptorJakartaDelegate) descriptor).delegate;
    } else {
      return new JspPropertyGroupDescriptorJavaxDelegate(descriptor);
    }
  }

  public static jakarta.servlet.HttpMethodConstraintElement httpMethodConstraintElement(
      javax.servlet.HttpMethodConstraintElement element) {
    if (element instanceof HttpMethodConstraintElementJavaxDelegate) {
      return ((HttpMethodConstraintElementJavaxDelegate) element).delegate;
    } else {
      return new HttpMethodConstraintElementJakartaDelegate(element);
    }
  }

  public static javax.servlet.HttpMethodConstraintElement httpMethodConstraintElement(
      jakarta.servlet.HttpMethodConstraintElement element) {
    if (element instanceof HttpMethodConstraintElementJakartaDelegate) {
      return ((HttpMethodConstraintElementJakartaDelegate) element).delegate;
    } else {
      return new HttpMethodConstraintElementJavaxDelegate(element);
    }
  }

  public static jakarta.servlet.annotation.ServletSecurity.EmptyRoleSemantic emptyRoleSemantic(
      javax.servlet.annotation.ServletSecurity.EmptyRoleSemantic semantic) {
    return switch (semantic) {
      case DENY -> jakarta.servlet.annotation.ServletSecurity.EmptyRoleSemantic.DENY;
      case PERMIT -> jakarta.servlet.annotation.ServletSecurity.EmptyRoleSemantic.PERMIT;
      default -> null;
    };
  }

  public static javax.servlet.annotation.ServletSecurity.EmptyRoleSemantic emptyRoleSemantic(
      jakarta.servlet.annotation.ServletSecurity.EmptyRoleSemantic guarantee) {
    return switch (guarantee) {
      case DENY -> javax.servlet.annotation.ServletSecurity.EmptyRoleSemantic.DENY;
      case PERMIT -> javax.servlet.annotation.ServletSecurity.EmptyRoleSemantic.PERMIT;
      default -> null;
    };
  }

  public static jakarta.servlet.annotation.ServletSecurity.TransportGuarantee transportGuarantee(
      javax.servlet.annotation.ServletSecurity.TransportGuarantee guarantee) {
    return switch (guarantee) {
      case NONE -> jakarta.servlet.annotation.ServletSecurity.TransportGuarantee.NONE;
      case CONFIDENTIAL -> jakarta.servlet.annotation.ServletSecurity.TransportGuarantee
          .CONFIDENTIAL;
      default -> null;
    };
  }

  public static javax.servlet.annotation.ServletSecurity.TransportGuarantee transportGuarantee(
      jakarta.servlet.annotation.ServletSecurity.TransportGuarantee guarantee) {
    return switch (guarantee) {
      case NONE -> javax.servlet.annotation.ServletSecurity.TransportGuarantee.NONE;
      case CONFIDENTIAL -> javax.servlet.annotation.ServletSecurity.TransportGuarantee.CONFIDENTIAL;
      default -> null;
    };
  }
}
