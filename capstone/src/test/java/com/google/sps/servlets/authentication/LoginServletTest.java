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

package com.google.sps.servlets.authentication;

import static com.google.sps.data.ProfileDatastoreUtil.IS_BUSINESS_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.NO;
import static com.google.sps.data.ProfileDatastoreUtil.PROFILE_TASK_NAME;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
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
import com.google.sps.data.User;
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
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public class LoginServletTest {
  private static final String USER_ID = "12345";
  private static final String EMAIL = "abc@gmail.com";
  private static final String AUTHDOMAIN = "gmail.com";
  private static final String LOG_IN_URL = "/_ah/login?continue=%2Fcheck_new_user";
  private static final String LOG_OUT_URL = "/_ah/logout?continue=index.html";

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  private LocalServiceTestHelper helper;
  private LoginServlet loginServlet;
  private DatastoreService datastore;
  private StringWriter stringWriter;
  private PrintWriter printWriter;

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

    loginServlet = new LoginServlet();
    datastore = DatastoreServiceFactory.getDatastoreService();

    stringWriter = new StringWriter();
    printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);
  }

  @After
  public void tearDown() throws Exception {
    helper.tearDown();
  }

  /*
   *  Test for when user is logged in, it should return isUserLoggedin boolean value
   *  true and the logout URL.
   **/
  @Test
  public void loggedInUserReturnsLogOutUrl() throws ServletException, IOException {
    String keyString = KeyFactory.createKeyString(PROFILE_TASK_NAME, USER_ID);
    Key userKey = KeyFactory.stringToKey(keyString);
    Entity ent = new Entity(PROFILE_TASK_NAME, USER_ID);
    ent.setProperty(IS_BUSINESS_PROPERTY, NO);
    datastore.put(ent);

    loginServlet.doGet(request, response);

    User LoggedInUser = new User(true, LOG_OUT_URL, USER_ID, NO);

    String responseString = stringWriter.getBuffer().toString().trim();
    JsonElement responseJsonElement = new JsonParser().parse(responseString);

    Gson gson = new GsonBuilder().create();
    JsonElement userJsonElement = gson.toJsonTree(LoggedInUser);

    JsonObject responseJsonObject = responseJsonElement.getAsJsonObject();
    JsonObject userJsonObject = userJsonElement.getAsJsonObject();

    Assert.assertEquals(responseJsonObject, userJsonObject);
  }

  /*
   *  Test for when user is logged out, it should return isUserLoggedin boolean value
   *  false and the login URL.
   **/
  @Test
  public void loggedOutUserReturnsLogInUrl() throws ServletException, IOException {
    helper =
        new LocalServiceTestHelper(new LocalUserServiceTestConfig())
            .setEnvEmail(EMAIL)
            .setEnvAuthDomain(AUTHDOMAIN)
            .setEnvIsLoggedIn(false);
    helper.setUp();

    loginServlet.doGet(request, response);

    User LoggedOutUser = new User(false, LOG_IN_URL, null, null);

    String responseString = stringWriter.getBuffer().toString().trim();
    JsonElement responseJsonElement = new JsonParser().parse(responseString);

    Gson gson = new GsonBuilder().create();
    JsonElement userJsonElement = gson.toJsonTree(LoggedOutUser);

    JsonObject responseJsonObject = responseJsonElement.getAsJsonObject();
    JsonObject userJsonObject = userJsonElement.getAsJsonObject();

    Assert.assertEquals(responseJsonObject, userJsonObject);
  }
}
