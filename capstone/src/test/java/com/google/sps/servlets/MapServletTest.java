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

import static com.google.sps.data.ProfileDatastoreUtil.BIO_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.IS_BUSINESS_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.LOCATION_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.LAT_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.LONG_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.NAME_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.NE_LAT_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.NE_LNG_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.NO;
import static com.google.sps.data.ProfileDatastoreUtil.PROFILE_TASK_NAME;
import static com.google.sps.data.ProfileDatastoreUtil.SW_LAT_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.SW_LNG_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.YES;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.sps.data.MapInfo;
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
  private static final double LAT_IN_BOUNDS = 37.386051;
  private static final double LONG_IN_BOUNDS = -122.083855;
  private static final double LAT_NOT_IN_BOUNDS = 40.730610;
  private static final double LONG_NOT_IN_BOUNDS = -73.935242;
  private static final String NOT_A_BUSINESS = NO;
  private static final String A_BUSINESS = YES;
  private static final String NAME = "Pizzeria";
  private static final String LOCATION_IN_BOUNDS = "Mountain View, CA, USA";
  private static final String LOCATION_NOT_IN_BOUNDS = "New York, NY";
  private static final String BIO = "This is my business bio.";
  private static final String SW_LAT = "37.2227223";
  private static final String SW_LNG = "-122.3033039";
  private static final String NE_LAT = "37.548271";
  private static final String NE_LNG = "-121.988571";

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
//   @Test
//   public void testEmptydoGet() throws Exception {
//     setRequestParams();

//     servlet.doGet(request, response);
//     Assert.assertEquals(servletResponseWriter.toString().replace("\n", ""), "[]");
//   }

  /*
   *  Test doGet() for response returning the correct list of businesses in the area given non businesses
   *  and other businesses in different areas.
   **/
  @Test
  public void testDoGetReturnCorrectList() throws Exception {
    setRequestParams();

    // This list will help in constructing the expected response.
    List<MapInfo> businesses = new ArrayList();

    Entity aBusinessInBounds = createBusiness(USER_ID_1);
    aBusinessInBounds.setProperty(IS_BUSINESS_PROPERTY, A_BUSINESS);
    aBusinessInBounds.setProperty(LOCATION_PROPERTY, LOCATION_IN_BOUNDS);
    aBusinessInBounds.setProperty(LAT_PROPERTY, LAT_IN_BOUNDS);
    aBusinessInBounds.setProperty(LONG_PROPERTY, LONG_IN_BOUNDS);
    datastore.put(aBusinessInBounds);

    Entity aBusinessNotInBounds = createBusiness(USER_ID_2);
    aBusinessNotInBounds.setProperty(IS_BUSINESS_PROPERTY, A_BUSINESS);
    aBusinessNotInBounds.setProperty(LOCATION_PROPERTY, LOCATION_NOT_IN_BOUNDS);
    aBusinessInBounds.setProperty(LAT_PROPERTY, LAT_NOT_IN_BOUNDS);
    aBusinessInBounds.setProperty(LONG_PROPERTY, LONG_NOT_IN_BOUNDS);
    datastore.put(aBusinessNotInBounds);

    Entity notABusiness = createNonBusiness(USER_ID_3);
    notABusiness.setProperty(IS_BUSINESS_PROPERTY, NOT_A_BUSINESS);
    datastore.put(notABusiness);

    MapInfo businessProfile =
        new MapInfo(USER_ID_1, NAME, LOCATION_IN_BOUNDS, LAT_IN_BOUNDS, LONG_IN_BOUNDS);
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
//   @Test
//   public void testDoGetReturnError() throws Exception {
//     servlet.doGet(request, response);

//     // verify if a sendError() was performed with the expected values.
//     Mockito.verify(response, Mockito.times(1))
//         .sendError(Mockito.eq(HttpServletResponse.SC_BAD_REQUEST), Mockito.anyString());
//   }

  // Create a business entity.
  private Entity createBusiness(String id) {
    Entity newBusiness = new Entity(PROFILE_TASK_NAME, id);
    newBusiness.setProperty(NAME_PROPERTY, NAME);

    return newBusiness;
  }

  // Create a non-business entity.
  private Entity createNonBusiness(String id) {
    Entity nonBusiness = new Entity(PROFILE_TASK_NAME, id);
    nonBusiness.setProperty(NAME_PROPERTY, NAME);
    nonBusiness.setProperty(LOCATION_PROPERTY, LOCATION_IN_BOUNDS);
    nonBusiness.setProperty(LAT_PROPERTY, LAT_IN_BOUNDS);
    nonBusiness.setProperty(LONG_PROPERTY, LONG_IN_BOUNDS);

    return nonBusiness;
  }

  // Set request parameters.
  private void setRequestParams() {
    when(request.getParameter(SW_LAT_PROPERTY)).thenReturn(SW_LAT);
    when(request.getParameter(SW_LNG_PROPERTY)).thenReturn(SW_LNG);
    when(request.getParameter(NE_LAT_PROPERTY)).thenReturn(NE_LAT);
    when(request.getParameter(NE_LNG_PROPERTY)).thenReturn(NE_LNG);
  }
}
