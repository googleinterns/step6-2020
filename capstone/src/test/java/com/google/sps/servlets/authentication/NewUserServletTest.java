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

import static com.google.sps.data.ProfileDatastoreUtil.ANONYMOUS_NAME;
import static com.google.sps.data.ProfileDatastoreUtil.BIO_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.LAT_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.LONG_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.IS_BUSINESS_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.LOCATION_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.NAME_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.NO;
import static com.google.sps.data.ProfileDatastoreUtil.NULL_STRING;
import static com.google.sps.data.ProfileDatastoreUtil.PROFILE_TASK_NAME;

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
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public class NewUserServletTest {
  private static final String NAME = "John Doe";
  private static final String LAT = "37.386051";
  private static final String LOCATION = "Mountain View, CA, USA";
  private static final String LONG = "-122.083855";
  private static final String BIO = "This is my bio.";
  private static final String USER_ID = "12345";
  private static final String EMAIL = "abc@gmail.com";
  private static final String AUTHDOMAIN = "gmail.com";

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  private LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
              new LocalUserServiceTestConfig(), new LocalDatastoreServiceTestConfig())
          .setEnvEmail(EMAIL)
          .setEnvAuthDomain(AUTHDOMAIN)
          .setEnvIsLoggedIn(true)
          .setEnvAttributes(
              new HashMap(
                  ImmutableMap.of(
                      "com.google.appengine.api.users.UserService.user_id_key", USER_ID)));
  private NewUserServlet newUserServlet;
  private DatastoreService datastore;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    helper.setUp();

    datastore = DatastoreServiceFactory.getDatastoreService();
    newUserServlet = new NewUserServlet();
  }

  @After
  public void tearDown() throws Exception {
    helper.tearDown();
  }

  /*
   *  Check the doGet function from New User Servlet. Create a a new user
   *  and add it to datastore. It should return a EntityNotFoundException.
   *  Then it should be added to the datastore.
   **/
  @Test
  public void newUserAddToDatabase() throws Exception {
    String keyString = KeyFactory.createKeyString(PROFILE_TASK_NAME, USER_ID);
    Key userKey = KeyFactory.stringToKey(keyString);

    newUserServlet.doGet(request, response);

    Entity capEntity = datastore.get(userKey);
    Assert.assertEquals(capEntity.getProperty(IS_BUSINESS_PROPERTY), NO);
    Assert.assertEquals(capEntity.getProperty(NAME_PROPERTY), ANONYMOUS_NAME);
    Assert.assertEquals(capEntity.getProperty(LOCATION_PROPERTY), NULL_STRING);
    Assert.assertEquals(capEntity.getProperty(LAT_PROPERTY), NULL_STRING);
    Assert.assertEquals(capEntity.getProperty(LONG_PROPERTY), NULL_STRING);
    Assert.assertEquals(capEntity.getProperty(BIO_PROPERTY), NULL_STRING);
  }

  /*
   *  Check the doGet function from New User Servlet. Create a a returning user
   *  and add it to datastore. It should return an entity when called datastore.get().
   **/
  @Test
  public void returningUserIsInDatabase() throws Exception {
    String keyString = KeyFactory.createKeyString(PROFILE_TASK_NAME, USER_ID);
    Key userKey = KeyFactory.stringToKey(keyString);

    Entity ent = new Entity(PROFILE_TASK_NAME, USER_ID);
    ent.setProperty(IS_BUSINESS_PROPERTY, NO);
    ent.setProperty(NAME_PROPERTY, NAME);
    ent.setProperty(LOCATION_PROPERTY, LOCATION);
    ent.setProperty(LAT_PROPERTY, LAT);
    ent.setProperty(LONG_PROPERTY, LONG);
    ent.setProperty(BIO_PROPERTY, BIO);
    datastore.put(ent);

    newUserServlet.doGet(request, response);

    Entity capEntity = datastore.get(userKey);
    Assert.assertEquals(capEntity.getProperty(IS_BUSINESS_PROPERTY), NO);
    Assert.assertEquals(capEntity.getProperty(NAME_PROPERTY), NAME);
    Assert.assertEquals(capEntity.getProperty(LOCATION_PROPERTY), LOCATION);
    Assert.assertEquals(capEntity.getProperty(LAT_PROPERTY), LAT);
    Assert.assertEquals(capEntity.getProperty(LONG_PROPERTY), LONG);
    Assert.assertEquals(capEntity.getProperty(BIO_PROPERTY), BIO);
  }
}
