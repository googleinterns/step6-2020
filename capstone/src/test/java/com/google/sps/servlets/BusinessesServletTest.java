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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.sps.data.BusinessProfile;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/** Unit tests for BusinessesServlet. */
public class BusinessesServletTest {

  private LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalUserServiceTestConfig())
          .setEnvIsAdmin(true)
          .setEnvIsLoggedIn(false);

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  private StringWriter servletResponseWriter;

  private BusinessesServlet servlet;

  private static final String USER_ID_1 = "12345";
  private static final String USER_ID_2 = "6789";
  private static final String NAME = "Pizzeria";
  private static final String LOCATION = "Mountain View, CA";
  private static final String BIO = "This is my business bio.";
  private static final String STORY = "The pandemic has affected my business in X many ways.";
  private static final String ABOUT = "Here is the Pizzeria's menu.";
  private static final String SUPPORT = "Please donate at X website."; 

  @Before
  public void setUp() throws IOException {
    MockitoAnnotations.initMocks(this);
    helper.setUp();

    servletResponseWriter = new StringWriter();
    doReturn(new PrintWriter(servletResponseWriter)).when(response).getWriter();
    servlet = new BusinessesServlet();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  private Entity createBusiness(int businessNo, String id) {
    Entity newBusiness = new Entity("UserProfile", id);
    newBusiness.setProperty("name", NAME);
    newBusiness.setProperty("location", LOCATION);
    newBusiness.setProperty("bio", BIO);
    newBusiness.setProperty("story", STORY);
    newBusiness.setProperty("about", ABOUT);
    newBusiness.setProperty("support", SUPPORT);
    
    return newBusiness;
  }

  private BusinessProfile createBusinessProfile(String id) {
    return new BusinessProfile(id, NAME, LOCATION, BIO, STORY, ABOUT, SUPPORT, false);
  }

  @Test
  public void testEmptydoGet() throws IOException {
    servlet.doGet(request, response);
    Assert.assertEquals(servletResponseWriter.toString().replace("\n", ""), "[]");
  }

  @Test
  public void testBasicdoGet() throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    // This list will help in constructing the expected response.
    List<BusinessProfile> businesses = new ArrayList();

    Entity business1 = createBusiness(1, USER_ID_1);
    datastore.put(business1);

    Entity business2 = createBusiness(2, USER_ID_2);
    datastore.put(business2);

    BusinessProfile profile1 = createBusinessProfile(USER_ID_1);
    businesses.add(profile1);

    BusinessProfile profile2 = createBusinessProfile(USER_ID_2);
    businesses.add(profile2);

    servlet.doGet(request, response);

    String servletResponse = servletResponseWriter.toString();
    Gson gson = new Gson();
    String expectedResponse = gson.toJson(businesses);

    // expectedResponse and servletResponse strings may differ in json property order.
    // JsonParser helps to compare two json strings regardless of property order.
    JsonParser parser = new JsonParser();
    Assert.assertEquals(parser.parse(servletResponse), parser.parse(expectedResponse));
  }
}
