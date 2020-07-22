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

package com.google.sps.servlets.profile;

import static com.google.sps.data.ProfileDatastoreUtil.BIO_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.GEO_PT_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.IS_BUSINESS_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.LAT_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.LOCATION_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.LONG_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.NAME_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.NO;
import static com.google.sps.data.ProfileDatastoreUtil.PROFILE_TASK_NAME;
import static com.google.sps.data.ProfileDatastoreUtil.YES;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.sps.data.UserProfile;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
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
public class ProfileServletTest {

  private static final String NAME = "John Doe";
  private static final String NO_NAME = null;
  private static final String LOCATION = "Mountain View, CA";
  private static final String LAT = "45.0";
  private static final String LONG = "45.0";
  private static final String BIO = "This is my bio.";
  private static final String USER_ID = "12345";
  private static final String USER2_ID = "6789";
  private static final String INVALID_USER_ID = null;
  private static final String EMAIL = "abc@gmail.com";
  private static final String AUTHDOMAIN = "gmail.com";
  private static final String PATHINFO = "profile/12345";
  private static final String INVALID_PATHINFO = "profile";

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  private LocalServiceTestHelper helper;
  private ProfileServlet profileServlet;
  private DatastoreService datastore;
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
    profileServlet = new ProfileServlet();
    GEO_PT = new GeoPt(Float.parseFloat(LAT), Float.parseFloat(LONG));
  }

  @After
  public void tearDown() throws Exception {
    helper.tearDown();
  }

  /*
   *  Test doGet() for when user enters an invalid URL param. It should return an error.
   **/
  @Test
  public void invalidUrlParamReturnError() throws Exception {
    when(request.getPathInfo()).thenReturn(INVALID_PATHINFO);

    profileServlet.doGet(request, response);

    // verify if a sendError() was performed with the expected values.
    Mockito.verify(response, Mockito.times(1))
        .sendError(Mockito.eq(HttpServletResponse.SC_NOT_FOUND), Mockito.anyString());
  }

  /*
   *  Test doGet() for when datastore cannot find entity key. User does not exist in datastore.
   *  It should return an error.
   **/
  @Test
  public void userNotInDatastoreReturnError() throws Exception {
    when(request.getPathInfo()).thenReturn(PATHINFO);

    // Populate the datastore with a business with the wrong target ID.
    Entity someBusiness = new Entity(PROFILE_TASK_NAME, USER2_ID);
    datastore.put(someBusiness);

    profileServlet.doGet(request, response);

    // verify if a sendError() was performed with the expected values.
    Mockito.verify(response, Mockito.times(1))
        .sendError(Mockito.eq(HttpServletResponse.SC_NOT_FOUND), Mockito.anyString());
  }

  /*
   *  Test doGet() for when user is a business owner, it should return a response error.
   **/
  @Test
  public void businessUserReturnError() throws Exception {
    when(request.getPathInfo()).thenReturn(PATHINFO);

    // Create an entity with this USER_ID and set it's property "isBusiness" to YES.
    // Then add this to datastore.
    Entity ent = setBusinessEntityProperty();

    ent.setProperty(IS_BUSINESS_PROPERTY, YES);

    profileServlet.doGet(request, response);

    // verify if a sendError() was performed with the expected values.
    Mockito.verify(response, Mockito.times(1))
        .sendError(Mockito.eq(HttpServletResponse.SC_NOT_FOUND), Mockito.anyString());
  }

  /*
   *  Test doGet() for when user not a business owner, it should return a JSON file of profile page information.
   **/
  @Test
  public void nonBusinessUserReturnJsonFile() throws Exception {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);
    when(request.getPathInfo()).thenReturn(PATHINFO);

    // Create an entity with this USER_ID and set it's property "isBusiness" to NO.
    // Then add this to datastore.
    Key userKey = KeyFactory.createKey(PROFILE_TASK_NAME, USER_ID);
    Entity ent = setBusinessEntityProperty();

    ent.setProperty(IS_BUSINESS_PROPERTY, NO);

    boolean isCurrentUser = true;

    datastore.put(ent);

    profileServlet.doGet(request, response);

    // verify that it sends a JSON file to response.
    UserProfile profile = new UserProfile(USER_ID, NAME, LOCATION, BIO, isCurrentUser);

    String responseString = stringWriter.getBuffer().toString().trim();
    JsonElement responseJsonElement = new JsonParser().parse(responseString);

    Gson gson = new GsonBuilder().create();
    JsonElement userJsonElement = gson.toJsonTree(profile);

    JsonObject responseJsonObject = responseJsonElement.getAsJsonObject();
    JsonObject userJsonObject = userJsonElement.getAsJsonObject();

    Assert.assertEquals(responseJsonObject, userJsonObject);
  }

  /*
   *  Test doPost() for when the user does not exist and they want to edit a profile. It should return error.
   **/
  @Test
  public void editProfileUserNotFoundReturnError() throws Exception {
    helper =
        new LocalServiceTestHelper(
            new LocalUserServiceTestConfig(), new LocalDatastoreServiceTestConfig());
    helper.setUp();

    profileServlet.doPost(request, response);

    // verify if a sendError() was performed with the expected values.
    Mockito.verify(response, Mockito.times(1))
        .sendError(Mockito.eq(HttpServletResponse.SC_NOT_FOUND), Mockito.anyString());
  }

  /*
   *  Test doPost() for when the user did not fill out the name section. It should return error.
   **/
  @Test
  public void editProfileNameNotFilledReturnError() throws Exception {
    helper =
        new LocalServiceTestHelper(
            new LocalUserServiceTestConfig(), new LocalDatastoreServiceTestConfig());
    helper.setUp();

    when(request.getParameter(IS_BUSINESS_PROPERTY)).thenReturn(NO);
    when(request.getParameter(NAME_PROPERTY)).thenReturn(NO_NAME);
    setRequestParameters();

    Key userKey = KeyFactory.createKey(PROFILE_TASK_NAME, USER_ID);
    Entity ent = new Entity(PROFILE_TASK_NAME, USER_ID);

    profileServlet.doPost(request, response);

    // verify if a sendError() was performed with the expected values.
    Mockito.verify(response, Mockito.times(1))
        .sendError(Mockito.eq(HttpServletResponse.SC_NOT_FOUND), Mockito.anyString());
  }

  /*
   *  Test doPost() for when user is editing their profile page, it should put correct information into datastore.
   **/
  @Test
  public void userEditProfileAddToDatastore() throws Exception {
    when(request.getParameter(IS_BUSINESS_PROPERTY)).thenReturn(NO);
    when(request.getParameter(NAME_PROPERTY)).thenReturn(NAME);
    setRequestParameters();

    profileServlet.doPost(request, response);

    String keyString = KeyFactory.createKeyString(PROFILE_TASK_NAME, USER_ID);
    Key userKey = KeyFactory.stringToKey(keyString);

    Entity capEntity = datastore.get(userKey);

    Assert.assertEquals(capEntity.getProperty(IS_BUSINESS_PROPERTY), NO);
    Assert.assertEquals(capEntity.getProperty(NAME_PROPERTY), NAME);
    Assert.assertEquals(capEntity.getProperty(LOCATION_PROPERTY), LOCATION);
    Assert.assertEquals(capEntity.getProperty(GEO_PT_PROPERTY), GEO_PT);
    Assert.assertEquals(capEntity.getProperty(BIO_PROPERTY), BIO);
  }

  /*
   *  Test doPost() for when user is editing their profile page, they decided to change to business profile.
   *  Return error.
   **/
  @Test
  public void nonBusinessUserEditProfileAddToDatastore() throws Exception {
    when(request.getParameter(IS_BUSINESS_PROPERTY)).thenReturn(YES);
    when(request.getParameter(NAME_PROPERTY)).thenReturn(NAME);
    setRequestParameters();

    profileServlet.doPost(request, response);

    // verify if a sendError() was performed with the expected values.
    Mockito.verify(response, Mockito.times(1))
        .sendError(Mockito.eq(HttpServletResponse.SC_NOT_FOUND), Mockito.anyString());
  }

  // Helper function to set business entity properties.
  private Entity setBusinessEntityProperty() {
    Entity ent = new Entity(PROFILE_TASK_NAME, USER_ID);

    ent.setProperty(NAME_PROPERTY, NAME);
    ent.setProperty(LOCATION_PROPERTY, LOCATION);
    ent.setProperty(GEO_PT_PROPERTY, GEO_PT);
    ent.setProperty(BIO_PROPERTY, BIO);

    return ent;
  }

  // Helper function to set getParameter() method.
  private void setRequestParameters() {
    when(request.getParameter(LOCATION_PROPERTY)).thenReturn(LOCATION);
    when(request.getParameter(LAT_PROPERTY)).thenReturn(LAT);
    when(request.getParameter(LONG_PROPERTY)).thenReturn(LONG);
    when(request.getParameter(BIO_PROPERTY)).thenReturn(BIO);
  }
}
