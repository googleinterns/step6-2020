// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the 'License');
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an 'AS IS' BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withDefaults;
import static com.google.sps.data.FollowDatastoreUtil.BUSINESS_ID_PROPERTY;
import static com.google.sps.data.FollowDatastoreUtil.FOLLOW_TASK_NAME;
import static com.google.sps.data.FollowDatastoreUtil.USER_ID_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.IS_BUSINESS_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.PROFILE_TASK_NAME;
import static com.google.sps.data.ProfileDatastoreUtil.YES;
import static com.google.sps.util.FollowTestUtil.createMockFollowEntity;
import static com.google.sps.util.TestUtil.assertResponseWithArbitraryTextRaised;
import static com.google.sps.util.TestUtil.assertSameJsonObject;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class FollowServletTest {
  private static final String MOCK_EMAIL = "tutorguy@gmail.com";
  private static final String MOCK_DOMAIN = "microsoft.com";
  private static final String MOCK_USER_ID_1 = "1";
  private static final String MOCK_USER_ID_2 = "2";
  private static final String MOCK_BUSINESS_ID_1 = "3";
  private static final String MOCK_BUSINESS_ID_2 = "4";
  private static final String NON_EXISTENT_BUSINESS_ID = "99";

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
              new LocalDatastoreServiceTestConfig(), new LocalUserServiceTestConfig())
          // All this is necessary to get the fake userService to return a user
          .setEnvEmail(MOCK_EMAIL)
          .setEnvAuthDomain(MOCK_DOMAIN)
          .setEnvIsLoggedIn(true)
          .setEnvAttributes(
              new HashMap(
                  ImmutableMap.of(
                      "com.google.appengine.api.users.UserService.user_id_key", MOCK_USER_ID_1)));

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;

  private StringWriter servletResponseWriter;
  private FollowServlet servlet;
  private DatastoreService ds;

  @Before
  public void setUp() throws IOException {
    MockitoAnnotations.initMocks(this);
    helper.setUp();

    ds = DatastoreServiceFactory.getDatastoreService();

    ds.put(createMockBusinessEntity(MOCK_BUSINESS_ID_1));
    ds.put(createMockBusinessEntity(MOCK_BUSINESS_ID_2));

    servletResponseWriter = new StringWriter();
    doReturn(new PrintWriter(servletResponseWriter)).when(response).getWriter();
    servlet = new FollowServlet();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  private Entity createMockBusinessEntity(String businessId) {
    Entity businessEntity = new Entity(PROFILE_TASK_NAME, businessId);

    businessEntity.setProperty(IS_BUSINESS_PROPERTY, YES);

    return businessEntity;
  }

  private int countFollowOccurences(String userId, String businessId) {
    Query followQuery =
        new Query(FOLLOW_TASK_NAME)
            .setFilter(
                new CompositeFilter(
                    CompositeFilterOperator.AND,
                    Arrays.asList(
                        new FilterPredicate(USER_ID_PROPERTY, FilterOperator.EQUAL, userId),
                        new FilterPredicate(
                            BUSINESS_ID_PROPERTY, FilterOperator.EQUAL, businessId))));

    return ds.prepare(followQuery).countEntities(withDefaults());
  }

  /** Add a single follow. */
  @Test
  public void testBasicDoPost() throws IOException {
    doReturn(MOCK_BUSINESS_ID_1).when(request).getParameter(BUSINESS_ID_PROPERTY);

    assertEquals(0, countFollowOccurences(MOCK_USER_ID_1, MOCK_BUSINESS_ID_1));

    servlet.doPost(request, response);

    assertEquals(1, countFollowOccurences(MOCK_USER_ID_1, MOCK_BUSINESS_ID_1));
  }

  /**
   * When the user sends two requests to follow the same business only one follow should be saved.
   */
  @Test
  public void testDoPostSameFollowTwice() throws IOException {
    doReturn(MOCK_BUSINESS_ID_1).when(request).getParameter(BUSINESS_ID_PROPERTY);

    assertEquals(0, countFollowOccurences(MOCK_USER_ID_1, MOCK_BUSINESS_ID_1));

    servlet.doPost(request, response);

    assertEquals(1, countFollowOccurences(MOCK_USER_ID_1, MOCK_BUSINESS_ID_1));

    servlet.doPost(request, response);

    assertResponseWithArbitraryTextRaised(HttpServletResponse.SC_BAD_REQUEST, response);
  }

  /** Make sure a user can follow multiple businesses */
  @Test
  public void testDoPostWithTwoBusinesses() throws IOException {
    assertEquals(0, countFollowOccurences(MOCK_USER_ID_1, MOCK_BUSINESS_ID_1));
    assertEquals(0, countFollowOccurences(MOCK_USER_ID_1, MOCK_BUSINESS_ID_2));

    doReturn(MOCK_BUSINESS_ID_1).when(request).getParameter(BUSINESS_ID_PROPERTY);
    servlet.doPost(request, response);

    doReturn(MOCK_BUSINESS_ID_2).when(request).getParameter(BUSINESS_ID_PROPERTY);
    servlet.doPost(request, response);

    assertEquals(1, countFollowOccurences(MOCK_USER_ID_1, MOCK_BUSINESS_ID_1));
    assertEquals(1, countFollowOccurences(MOCK_USER_ID_1, MOCK_BUSINESS_ID_2));
  }

  /** Make sure the servlet rejects requests when the user isn't logged in. */
  @Test
  public void testDoPostWithUserNotLoggedIn() throws IOException {
    doReturn(MOCK_BUSINESS_ID_1).when(request).getParameter(BUSINESS_ID_PROPERTY);
    helper.setEnvIsLoggedIn(false);

    servlet.doPost(request, response);

    assertResponseWithArbitraryTextRaised(HttpServletResponse.SC_UNAUTHORIZED, response);
  }

  @Test
  public void testDoPostWhereBusinessDoesNotExist() throws IOException {
    doReturn(NON_EXISTENT_BUSINESS_ID).when(request).getParameter(BUSINESS_ID_PROPERTY);

    servlet.doPost(request, response);

    assertResponseWithArbitraryTextRaised(HttpServletResponse.SC_NOT_FOUND, response);
  }

  @Test
  public void testDoPostWithNoBusinessSpecified() throws IOException {
    servlet.doPost(request, response);

    assertResponseWithArbitraryTextRaised(HttpServletResponse.SC_BAD_REQUEST, response);
  }

  @Test
  public void testUserCantFollowSelf() throws IOException {
    // Set the user currently logged in to be the business that they are following.
    helper.setEnvAttributes(
        new HashMap(
            ImmutableMap.of(
                "com.google.appengine.api.users.UserService.user_id_key", MOCK_BUSINESS_ID_1)));
    helper.setUp();

    ds = DatastoreServiceFactory.getDatastoreService();

    ds.put(createMockBusinessEntity(MOCK_BUSINESS_ID_1));
    ds.put(createMockBusinessEntity(MOCK_BUSINESS_ID_2));

    doReturn(MOCK_BUSINESS_ID_1).when(request).getParameter(BUSINESS_ID_PROPERTY);

    servlet.doPost(request, response);

    assertResponseWithArbitraryTextRaised(HttpServletResponse.SC_BAD_REQUEST, response);
  }

  /**
   * Run the most basic doDeleteTest you can do. The standard user deletes a business with a
   * specified id. This assumes the follow being deleted is already in the database.
   */
  private void runBasicDoDeleteTest(String businessId) throws IOException {
    doReturn(businessId).when(request).getParameter(BUSINESS_ID_PROPERTY);

    servlet.doDelete(request, response);

    assertEquals(0, countFollowOccurences(MOCK_USER_ID_1, businessId));
  }

  @Test
  public void testBasicDoDelete() throws IOException {
    ds.put(createMockFollowEntity(MOCK_USER_ID_1, MOCK_BUSINESS_ID_1));
    runBasicDoDeleteTest(MOCK_BUSINESS_ID_1);
  }

  @Test
  public void testTwoDoDeletes() throws IOException {
    ds.put(createMockFollowEntity(MOCK_USER_ID_1, MOCK_BUSINESS_ID_1));
    ds.put(createMockFollowEntity(MOCK_USER_ID_1, MOCK_BUSINESS_ID_2));

    runBasicDoDeleteTest(MOCK_BUSINESS_ID_1);
    runBasicDoDeleteTest(MOCK_BUSINESS_ID_2);
  }

  @Test
  public void testDoDeleteWithMultipleEntitiesInDatabase() throws IOException {
    ds.put(createMockFollowEntity(MOCK_USER_ID_1, MOCK_BUSINESS_ID_1));
    ds.put(createMockFollowEntity(MOCK_USER_ID_1, MOCK_BUSINESS_ID_2));
    ds.put(createMockFollowEntity(MOCK_USER_ID_2, MOCK_BUSINESS_ID_1));
    ds.put(createMockFollowEntity(MOCK_USER_ID_2, MOCK_BUSINESS_ID_2));

    doReturn(MOCK_BUSINESS_ID_1).when(request).getParameter(BUSINESS_ID_PROPERTY);

    servlet.doDelete(request, response);

    assertEquals(0, countFollowOccurences(MOCK_USER_ID_1, MOCK_BUSINESS_ID_1));
    assertEquals(1, countFollowOccurences(MOCK_USER_ID_1, MOCK_BUSINESS_ID_2));
    assertEquals(1, countFollowOccurences(MOCK_USER_ID_2, MOCK_BUSINESS_ID_1));
    assertEquals(1, countFollowOccurences(MOCK_USER_ID_2, MOCK_BUSINESS_ID_2));
  }

  @Test
  public void testDoDeleteOnFollowThatDoesntExist() throws IOException {
    doReturn(MOCK_BUSINESS_ID_1).when(request).getParameter(BUSINESS_ID_PROPERTY);
    servlet.doDelete(request, response);

    assertResponseWithArbitraryTextRaised(HttpServletResponse.SC_NOT_FOUND, response);
  }

  @Test
  public void testDoDeleteWithNoUserLoggedIn() throws IOException {
    ds.put(createMockFollowEntity(MOCK_USER_ID_1, MOCK_BUSINESS_ID_1));
    doReturn(MOCK_BUSINESS_ID_1).when(request).getParameter(BUSINESS_ID_PROPERTY);

    helper.setEnvIsLoggedIn(false);
    helper.setUp();

    servlet.doDelete(request, response);

    assertResponseWithArbitraryTextRaised(HttpServletResponse.SC_UNAUTHORIZED, response);
  }

  @Test
  public void testDoDeleteWithNoBusinessSpecified() throws IOException {
    ds.put(createMockFollowEntity(MOCK_USER_ID_1, MOCK_BUSINESS_ID_1));
    servlet.doDelete(request, response);

    assertResponseWithArbitraryTextRaised(HttpServletResponse.SC_BAD_REQUEST, response);
  }

  @Test
  public void testBasicDoGetPositive() throws IOException {
    ds.put(createMockFollowEntity(MOCK_USER_ID_1, MOCK_BUSINESS_ID_1));
    doReturn(MOCK_BUSINESS_ID_1).when(request).getParameter(BUSINESS_ID_PROPERTY);

    servlet.doGet(request, response);

    assertSameJsonObject("true", servletResponseWriter.toString());
  }

  @Test
  public void testBasicDoGetNegative() throws IOException {
    // Add different follow
    ds.put(createMockFollowEntity(MOCK_USER_ID_1, MOCK_BUSINESS_ID_2));
    doReturn(MOCK_BUSINESS_ID_1).when(request).getParameter(BUSINESS_ID_PROPERTY);

    servlet.doGet(request, response);

    assertSameJsonObject("false", servletResponseWriter.toString());
  }

  @Test
  public void testDoGetUserNotLoggedIn() throws IOException {
    helper.setEnvIsLoggedIn(false);
    helper.setUp();

    ds.put(createMockFollowEntity(MOCK_USER_ID_1, MOCK_BUSINESS_ID_1));
    doReturn(MOCK_BUSINESS_ID_1).when(request).getParameter(BUSINESS_ID_PROPERTY);

    servlet.doGet(request, response);

    assertResponseWithArbitraryTextRaised(HttpServletResponse.SC_UNAUTHORIZED, response);
  }

  @Test
  public void testDoGetNoBusinessId() throws IOException {
    ds.put(createMockFollowEntity(MOCK_USER_ID_1, MOCK_BUSINESS_ID_1));

    servlet.doGet(request, response);

    assertResponseWithArbitraryTextRaised(HttpServletResponse.SC_BAD_REQUEST, response);
  }
}
