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
import static org.mockito.Mockito.when;
import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.data.DatastoreNames;
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
import org.mockito.ArgumentMatchers;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.users.User;

public class CommentServletTest {

  private final int COUNTING_LIMIT = 10;

  private final String MOCK_CONTENT = "This is my comment content.";
  private final String MOCK_USER_ID = "1";
  private final String MOCK_BUSINESS_ID = "2";
  private final String MOCK_PARENT_ID = "3";

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig(), 
                                 new LocalUserServiceTestConfig());

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock private UserService userService;
  

  private CommentServlet servlet;
  private DatastoreService ds;
  
  @Before
  public void setUp() throws IOException {
    MockitoAnnotations.initMocks(this);
    helper.setUp();

    ds = DatastoreServiceFactory.getDatastoreService();
    servlet = new CommentServlet(userService, ds);
    setMockUserId(MOCK_USER_ID);
    setMockRequestParameters(request, MOCK_CONTENT, MOCK_USER_ID, MOCK_BUSINESS_ID, MOCK_PARENT_ID);
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  private void setMockUserId(String mockId) {
      when(userService.getCurrentUser()).thenReturn(new User("", "", mockId));
  }

  private void setMockRequestParameters(
      HttpServletRequest request, String contentStr, String userId, String businessId, String parentId) {

    doReturn(contentStr).when(request).getParameter(DatastoreNames.CONTENT_PROPERTY);
    doReturn(userId).when(request).getParameter(DatastoreNames.USER_ID_PROPERTY);
    doReturn(businessId)
        .when(request)
        .getParameter(DatastoreNames.BUSINESS_ID_PROPERTY);
    doReturn(parentId).when(request).getParameter(DatastoreNames.PARENT_ID_PROPERTY);
  }

  private Query queryComment(String content, String userId, String businessId, String parentId) {
    return new Query(DatastoreNames.COMMENT_ENTITY_NAME)
        .setFilter(
            new CompositeFilter(
                CompositeFilterOperator.AND,
                Arrays.asList(
                    new FilterPredicate(
                        DatastoreNames.CONTENT_PROPERTY, FilterOperator.EQUAL, content),
                    new FilterPredicate(
                        DatastoreNames.USER_ID_PROPERTY, FilterOperator.EQUAL, userId),
                    new FilterPredicate(
                        DatastoreNames.BUSINESS_ID_PROPERTY, FilterOperator.EQUAL, businessId),
                    new FilterPredicate(
                        DatastoreNames.PARENT_ID_PROPERTY, FilterOperator.EQUAL, parentId))));
  }

  private int countCommentOccurences(
      DatastoreService ds, String content, String userId, String businessId, String parentId) {
    return ds.prepare(queryComment(content, userId, businessId, parentId))
        .countEntities(withLimit(COUNTING_LIMIT));
  }

  // Check if we can add a comment
  @Test
  public void testBasicDoPost() throws IOException {
    assertEquals(0, countCommentOccurences(ds, MOCK_CONTENT, MOCK_USER_ID, MOCK_BUSINESS_ID, 
                                           MOCK_PARENT_ID));

    servlet.doPost(request, response);

    assertEquals(1, countCommentOccurences(ds, MOCK_CONTENT, MOCK_USER_ID, MOCK_BUSINESS_ID,
                                           MOCK_PARENT_ID));   
  }

  // Make sure that when we add two comments with the same properties we still save two seperate 
  // entities
  @Test
  public void testSameCommentTwice() throws IOException {
    assertEquals(
        0,
        countCommentOccurences(ds, MOCK_CONTENT, MOCK_USER_ID, MOCK_BUSINESS_ID, MOCK_PARENT_ID));

    servlet.doPost(request, response);
    servlet.doPost(request, response);

    assertEquals(
        2,
        countCommentOccurences(ds, MOCK_CONTENT, MOCK_USER_ID, MOCK_BUSINESS_ID, MOCK_PARENT_ID));
  }

  // Make requests where one parameter is missing, we expect that to lead to an error
  @Test
  public void testInvalidRequests() throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    Map<String, String> parameterMap = new HashMap<String, String>();

    parameterMap.put(DatastoreNames.CONTENT_PROPERTY, MOCK_CONTENT);
    parameterMap.put(DatastoreNames.USER_ID_PROPERTY, String.valueOf(MOCK_USER_ID));
    parameterMap.put(DatastoreNames.BUSINESS_ID_PROPERTY, String.valueOf(MOCK_BUSINESS_ID));
    parameterMap.put(DatastoreNames.PARENT_ID_PROPERTY, String.valueOf(MOCK_PARENT_ID));

    setMockRequestParameters(request, MOCK_CONTENT, MOCK_USER_ID, MOCK_BUSINESS_ID, MOCK_PARENT_ID);

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
                  "Parameter \'"
                      + excludedParameterName
                      + "\' missing in request."));

      // Add parameter again
      doReturn(parameterMap.get(excludedParameterName))
          .when(request)
          .getParameter(excludedParameterName);
    }
  }

  // Make sure it's impossible to post a comment without being logged in
  @Test
  public void testNoUserLoggedIn() throws IOException {
    doReturn(null).when(userService).getCurrentUser();

    servlet.doPost(request, response);

    Mockito.verify(response, Mockito.times(1))
           .sendError(Mockito.eq(HttpServletResponse.SC_UNAUTHORIZED), Mockito.anyString());
  }

  // Make sure it's impossible to post comment under a different name
  @Test
  public void testWrongUserLoggedIn() throws IOException {
    setMockUserId(MOCK_USER_ID + 1);
    servlet.doPost(request, response);

    Mockito.verify(response, Mockito.times(1))
           .sendError(Mockito.eq(HttpServletResponse.SC_UNAUTHORIZED), Mockito.anyString());
  }
}
