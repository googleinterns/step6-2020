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

import static com.google.sps.data.FollowDatastoreUtil.BUSINESS_ID_PROPERTY;
import static com.google.sps.data.FollowDatastoreUtil.FOLLOW_TASK_NAME;
import static com.google.sps.data.FollowDatastoreUtil.USER_ID_PROPERTY;
import static org.mockito.Mockito.doReturn;
import static com.google.sps.util.TestUtil.assertResponseWithArbitraryTextRaised;
import static com.google.sps.util.TestUtil.assertSameJsonObject;

import com.google.sps.data.Follow;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;
import static com.google.sps.util.FollowTestUtil.createMockFollowEntity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.HashMap;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class FollowsServletTest {
  private static final String MOCK_EMAIL = "tutorguy@gmail.com";
  private static final String MOCK_DOMAIN = "microsoft.com";
  private static final String MOCK_USER_ID_1 = "1";
  private static final String MOCK_USER_ID_2 = "2";
  private static final String MOCK_USER_ID_3 = "3";  
  private static final String MOCK_BUSINESS_ID_1 = "4";
  private static final String MOCK_BUSINESS_ID_2 = "5";
  private static final String MOCK_BUSINESS_ID_3 = "6";

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;

  private StringWriter servletResponseWriter;
  private FollowsServlet servlet;
  private DatastoreService ds;

  @Before
  public void setUp() throws IOException {
    MockitoAnnotations.initMocks(this);
    helper.setUp();

    ds = DatastoreServiceFactory.getDatastoreService();

    // These are the follows that exist by default.
    // User 1 follows the first and second business.
    // User 2 follows only the first business.
    // Nobody follows user three. Sad!
    ds.put(createMockFollowEntity(MOCK_USER_ID_1, MOCK_BUSINESS_ID_1));
    ds.put(createMockFollowEntity(MOCK_USER_ID_1, MOCK_BUSINESS_ID_2));
    ds.put(createMockFollowEntity(MOCK_USER_ID_2, MOCK_BUSINESS_ID_1));

    servletResponseWriter = new StringWriter();
    doReturn(new PrintWriter(servletResponseWriter)).when(response).getWriter();

    servlet = new FollowsServlet();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  private void runTest(String filterParameter, String filterValue, Follow[] expectedReturnedFollows)
      throws IOException {
    doReturn(filterValue).when(request).getParameter(filterParameter);

    servlet.doGet(request, response);

    String expectedResponse = new Gson().toJson(expectedReturnedFollows);

    assertSameJsonObject(expectedResponse, servletResponseWriter.toString());
  }

  @Test
  public void testDoGetBusinessMultipleResults() throws IOException {
    Follow[] expectedReturnedFollows = 
        new Follow[]{
            new Follow(MOCK_USER_ID_1, MOCK_BUSINESS_ID_1), 
            new Follow(MOCK_USER_ID_2, MOCK_BUSINESS_ID_1)};
    
    runTest(BUSINESS_ID_PROPERTY, MOCK_BUSINESS_ID_1, expectedReturnedFollows);
  }

  @Test
  public void testDoGetBusinessSingleResult() throws IOException {
    Follow[] expectedReturnedFollows = new Follow[]{new Follow(MOCK_USER_ID_1, MOCK_BUSINESS_ID_2)};
    
    runTest(BUSINESS_ID_PROPERTY, MOCK_BUSINESS_ID_2, expectedReturnedFollows);
  }

  @Test
  public void testDoGetBusinessNoResults() throws IOException {
    Follow[] expectedReturnedFollows = new Follow[]{};
    
    runTest(BUSINESS_ID_PROPERTY, MOCK_BUSINESS_ID_3, expectedReturnedFollows);
  }

  private void runDoGetUserTest(String userId, Follow[] expectedReturnedFollows) throws IOException {
    doReturn(userId).when(request).getParameter(USER_ID_PROPERTY);
    
    servlet.doGet(request, response);

    String expectedResponse = new Gson().toJson(expectedReturnedFollows);

    assertSameJsonObject(expectedResponse, servletResponseWriter.toString());
  }

  @Test
  public void testDoGetUserMultipleResults() throws IOException {
    Follow[] expectedReturnedFollows = 
        new Follow[]{
            new Follow(MOCK_USER_ID_1, MOCK_BUSINESS_ID_1), 
            new Follow(MOCK_USER_ID_1, MOCK_BUSINESS_ID_2)};
    
    runTest(USER_ID_PROPERTY, MOCK_USER_ID_1, expectedReturnedFollows);
  }

  @Test
  public void testDoGetUserSingleResult() throws IOException {
    Follow[] expectedReturnedFollows = new Follow[]{new Follow(MOCK_USER_ID_2, MOCK_BUSINESS_ID_1)};
    
    runTest(USER_ID_PROPERTY, MOCK_USER_ID_2, expectedReturnedFollows);
  }

  @Test
  public void testDoGetUserNoResults() throws IOException {
    Follow[] expectedReturnedFollows = new Follow[]{};
    
    runTest(USER_ID_PROPERTY, MOCK_USER_ID_3, expectedReturnedFollows);
  }

  @Test
  public void testDoGetNoParameters() throws IOException {
    servlet.doGet(request, response);

    assertResponseWithArbitraryTextRaised(HttpServletResponse.SC_BAD_REQUEST, response);
  }

  @Test
  public void testDoGetBothParameters() throws IOException {
    doReturn(MOCK_BUSINESS_ID_1).when(request).getParameter(BUSINESS_ID_PROPERTY);
    doReturn(MOCK_USER_ID_1).when(request).getParameter(USER_ID_PROPERTY);

    servlet.doGet(request, response);

    assertResponseWithArbitraryTextRaised(HttpServletResponse.SC_BAD_REQUEST, response);
  }
}
