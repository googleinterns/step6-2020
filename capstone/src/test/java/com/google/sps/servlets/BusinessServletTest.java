// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import static org.mockito.Mockito.doReturn;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for BusinessServlet. */
public class BusinessServletTest {

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  private StringWriter servletResponseWriter;
  private BusinessServlet servlet;

  @Before
  public void setUp() throws IOException {
    MockitoAnnotations.initMocks(this);
    helper.setUp();

    servletResponseWriter = new StringWriter();
    doReturn(new PrintWriter(servletResponseWriter)).when(response).getWriter();
    servlet = new BusinessServlet();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void testEmptyDatastoredoGet() throws IOException {
    doReturn("/1").when(request).getPathInfo();
    servlet.doGet(request, response);
    // response = "\n" before replacing \n
    Assert.assertTrue(servletResponseWriter.toString().replace("\n", "").isEmpty());
  }

  @Test
  public void testBasicdoGet() throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity business1 = new Entity("Business");
    business1.setProperty("name", "Business 1");
    business1.setProperty("email", "work@b1.com");
    business1.setProperty("bio", "This is a bit about our business");
    business1.setProperty("location", "Mountain View, CA");
    datastore.put(business1);

    // Add an "id" property so that the expected response shows id as well.
    // servletResponse returns "id" from the BusinessProfile
    business1.setProperty("id", business1.getKey().getId());

    Entity business2 = new Entity("Business");
    business2.setProperty("name", "Business 2");
    business2.setProperty("email", "work@b2.com");
    business2.setProperty("bio", "This is a bit about our business");
    business2.setProperty("location", "New York City, NY");
    datastore.put(business2);

    // Return the path "/business/{business1 ID}".
    doReturn("/" + business1.getKey().getId()).when(request).getPathInfo();

    servlet.doGet(request, response);
    String servletResponse = servletResponseWriter.toString();
    Gson gson = new Gson();
    String expectedResponse = gson.toJson(business1.getProperties());

    // expectedResponse and servletResponse strings may differ in json property order.
    // JsonParser helps to compare two json strings regardless of property order.
    JsonParser parser = new JsonParser();
    Assert.assertEquals(parser.parse(servletResponse), parser.parse(expectedResponse));
  }

  @Test
  public void testInvalidId() throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity business1 = new Entity("Business");
    business1.setProperty("name", "Business 1");
    business1.setProperty("email", "work@b1.com");
    business1.setProperty("bio", "This is a bit about our business");
    business1.setProperty("location", "Mountain View, CA");
    datastore.put(business1);

    // Try to search for a business using an invalid/unregistered ID.
    doReturn("/" + (business1.getKey().getId() + 1)).when(request).getPathInfo();

    servlet.doGet(request, response);
    // servletResponse = "\n" before replacing \n.
    String servletResponse = servletResponseWriter.toString().replace("\n", "");
    Assert.assertTrue(servletResponse.isEmpty());
  }
}
