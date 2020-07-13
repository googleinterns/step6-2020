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
import com.google.sps.data.BusinessProfile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for BusinessesServlet. */
public class BusinessesServletTest {

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  private StringWriter servletResponseWriter;

  private BusinessesServlet servlet;

  private static final String USER_ID_1 = "12345";
  private static final String USER_ID_2 = "6789";
  private static final String NOT_A_BUSINESS = "No";
  private static final String A_BUSINESS = "Yes";
  private static final String NAME = "Pizzeria";
  private static final String LOCATION = "Mountain View, CA";
  private static final String BIO = "This is my business bio.";
  private static final String STORY = "The pandemic has affected my business in X many ways.";
  private static final String ABOUT = "Here is the Pizzeria's menu.";
  private static final String EMAIL = "email@business.biz";
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

  private Entity createBusiness(String id) {
    Entity newBusiness = new Entity("UserProfile", id);
    newBusiness.setProperty("name", NAME);
    newBusiness.setProperty("location", LOCATION);
    newBusiness.setProperty("bio", BIO);
    newBusiness.setProperty("story", STORY);
    newBusiness.setProperty("about", ABOUT);
    newBusiness.setProperty("calendarEmail", EMAIL);
    newBusiness.setProperty("support", SUPPORT);

    return newBusiness;
  }

  /*
   *  Test doGet() for response returning the correct empty list of businesses.
   **/
  @Test
  public void testEmptydoGet() throws IOException {
    servlet.doGet(request, response);
    Assert.assertEquals(servletResponseWriter.toString().replace("\n", ""), "[]");
  }

  /*
   *  Test doGet() for response returning the correct list of businesses.
   **/
  @Test
  public void testDoGetReturnCorrectList() throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // This list will help in constructing the expected response.
    List<BusinessProfile> businesses = new ArrayList();

    Entity aBusiness = createBusiness(USER_ID_1);
    aBusiness.setProperty("isBusiness", A_BUSINESS);
    datastore.put(aBusiness);

    Entity notABusiness = createBusiness(USER_ID_2);
    notABusiness.setProperty("isBusiness", NOT_A_BUSINESS);
    datastore.put(notABusiness);

    BusinessProfile businessProfile =
        new BusinessProfile(id, NAME, LOCATION, BIO, STORY, ABOUT, EMAIL, SUPPORT, false);
    businesses.add(businessProfile);

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
