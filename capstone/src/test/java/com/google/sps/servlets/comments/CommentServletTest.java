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

import static com.google.appengine.api.datastore.FetchOptions.Builder.withDefaults;
import static com.google.sps.data.CommentDatastoreUtil.BUSINESS_ID_PROPERTY;
import static com.google.sps.data.CommentDatastoreUtil.COMMENT_TASK_NAME;
import static com.google.sps.data.CommentDatastoreUtil.CONTENT_PROPERTY;
import static com.google.sps.data.CommentDatastoreUtil.HAS_REPLIES_PROPERTY;
import static com.google.sps.data.CommentDatastoreUtil.PARENT_ID_PROPERTY;
import static com.google.sps.data.CommentDatastoreUtil.USER_ID_PROPERTY;
import static com.google.sps.util.CommentTestUtil.createCommentEntity;
import static com.google.sps.util.CommentTestUtil.generateUniqueCommentId;
import static com.google.sps.util.TestUtil.assertResponseWithArbitraryTextRaised;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CommentServletTest {

  private static final String MOCK_EMAIL = "tutorguy@gmail.com";
  private static final String MOCK_DOMAIN = "microsoft.com";
  private final String MOCK_CONTENT = "This is my comment content.";
  private final String MOCK_USER_ID = "1";
  private final String MOCK_BUSINESS_ID = "2";
  private final String NON_EXISTENT_COMMENT_ID = "5000";

  private LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
              new LocalDatastoreServiceTestConfig(), new LocalUserServiceTestConfig())
          // All this is necessary to get the fake userService to return a user
          .setEnvEmail(MOCK_EMAIL)
          .setEnvAuthDomain(MOCK_DOMAIN)
          .setEnvIsLoggedIn(true)
          .setEnvAttributes(
              new HashMap(
                  ImmutableMap.of(
                      "com.google.appengine.api.users.UserService.user_id_key", MOCK_USER_ID)));

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;

  private CommentServlet servlet;
  private DatastoreService ds;

  @Before
  public void setUp() throws IOException {
    MockitoAnnotations.initMocks(this);
    helper.setUp();

    ds = DatastoreServiceFactory.getDatastoreService();

    servlet = new CommentServlet();
    setMockRequestParameters(request, MOCK_CONTENT, MOCK_BUSINESS_ID, null);
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  private void setMockRequestParameters(
      HttpServletRequest request, String contentStr, String businessId, String parentId) {

    doReturn(contentStr).when(request).getParameter(CONTENT_PROPERTY);
    doReturn(businessId).when(request).getParameter(BUSINESS_ID_PROPERTY);
    doReturn(parentId).when(request).getParameter(PARENT_ID_PROPERTY);
  }

  private int countCommentOccurences(
      DatastoreService ds,
      String content,
      String userId,
      String businessId,
      String parentId,
      boolean hasReplies) {
    Query query =
        new Query(COMMENT_TASK_NAME)
            .setFilter(
                new CompositeFilter(
                    CompositeFilterOperator.AND,
                    Arrays.asList(
                        new FilterPredicate(CONTENT_PROPERTY, FilterOperator.EQUAL, content),
                        new FilterPredicate(USER_ID_PROPERTY, FilterOperator.EQUAL, userId),
                        new FilterPredicate(BUSINESS_ID_PROPERTY, FilterOperator.EQUAL, businessId),
                        new FilterPredicate(PARENT_ID_PROPERTY, FilterOperator.EQUAL, parentId),
                        new FilterPredicate(
                            HAS_REPLIES_PROPERTY, FilterOperator.EQUAL, hasReplies))));

    return ds.prepare(query).countEntities(withDefaults());
  }

  // Check if we can add a comment
  @Test
  public void testBasicDoPost() throws IOException {
    assertEquals(
        0, countCommentOccurences(ds, MOCK_CONTENT, MOCK_USER_ID, MOCK_BUSINESS_ID, "", false));

    servlet.doPost(request, response);

    assertEquals(
        1, countCommentOccurences(ds, MOCK_CONTENT, MOCK_USER_ID, MOCK_BUSINESS_ID, "", false));
  }

  // Make sure that when we add two comments with the same properties we still save two seperate
  // entities
  @Test
  public void testSameCommentTwice() throws IOException {
    assertEquals(
        0, countCommentOccurences(ds, MOCK_CONTENT, MOCK_USER_ID, MOCK_BUSINESS_ID, "", false));

    servlet.doPost(request, response);
    servlet.doPost(request, response);

    assertEquals(
        2, countCommentOccurences(ds, MOCK_CONTENT, MOCK_USER_ID, MOCK_BUSINESS_ID, "", false));
  }

  // Make requests where one parameter is missing, we expect that to lead to an error
  @Test
  public void testInvalidRequests() throws IOException {
    Map<String, String> parameterMap = new HashMap<String, String>();

    parameterMap.put(CONTENT_PROPERTY, MOCK_CONTENT);
    parameterMap.put(BUSINESS_ID_PROPERTY, String.valueOf(MOCK_BUSINESS_ID));

    // Test behavior while excluding any of the parameters
    for (String excludedParameterName : parameterMap.keySet()) {
      // Remove excluded parameter
      doReturn(null).when(request).getParameter(excludedParameterName);

      servlet.doPost(request, response);

      // Check that the server rejected the request
      Mockito.verify(response, Mockito.times(1))
          .sendError(
              Mockito.eq(HttpServletResponse.SC_BAD_REQUEST),
              ArgumentMatchers.eq(
                  "Parameter \'" + excludedParameterName + "\' missing in request."));

      // Add parameter again
      doReturn(parameterMap.get(excludedParameterName))
          .when(request)
          .getParameter(excludedParameterName);
    }
  }

  @Test
  public void testUserNotLoggedIn() throws IOException {
    // Simulate no user being logged in
    helper.setEnvIsLoggedIn(false);
    helper.setUp();

    servlet.doPost(request, response);
    Mockito.verify(response, Mockito.times(1))
        .sendError(Mockito.eq(HttpServletResponse.SC_UNAUTHORIZED), Mockito.anyString());
  }

  @Test
  public void testNoParentIdSpecified() throws IOException {
    doReturn(null).when(request).getParameter(PARENT_ID_PROPERTY);
    servlet.doPost(request, response);

    assertEquals(
        1, countCommentOccurences(ds, MOCK_CONTENT, MOCK_USER_ID, MOCK_BUSINESS_ID, "", false));
  }

  @Test
  public void testSavesCorrectUserId() throws IOException {
    assertEquals(0, countUserIdOccurences(MOCK_USER_ID));

    servlet.doPost(request, response);

    assertEquals(1, countUserIdOccurences(MOCK_USER_ID));
  }

  private int countUserIdOccurences(String userId) {
    Query query =
        new Query(COMMENT_TASK_NAME)
            .setFilter(new FilterPredicate(USER_ID_PROPERTY, FilterOperator.EQUAL, userId));

    return ds.prepare(query).countEntities(withDefaults());
  }

  @Test
  public void testPostingCommentChangesHasRepliesField() throws IOException {
    // Add parent comment
    int timestamp = 1;
    ds.put(createCommentEntity(timestamp, MOCK_USER_ID, MOCK_BUSINESS_ID, false));

    // The Id that gets generated when the comment is added
    String parentId = generateUniqueCommentId(timestamp, MOCK_USER_ID, MOCK_BUSINESS_ID);

    assertEquals(0, countCommentOccurences(ds, parentId, MOCK_USER_ID, MOCK_BUSINESS_ID, "", true));

    // Add reply
    doReturn(parentId).when(request).getParameter(PARENT_ID_PROPERTY);

    servlet.doPost(request, response);

    assertEquals(1, countCommentOccurences(ds, parentId, MOCK_USER_ID, MOCK_BUSINESS_ID, "", true));
    assertEquals(
        0, countCommentOccurences(ds, parentId, MOCK_USER_ID, MOCK_BUSINESS_ID, "", false));
  }

  @Test
  public void testPostReplyToNonExistentComment() throws IOException {
    doReturn(NON_EXISTENT_COMMENT_ID).when(request).getParameter(PARENT_ID_PROPERTY);

    servlet.doPost(request, response);

    assertResponseWithArbitraryTextRaised(HttpServletResponse.SC_BAD_REQUEST, response);
  }
}
