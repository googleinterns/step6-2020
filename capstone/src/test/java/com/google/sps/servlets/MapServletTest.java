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

import static com.google.sps.data.ProfileDatastoreUtil.PROFILE_TASK_NAME;
import static com.google.sps.data.ProfileDatastoreUtil.BIO_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.CALENDAR_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.GEO_PT_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.STORY_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.ABOUT_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.SUPPORT_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.IS_BUSINESS_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.LAT_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.LOCATION_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.LONG_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.NAME_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.NO;
import static com.google.sps.data.ProfileDatastoreUtil.PROFILE_TASK_NAME;
import static com.google.sps.data.ProfileDatastoreUtil.YES;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.sps.data.BusinessProfile;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/** Unit tests for MapServlet. */
public class MapServletTest {

  private static final String USER_ID_1 = "12345";
  private static final String USER_ID_2 = "6789";
  private static final String USER_ID_3 = "5555";
  private static final String LAT_IN_BOUNDS = "37.386051";
  private static final String LONG_IN_BOUNDS = "-122.083855";
  private static final String LAT_NOT_IN_BOUNDS = "40.730610";
  private static final String LONG_NOT_IN_BOUNDS = "-73.935242";
  private static final String NOT_A_BUSINESS = NO;
  private static final String A_BUSINESS = YES;
  private static final String NAME = "Pizzeria";
  private static final String LOCATION_IN_BOUNDS = "Mountain View, CA, USA";
  private static final String LOCATION_NOT_IN_BOUNDS = "New York, NY";
  private static final String BIO = "This is my business bio.";
  private static final String STORY = "The pandemic has affected my business in X many ways.";
  private static final String ABOUT = "Here is the Pizzeria's menu.";
  private static final String EMAIL = "email@business.biz";
  private static final String SUPPORT = "Please donate at X website.";
  private static final String PATHINFO = "map/37.2227223/-122.3033039/37.548271/-121.988571";
  private static final String INVALID_PATHINFO = "map";

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  private StringWriter servletResponseWriter;
  private MapServlet servlet;
  private DatastoreService datastore;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    helper.setUp();

    servletResponseWriter = new StringWriter();
    doReturn(new PrintWriter(servletResponseWriter)).when(response).getWriter();
    servlet = new MapServlet();
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  /*
   *  Test doGet() for response returning the correct empty list of businesses.
   **/
  @Test
  public void testEmptydoGet() throws Exception {
    when(request.getPathInfo()).thenReturn(PATHINFO);

    servlet.doGet(request, response);
    Assert.assertEquals(servletResponseWriter.toString().replace("\n", ""), "[]");
  }

  /*
   *  Test doGet() for response returning the correct list of businesses in the area given non businesses
   *  and other businesses in different areas.
   **/
  @Test
  public void testDoGetReturnCorrectList() throws Exception {
    when(request.getPathInfo()).thenReturn(PATHINFO);

    // This list will help in constructing the expected response.
    List<BusinessProfile> businesses = new ArrayList();

    Entity aBusinessInBounds = createBusiness(USER_ID_1);
    aBusinessInBounds.setProperty(IS_BUSINESS_PROPERTY, A_BUSINESS);
    aBusinessInBounds.setProperty(LOCATION_PROPERTY, LOCATION_IN_BOUNDS);
    aBusinessInBounds.setProperty(GEO_PT_PROPERTY, createGeoPt(LAT_IN_BOUNDS, LONG_IN_BOUNDS));
    datastore.put(aBusinessInBounds);

    Entity aBusinessNotInBounds = createBusiness(USER_ID_2);
    aBusinessNotInBounds.setProperty(IS_BUSINESS_PROPERTY, A_BUSINESS);
    aBusinessNotInBounds.setProperty(LOCATION_PROPERTY, LOCATION_NOT_IN_BOUNDS);
    aBusinessNotInBounds.setProperty(GEO_PT_PROPERTY, createGeoPt(LAT_NOT_IN_BOUNDS, LONG_NOT_IN_BOUNDS));
    datastore.put(aBusinessNotInBounds);

    Entity notABusiness = createNonBusiness(USER_ID_3);
    notABusiness.setProperty(IS_BUSINESS_PROPERTY, NOT_A_BUSINESS);
    datastore.put(notABusiness);

    BusinessProfile businessProfile =
        new BusinessProfile(USER_ID_1, NAME, LOCATION_IN_BOUNDS, BIO, STORY, ABOUT, EMAIL, SUPPORT, false);
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

  /*
   *  Test doGet() for response returning error because of invalid parameters.
   **/
  @Test
  public void testDoGetReturnError() throws Exception {
    when(request.getPathInfo()).thenReturn(INVALID_PATHINFO);

    servlet.doGet(request, response);

    // verify if a sendError() was performed with the expected values.
    Mockito.verify(response, Mockito.times(1))
        .sendError(Mockito.eq(HttpServletResponse.SC_NOT_FOUND), Mockito.anyString());
  }

  // Create a business entity.
  private Entity createBusiness(String id) {
    Entity newBusiness = new Entity(PROFILE_TASK_NAME, id);
    newBusiness.setProperty(NAME_PROPERTY, NAME);
    newBusiness.setProperty(BIO_PROPERTY, BIO);
    newBusiness.setProperty(STORY_PROPERTY, STORY);
    newBusiness.setProperty(ABOUT_PROPERTY, ABOUT);
    newBusiness.setProperty(CALENDAR_PROPERTY, EMAIL);
    newBusiness.setProperty(SUPPORT_PROPERTY, SUPPORT);

    return newBusiness;
  }

  // Create a geoPt.
  private GeoPt createGeoPt(String lat, String lng) {
    return new GeoPt(Float.parseFloat(lat), Float.parseFloat(lng));
  }

  // Create a non-business entity.
  private Entity createNonBusiness(String id) {
    Entity nonBusiness = new Entity(PROFILE_TASK_NAME, id);
    nonBusiness.setProperty(NAME_PROPERTY, NAME);
    nonBusiness.setProperty(LOCATION_PROPERTY, LOCATION_IN_BOUNDS);
    nonBusiness.setProperty(BIO_PROPERTY, BIO);
    nonBusiness.setProperty(GEO_PT_PROPERTY, createGeoPt(LAT_IN_BOUNDS, LONG_IN_BOUNDS));

    return nonBusiness;
  }
}