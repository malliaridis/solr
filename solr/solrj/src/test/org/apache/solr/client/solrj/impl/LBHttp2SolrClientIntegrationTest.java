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
package org.apache.solr.client.solrj.impl;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.lucene.util.IOUtils;
import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SolrResponseBase;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.TimeSources;
import org.apache.solr.embedded.JettyConfig;
import org.apache.solr.embedded.JettySolrRunner;
import org.apache.solr.util.TimeOut;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for LBHttp2SolrClient
 *
 * @since solr 1.4
 */
public class LBHttp2SolrClientIntegrationTest extends SolrTestCaseJ4 {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  SolrInstance[] solr = new SolrInstance[3];

  // TODO: fix this test to not require FSDirectory
  static String savedFactory;

  @BeforeClass
  public static void beforeClass() {
    savedFactory = System.getProperty("solr.DirectoryFactory");
    System.setProperty("solr.directoryFactory", "org.apache.solr.core.MockFSDirectoryFactory");
    System.setProperty("tests.shardhandler.randomSeed", Long.toString(random().nextLong()));
  }

  @AfterClass
  public static void afterClass() {
    if (savedFactory == null) {
      System.clearProperty("solr.directoryFactory");
    } else {
      System.setProperty("solr.directoryFactory", savedFactory);
    }
    System.clearProperty("tests.shardhandler.randomSeed");
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();

    for (int i = 0; i < solr.length; i++) {
      solr[i] =
          new SolrInstance("solr/collection1" + i, createTempDir("instance-" + i).toFile(), 0);
      solr[i].setUp();
      solr[i].startJetty();
      addDocs(solr[i]);
    }
  }

  private void addDocs(SolrInstance solrInstance) throws IOException, SolrServerException {
    List<SolrInputDocument> docs = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      SolrInputDocument doc = new SolrInputDocument();
      doc.addField("id", i);
      doc.addField("name", solrInstance.name);
      docs.add(doc);
    }
    SolrResponseBase resp;
    try (SolrClient client =
        getHttpSolrClient(solrInstance.getBaseUrl(), solrInstance.getDefaultCollection())) {
      resp = client.add(docs);
      assertEquals(0, resp.getStatus());
      resp = client.commit();
      assertEquals(0, resp.getStatus());
    }
  }

  @Override
  public void tearDown() throws Exception {
    for (SolrInstance aSolr : solr) {
      if (aSolr != null) {
        aSolr.tearDown();
      }
    }
    super.tearDown();
  }

  private LBClientHolder client(LBSolrClient.Endpoint... baseSolrEndpoints) {
    if (random().nextBoolean()) {
      var delegateClient =
          new Http2SolrClient.Builder()
              .withConnectionTimeout(1000, TimeUnit.MILLISECONDS)
              .withIdleTimeout(2000, TimeUnit.MILLISECONDS)
              .build();
      var lbClient =
          new LBHttp2SolrClient.Builder<>(delegateClient, baseSolrEndpoints)
              .withDefaultCollection(solr[0].getDefaultCollection())
              .setAliveCheckInterval(500, TimeUnit.MILLISECONDS)
              .build();
      return new LBClientHolder(lbClient, delegateClient);
    } else {
      var delegateClient =
          new HttpJdkSolrClient.Builder()
              .withConnectionTimeout(1000, TimeUnit.MILLISECONDS)
              .withIdleTimeout(2000, TimeUnit.MILLISECONDS)
              .withSSLContext(MockTrustManager.ALL_TRUSTING_SSL_CONTEXT)
              .build();
      var lbClient =
          new LBHttp2SolrClient.Builder<>(delegateClient, baseSolrEndpoints)
              .withDefaultCollection(solr[0].getDefaultCollection())
              .setAliveCheckInterval(500, TimeUnit.MILLISECONDS)
              .build();
      return new LBClientHolder(lbClient, delegateClient);
    }
  }

  public void testSimple() throws Exception {
    final var baseSolrEndpoints = bootstrapBaseSolrEndpoints(solr.length);
    try (var h = client(baseSolrEndpoints)) {
      SolrQuery solrQuery = new SolrQuery("*:*");
      Set<String> names = new HashSet<>();
      QueryResponse resp = null;
      for (int i = 0; i < solr.length; i++) {
        resp = h.lbClient.query(solrQuery);
        assertEquals(10, resp.getResults().getNumFound());
        names.add(resp.getResults().get(0).getFieldValue("name").toString());
      }
      assertEquals(3, names.size());

      // Kill a server and test again
      solr[1].jetty.stop();
      solr[1].jetty = null;
      names.clear();
      for (int i = 0; i < solr.length; i++) {
        resp = h.lbClient.query(solrQuery);
        assertEquals(10, resp.getResults().getNumFound());
        names.add(resp.getResults().get(0).getFieldValue("name").toString());
      }
      assertEquals(2, names.size());
      assertFalse(names.contains("solr1"));

      // Start the killed server once again
      solr[1].startJetty();
      // Wait for the alive check to complete
      Thread.sleep(1200);
      names.clear();
      for (int i = 0; i < solr.length; i++) {
        resp = h.lbClient.query(solrQuery);
        assertEquals(10, resp.getResults().getNumFound());
        names.add(resp.getResults().get(0).getFieldValue("name").toString());
      }
      assertEquals(3, names.size());
    }
  }

  public void testTwoServers() throws Exception {
    final var baseSolrEndpoints = bootstrapBaseSolrEndpoints(2);
    try (var h = client(baseSolrEndpoints)) {
      SolrQuery solrQuery = new SolrQuery("*:*");
      QueryResponse resp = null;
      solr[0].jetty.stop();
      solr[0].jetty = null;
      resp = h.lbClient.query(solrQuery);
      String name = resp.getResults().get(0).getFieldValue("name").toString();
      assertEquals("solr/collection11", name);
      resp = h.lbClient.query(solrQuery);
      name = resp.getResults().get(0).getFieldValue("name").toString();
      assertEquals("solr/collection11", name);
      solr[1].jetty.stop();
      solr[1].jetty = null;
      solr[0].startJetty();
      Thread.sleep(1200);
      try {
        resp = h.lbClient.query(solrQuery);
      } catch (SolrServerException e) {
        // try again after a pause in case the error is lack of time to start server
        Thread.sleep(3000);
        resp = h.lbClient.query(solrQuery);
      }
      name = resp.getResults().get(0).getFieldValue("name").toString();
      assertEquals("solr/collection10", name);
    }
  }

  public void testReliability() throws Exception {
    final var baseSolrEndpoints = bootstrapBaseSolrEndpoints(solr.length);
    try (var h = client(baseSolrEndpoints)) {

      // Kill a server and test again
      solr[1].jetty.stop();
      solr[1].jetty = null;

      // query the servers
      for (int i = 0; i < solr.length; i++) {
        h.lbClient.query(new SolrQuery("*:*"));
      }

      // Start the killed server once again
      solr[1].startJetty();
      // Wait for the alive check to complete
      waitForServer(30, h.lbClient, 3, solr[1].name);
    }
  }

  // wait maximum ms for serverName to come back up
  private void waitForServer(
      int maxSeconds, LBHttp2SolrClient<?> client, int nServers, String serverName)
      throws Exception {
    final TimeOut timeout = new TimeOut(maxSeconds, TimeUnit.SECONDS, TimeSources.NANO_TIME);
    while (!timeout.hasTimedOut()) {
      QueryResponse resp;
      try {
        resp = client.query(new SolrQuery("*:*"));
      } catch (Exception e) {
        log.warn("", e);
        continue;
      }
      String name = resp.getResults().get(0).getFieldValue("name").toString();
      if (name.equals(serverName)) return;

      Thread.sleep(500);
    }
  }

  private LBSolrClient.Endpoint[] bootstrapBaseSolrEndpoints(int max) {
    LBSolrClient.Endpoint[] solrUrls = new LBSolrClient.Endpoint[max];
    for (int i = 0; i < max; i++) {
      solrUrls[i] = new LBSolrClient.Endpoint(solr[i].getBaseUrl());
    }
    return solrUrls;
  }

  private static class SolrInstance {
    String name;
    File homeDir;
    File dataDir;
    File confDir;
    int port;
    JettySolrRunner jetty;

    public SolrInstance(String name, File homeDir, int port) {
      this.name = name;
      this.homeDir = homeDir;
      this.port = port;

      dataDir = new File(homeDir + "/collection1", "data");
      confDir = new File(homeDir + "/collection1", "conf");
    }

    public String getHomeDir() {
      return homeDir.toString();
    }

    public String getUrl() {
      return buildUrl(port) + "/collection1";
    }

    public String getBaseUrl() {
      return buildUrl(port);
    }

    public String getDefaultCollection() {
      return "collection1";
    }

    public String getSchemaFile() {
      return "solrj/solr/collection1/conf/schema-replication1.xml";
    }

    public String getConfDir() {
      return confDir.toString();
    }

    public String getDataDir() {
      return dataDir.toString();
    }

    public String getSolrConfigFile() {
      return "solrj/solr/collection1/conf/solrconfig-follower1.xml";
    }

    public String getSolrXmlFile() {
      return "solrj/solr/solr.xml";
    }

    public void setUp() throws Exception {
      homeDir.mkdirs();
      dataDir.mkdirs();
      confDir.mkdirs();

      Files.copy(
          SolrTestCaseJ4.getFile(getSolrXmlFile()).toPath(), homeDir.toPath().resolve("solr.xml"));

      Path f = confDir.toPath().resolve("solrconfig.xml");
      Files.copy(SolrTestCaseJ4.getFile(getSolrConfigFile()).toPath(), f);
      f = confDir.toPath().resolve("schema.xml");
      Files.copy(SolrTestCaseJ4.getFile(getSchemaFile()).toPath(), f);
      Files.createFile(homeDir.toPath().resolve("collection1/core.properties"));
    }

    public void tearDown() throws Exception {
      if (jetty != null) jetty.stop();
      IOUtils.rm(homeDir.toPath());
    }

    public void startJetty() throws Exception {

      Properties props = new Properties();
      props.setProperty("solrconfig", "bad_solrconfig.xml");
      props.setProperty("solr.data.dir", getDataDir());

      JettyConfig jettyConfig = JettyConfig.builder().setPort(port).build();

      jetty = new JettySolrRunner(getHomeDir(), props, jettyConfig);
      jetty.start();
      int newPort = jetty.getLocalPort();
      if (port != 0 && newPort != port) {
        fail("TESTING FAILURE: could not grab requested port.");
      }
      this.port = newPort;
    }
  }

  private static class LBClientHolder implements AutoCloseable {

    final LBHttp2SolrClient<?> lbClient;
    final HttpSolrClientBase delegate;

    LBClientHolder(LBHttp2SolrClient<?> lbClient, HttpSolrClientBase delegate) {
      this.lbClient = lbClient;
      this.delegate = delegate;
    }

    @Override
    public void close() {
      lbClient.close();
      try {
        delegate.close();
      } catch (IOException ioe) {
        throw new UncheckedIOException(ioe);
      }
    }
  }
}
