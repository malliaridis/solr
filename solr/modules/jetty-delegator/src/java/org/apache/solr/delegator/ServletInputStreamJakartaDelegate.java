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

import static org.apache.solr.delegator.Delegates.readListener;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import java.io.IOException;

class ServletInputStreamJakartaDelegate extends ServletInputStream {

  final javax.servlet.ServletInputStream delegate;

  ServletInputStreamJakartaDelegate(final javax.servlet.ServletInputStream inputStream) {
    delegate = inputStream;
  }

  @Override
  public boolean isFinished() {
    return delegate.isFinished();
  }

  @Override
  public boolean isReady() {
    return delegate.isReady();
  }

  @Override
  public void setReadListener(ReadListener readListener) {
    delegate.setReadListener(readListener(readListener));
  }

  @Override
  public int read() throws IOException {
    return delegate.read();
  }
}
