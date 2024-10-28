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
package org.apache.solr.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.solr.BaseDistributedSearchTestCase;
import org.apache.solr.SolrTestCase;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.Test;

public class TestNamedListCodec extends SolrTestCase {
  @Test
  public void testSimple() throws Exception {

    NamedList<Object> nl = new NamedList<>();
    Float fval = 10.01f;
    Boolean bval = Boolean.TRUE;
    String sval = "12qwaszx";

    // Set up a simple document
    NamedList<Object> r = new NamedList<>();

    nl.add("responseHeader", r);

    r.add("status", 0);
    r.add("QTime", 63);
    NamedList<Object> p = new NamedList<>();
    r.add("params", p);
    p.add("rows", 10);
    p.add("start", 0);
    p.add("indent", "on");
    p.add("q", "ipod");

    SolrDocumentList list = new SolrDocumentList();
    nl.add("response", list);
    list.setMaxScore(1.0f);
    list.setStart(10);
    list.setNumFound(12);

    SolrDocument doc = new SolrDocument();
    doc.addField("f", fval);
    doc.addField("b", bval);
    doc.addField("s", sval);
    doc.addField("f", 100);
    list.add(doc);

    doc = new SolrDocument();
    doc.addField("f", fval);
    doc.addField("b", bval);
    doc.addField("s", sval);
    doc.addField("f", 101);
    list.add(doc);

    nl.add("zzz", doc);
    byte[] arr;
    try (JavaBinCodec jbc = new JavaBinCodec(null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      jbc.marshal(nl, baos);
      arr = baos.toByteArray();
    }
    try (JavaBinCodec jbc = new JavaBinCodec();
        ByteArrayInputStream bais = new ByteArrayInputStream(arr)) {
      NamedList<?> res = (NamedList<?>) jbc.unmarshal(bais);
      assertEquals(3, res.size());
      assertEquals("ipod", ((NamedList<?>) ((NamedList<?>) res.getVal(0)).get("params")).get("q"));
      list = (SolrDocumentList) res.getVal(1);
      assertEquals(12, list.getNumFound());
      assertEquals(10, list.getStart());
      assertEquals(101, ((List) list.get(1).getFieldValue("f")).get(1));
    }
  }

  @Test
  public void testIterator() throws Exception {

    NamedList<Object> nl = new NamedList<>();
    Float fval = 10.01f;
    Boolean bval = Boolean.TRUE;
    String sval = "12qwaszx";

    // Set up a simple document
    List<SolrDocument> list = new ArrayList<>();

    SolrDocument doc = new SolrDocument();
    doc.addField("f", fval);
    doc.addField("b", bval);
    doc.addField("s", sval);
    doc.addField("f", 100);
    list.add(doc);

    doc = new SolrDocument();
    doc.addField("f", fval);
    doc.addField("b", bval);
    doc.addField("s", sval);
    doc.addField("f", 101);
    list.add(doc);

    nl.add("zzz", list.iterator());
    byte[] arr;
    try (JavaBinCodec jbc = new JavaBinCodec(null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      jbc.marshal(nl, baos);
      arr = baos.toByteArray();
    }
    try (JavaBinCodec jbc = new JavaBinCodec();
        ByteArrayInputStream bais = new ByteArrayInputStream(arr)) {
      NamedList<?> res = (NamedList<?>) jbc.unmarshal(bais);
      List<?> l = (List<?>) res.get("zzz");
      assertEquals(list.size(), l.size());
    }
  }

  @Test
  public void testIterable() throws Exception {
    NamedList<Object> r = new NamedList<>();

    Map<String, String> map = new HashMap<>();
    map.put("foo", "bar");
    map.put("junk", "funk");
    map.put("ham", "burger");

    r.add("keys", map.keySet());
    r.add("more", "less");
    r.add("values", map.values());
    r.add("finally", "the end");
    byte[] arr;
    try (JavaBinCodec jbc = new JavaBinCodec(null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      jbc.marshal(r, baos);
      arr = baos.toByteArray();
    }

    try (JavaBinCodec jbc = new JavaBinCodec();
        ByteArrayInputStream bais = new ByteArrayInputStream(arr)) {
      NamedList<?> result = (NamedList<?>) jbc.unmarshal(bais);
      assertNotNull("result is null and it shouldn't be", result);
      List<?> keys = (List<?>) result.get("keys");
      assertNotNull("keys is null and it shouldn't be", keys);
      assertEquals("keys Size: " + keys.size() + " is not: " + 3, 3, keys.size());
      String less = (String) result.get("more");
      assertNotNull("less is null and it shouldn't be", less);
      assertEquals(less + " is not equal to " + "less", "less", less);
      List<?> values = (List<?>) result.get("values");
      assertNotNull("values is null and it shouldn't be", values);
      assertEquals("values Size: " + values.size() + " is not: " + 3, 3, values.size());
      String theEnd = (String) result.get("finally");
      assertNotNull("theEnd is null and it shouldn't be", theEnd);
      assertEquals(theEnd + " is not equal to " + "the end", "the end", theEnd);
    } catch (ClassCastException e) {
      fail("Received a CCE and we shouldn't have");
    }
  }

  int rSz(int orderOfMagnitude) {
    int sz = r.nextInt(orderOfMagnitude);
    return switch (sz) {
      case 0 -> r.nextInt(10);
      case 1 -> r.nextInt(100);
      case 2 -> r.nextInt(1000);
      default -> r.nextInt(10000);
    };
  }

  public String rStr(int sz) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < sz; i++) {
      sb.appendCodePoint(r.nextInt(Character.MIN_HIGH_SURROGATE));
    }
    return sb.toString();
  }

  public NamedList<Object> rNamedList(int lev) {
    int sz = lev <= 0 ? 0 : r.nextInt(3);
    NamedList<Object> nl = new NamedList<>();
    for (int i = 0; i < sz; i++) {
      nl.add(rStr(2), makeRandom(lev - 1));
    }
    return nl;
  }

  public List<Object> rList(int lev) {
    int sz = lev <= 0 ? 0 : r.nextInt(3);
    ArrayList<Object> lst = new ArrayList<>();
    for (int i = 0; i < sz; i++) {
      lst.add(makeRandom(lev - 1));
    }
    return lst;
  }

  Random r;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    r = random();
  }

  public Object makeRandom(int lev) {
    return switch (r.nextInt(10)) {
      case 0 -> rList(lev);
      case 1 -> rNamedList(lev);
      case 2 -> rStr(rSz(4));
      case 3 -> r.nextInt();
      case 4 -> r.nextLong();
      case 5 -> r.nextBoolean();
      case 6 -> {
        byte[] arr = new byte[rSz(4)];
        r.nextBytes(arr);
        yield arr;
      }
      case 7 -> r.nextFloat();
      case 8 -> r.nextDouble();
      default -> null;
    };
  }

  @Test
  public void testRandom() throws Exception {
    // Random r = random;
    // let's keep it deterministic since just the wrong
    // random stuff could cause failure because of an OOM (too big)

    NamedList<Object> nl;
    NamedList<?> res;
    String cmp;

    for (int i = 0; i < 10000; i++) { // pump up the iterations for good stress testing
      nl = rNamedList(3);
      byte[] arr;
      try (JavaBinCodec jbc = new JavaBinCodec();
          ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        jbc.marshal(nl, baos);
        arr = baos.toByteArray();
      }
      // System.out.println(arr.length);
      try (JavaBinCodec jbc = new JavaBinCodec();
          ByteArrayInputStream bais = new ByteArrayInputStream(arr)) {
        res = (NamedList<?>) jbc.unmarshal(bais);
        cmp = BaseDistributedSearchTestCase.compare(nl, res, 0, null);
      }
      if (cmp != null) {
        System.out.println(nl);
        System.out.println(res);
        fail(cmp);
      }
    }
  }
}
