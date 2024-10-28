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

import static org.apache.solr.delegator.Delegates.asyncEvent;

import java.io.IOException;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;

class AsyncListenerJavaxDelegate implements AsyncListener {

  final jakarta.servlet.AsyncListener delegate;

  AsyncListenerJavaxDelegate(final jakarta.servlet.AsyncListener listener) {
    delegate = listener;
  }

  @Override
  public void onComplete(AsyncEvent event) throws IOException {
    delegate.onComplete(asyncEvent(event));
  }

  @Override
  public void onTimeout(AsyncEvent event) throws IOException {
    delegate.onTimeout(asyncEvent(event));
  }

  @Override
  public void onError(AsyncEvent event) throws IOException {
    delegate.onError(asyncEvent(event));
  }

  @Override
  public void onStartAsync(AsyncEvent event) throws IOException {
    delegate.onStartAsync(asyncEvent(event));
  }
}
