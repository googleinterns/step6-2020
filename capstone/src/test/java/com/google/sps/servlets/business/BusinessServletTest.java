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

import static com.google.sps.data.ProfileDatastoreUtil.ABOUT_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.BIO_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.CALENDAR_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.GEO_PT_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.IS_BUSINESS_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.LAT_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.LOCATION_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.LONG_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.NAME_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.NO;
import static com.google.sps.data.ProfileDatastoreUtil.PROFILE_TASK_NAME;
import static com.google.sps.data.ProfileDatastoreUtil.STORY_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.SUPPORT_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.YES;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.sps.data.BusinessProfile;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public class BusinessServletTest {

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  private static final String NAME = "Pizzeria";
  private static final String NO_NAME = null;
  private static final String LOCATION = "Mountain View, CA";
  private static final String LAT = "45.0";
  private static final String LONG = "45.0";
  private static final String BIO = "This is my business bio.";
  private static final String STORY = "The pandemic has affected my business in X many ways.";
  private static final String ABOUT = "Here is the Pizzeria's menu.";
  private static final String SUPPORT = "Please donate at X website.";
  private static final String USER_ID = "12345";
  private static final String WRONG_USER = "54321";
  private static final String INVALID_USER_ID = null;
  private static final String EMAIL = "abc@gmail.com";
  private static final String AUTHDOMAIN = "gmail.com";
  private static final String PATHINFO = "/12345";
  private static final String INVALID_PATHINFO = "/notANumber";

  private LocalServiceTestHelper helper;

  private BusinessServlet servlet;
  private DatastoreService datastore;
  private StringWriter servletResponseWriter;
  private GeoPt GEO_PT;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    helper =
        new LocalServiceTestHelper(
                new LocalUserServiceTestConfig(), new LocalDatastoreServiceTestConfig())
            .setEnvEmail(EMAIL)
            .setEnvAuthDomain(AUTHDOMAIN)
            .setEnvIsLoggedIn(true)
            .setEnvAttributes(
                new HashMap(
                    ImmutableMap.of(
                        "com.google.appengine.api.users.UserService.user_id_key", USER_ID)));
    helper.setUp();

    datastore = DatastoreServiceFactory.getDatastoreService();
    servletResponseWriter = new StringWriter();
    doReturn(new PrintWriter(servletResponseWriter)).when(response).getWriter();
    servlet = new BusinessServlet();
    GEO_PT = new GeoPt(Float.parseFloat(LAT), Float.parseFloat(LONG));
  }

  @After
  public void tearDown() throws Exception {
    helper.tearDown();
  }

  // Set static variables to entity.
  public Entity setUserProfileData() {
    Entity ent = new Entity(PROFILE_TASK_NAME, USER_ID);
    ent.setProperty(NAME_PROPERTY, NAME);
    ent.setProperty(LOCATION_PROPERTY, LOCATION);
    ent.setProperty(GEO_PT_PROPERTY, GEO_PT);
    ent.setProperty(BIO_PROPERTY, BIO);
    ent.setProperty(STORY_PROPERTY, STORY);
    ent.setProperty(ABOUT_PROPERTY, ABOUT);
    ent.setProperty(CALENDAR_PROPERTY, EMAIL);
    ent.setProperty(SUPPORT_PROPERTY, SUPPORT);

    return ent;
  }

  /** Test doGet() for when user enters an invalid URL param. It should return an error. */
  @Test
  public void invalidUrlParamReturnError() throws ServletException, IOException {
    when(request.getPathInfo()).thenReturn(INVALID_PATHINFO);

    servlet.doGet(request, response);
    // verify if a sendError() was performed with the expected values.
    Mockito.verify(response, Mockito.times(1))
        .sendError(Mockito.eq(HttpServletResponse.SC_NOT_FOUND), Mockito.anyString());
  }

  /**
   * Test doGet() for when datastore cannot find entity key. User does not exist in datastore. It
   * should return an error.
   */
  @Test
  public void userNotInDatastoreReturnError()
      throws ServletException, IOException, EntityNotFoundException {
    when(request.getPathInfo()).thenReturn(PATHINFO);

    // Populate the datastore with a business with the wrong target ID.
    Entity someBusiness = new Entity(PROFILE_TASK_NAME, WRONG_USER);
    datastore.put(someBusiness);

    servlet.doGet(request, response);

    // verify if a sendError() was performed with the expected values.
    Mockito.verify(response, Mockito.times(1))
        .sendError(Mockito.eq(HttpServletResponse.SC_NOT_FOUND), Mockito.anyString());
  }

  /** Test doGet() for when user is not a business owner, it should return a response error. */
  @Test
  public void nonBusinessUserReturnError()
      throws ServletException, IOException, EntityNotFoundException {
    when(request.getPathInfo()).thenReturn(PATHINFO);

    Entity fakeBusiness = setUserProfileData();
    fakeBusiness.setProperty(IS_BUSINESS_PROPERTY, NO);
    datastore.put(fakeBusiness);

    servlet.doGet(request, response);

    // verify if a sendError() was performed with the expected values.
    Mockito.verify(response, Mockito.times(1))
        .sendError(Mockito.eq(HttpServletResponse.SC_NOT_FOUND), Mockito.anyString());
  }

  /**
   * Test doGet() for when user a business owner, it should return a JSON string of business profile
   * page information.
   */
  @Test
  public void BusinessUserReturnJsonFile() throws ServletException, IOException {
    when(request.getPathInfo()).thenReturn(PATHINFO);

    Entity validBusiness = setUserProfileData();
    validBusiness.setProperty(IS_BUSINESS_PROPERTY, YES);
    datastore.put(validBusiness);

    servlet.doGet(request, response);

    BusinessProfile profile =
        new BusinessProfile(USER_ID, NAME, LOCATION, BIO, STORY, ABOUT, EMAIL, SUPPORT, true);

    String servletResponse = servletResponseWriter.toString();

    Gson gson = new Gson();
    String expectedResponse = gson.toJson(profile);

    JsonParser parser = new JsonParser();
    Assert.assertEquals(parser.parse(expectedResponse), parser.parse(servletResponse));
  }

  /**
   * Test doPost() for when the user does not exist and they want to edit a profile. It should
   * return error.
   */
  @Test
  public void notLoggedInUserEditProfileReturnError()
      throws ServletException, IOException, EntityNotFoundException {
    helper.setEnvIsLoggedIn(false);

    servlet.doPost(request, response);

    // verify if a sendError() was performed with the expected values.
    Mockito.verify(response, Mockito.times(1))
        .sendError(Mockito.eq(HttpServletResponse.SC_UNAUTHORIZED), Mockito.anyString());
  }

  /** Test doPost() for when the user did not fill out the name section. It should return error. */
  @Test
  public void editProfileNameNotFilledReturnError()
      throws ServletException, IOException, EntityNotFoundException {
    servlet.doPost(request, response);

    // verify if a sendError() was performed with the expected values.
    Mockito.verify(response, Mockito.times(1))
        .sendError(Mockito.eq(HttpServletResponse.SC_BAD_REQUEST), Mockito.anyString());
  }

  /**
   * Test doPost() for when user is editing their profile page, it should put correct information
   * into datastore.
   */
  @Test
  public void userEditProfileAddToDatastore() throws Exception {
    when(request.getParameter(IS_BUSINESS_PROPERTY)).thenReturn("Yes");
    when(request.getParameter(NAME_PROPERTY)).thenReturn(NAME);
    when(request.getParameter(LOCATION_PROPERTY)).thenReturn(LOCATION);
    when(request.getParameter(LAT_PROPERTY)).thenReturn(LAT);
    when(request.getParameter(LONG_PROPERTY)).thenReturn(LONG);
    when(request.getParameter(BIO_PROPERTY)).thenReturn(BIO);
    when(request.getParameter(STORY_PROPERTY)).thenReturn(STORY);
    when(request.getParameter(ABOUT_PROPERTY)).thenReturn(ABOUT);
    when(request.getParameter(CALENDAR_PROPERTY)).thenReturn(EMAIL);
    when(request.getParameter(SUPPORT_PROPERTY)).thenReturn(SUPPORT);

    servlet.doPost(request, response);

    String keyString = KeyFactory.createKeyString(PROFILE_TASK_NAME, USER_ID);
    Key userKey = KeyFactory.stringToKey(keyString);

    Entity capEntity = datastore.get(userKey);

    Assert.assertEquals(capEntity.getProperty(IS_BUSINESS_PROPERTY), YES);
    Assert.assertEquals(capEntity.getProperty(NAME_PROPERTY), NAME);
    Assert.assertEquals(capEntity.getProperty(LOCATION_PROPERTY), LOCATION);
    Assert.assertEquals(capEntity.getProperty(GEO_PT_PROPERTY), GEO_PT);
    Assert.assertEquals(capEntity.getProperty(BIO_PROPERTY), BIO);
    Assert.assertEquals(capEntity.getProperty(STORY_PROPERTY), STORY);
    Assert.assertEquals(capEntity.getProperty(ABOUT_PROPERTY), ABOUT);
    Assert.assertEquals(capEntity.getProperty(CALENDAR_PROPERTY), EMAIL);
    Assert.assertEquals(capEntity.getProperty(SUPPORT_PROPERTY), SUPPORT);
  }

  /**
   * Test doPost() for when user is editing their profile page, they decided to change to
   * non-business profile. Return error.
   */
  @Test
  public void nonBusinessUserEditProfileAddToDatastore() throws Exception {
    when(request.getParameter(IS_BUSINESS_PROPERTY)).thenReturn(NO);
    when(request.getParameter(NAME_PROPERTY)).thenReturn(NAME);

    servlet.doPost(request, response);

    // verify if a sendError() was performed with the expected values.
    Mockito.verify(response, Mockito.times(1))
        .sendError(Mockito.eq(HttpServletResponse.SC_FORBIDDEN), Mockito.anyString());
  }
}
