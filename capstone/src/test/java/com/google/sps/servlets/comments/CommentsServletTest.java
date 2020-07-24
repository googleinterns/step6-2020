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

import static com.google.sps.data.CommentDatastoreUtil.BUSINESS_ID_PROPERTY;
import static com.google.sps.data.CommentDatastoreUtil.COMMENT_TASK_NAME;
import static com.google.sps.data.CommentDatastoreUtil.CONTENT_PROPERTY;
import static com.google.sps.data.CommentDatastoreUtil.PARENT_ID_PROPERTY;
import static com.google.sps.data.CommentDatastoreUtil.TIMESTAMP_PROPERTY;
import static com.google.sps.data.CommentDatastoreUtil.USER_ID_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.NAME_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.PROFILE_TASK_NAME;
import static com.google.sps.data.ProfileDatastoreUtil.getProfileName;
import static com.google.sps.util.TestUtil.assertResponseWithArbitraryTextRaised;
import static com.google.sps.util.TestUtil.assertSameJsonObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CommentsServletTest {
  private final String USER_ID_0 = "0";
  private final String USER_ID_1 = "1";
  private final String USER_ID_2 = "2";
  private final String BUSINESS_ID_0 = "0";
  private final String BUSINESS_ID_1 = "1";
  private final String USER_NAME_0 = "User 0";
  private final String USER_NAME_1 = "User 1";

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;

  private StringWriter servletResponseWriter;
  private CommentsServlet servlet;

  DatastoreService ds;

  @Before
  public void setUp() throws IOException {
    MockitoAnnotations.initMocks(this);
    helper.setUp();

    ds = DatastoreServiceFactory.getDatastoreService();

    ds.put(createProfileEntity(USER_ID_0, USER_NAME_0));
    ds.put(createProfileEntity(USER_ID_1, USER_NAME_1));

    servletResponseWriter = new StringWriter();
    doReturn(new PrintWriter(servletResponseWriter)).when(response).getWriter();
    servlet = new CommentsServlet(ds);
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  private void initDatastore(DatastoreService ds) {
    // Create top level comments
    ds.put(createCommentEntity(/*timestamp*/ 0, USER_ID_0, BUSINESS_ID_0));
    ds.put(createCommentEntity(/*timestamp*/ 3, USER_ID_1, BUSINESS_ID_0));
    ds.put(createCommentEntity(/*timestamp*/ 6, USER_ID_0, BUSINESS_ID_1));
    ds.put(createCommentEntity(/*timestamp*/ 9, USER_ID_1, BUSINESS_ID_1));

    // Create two replies to each top level comment
    ds.put(
        createCommentEntity(
            /*timestamp*/ 1,
            USER_ID_0,
            BUSINESS_ID_0, /*parentId*/
            generateUniqueId(0, USER_ID_0, BUSINESS_ID_0)));
    ds.put(
        createCommentEntity(
            /*timestamp*/ 2,
            USER_ID_1,
            BUSINESS_ID_0, /*parentId*/
            generateUniqueId(0, USER_ID_0, BUSINESS_ID_0)));

    ds.put(
        createCommentEntity(
            /*timestamp*/ 4,
            USER_ID_0,
            BUSINESS_ID_0, /*parentId*/
            generateUniqueId(3, USER_ID_1, BUSINESS_ID_0)));
    ds.put(
        createCommentEntity(
            /*timestamp*/ 5,
            USER_ID_1,
            BUSINESS_ID_0, /*parentId*/
            generateUniqueId(3, USER_ID_1, BUSINESS_ID_0)));

    ds.put(
        createCommentEntity(
            /*timestamp*/ 7,
            USER_ID_0,
            BUSINESS_ID_1, /*parentId*/
            generateUniqueId(6, USER_ID_0, BUSINESS_ID_1)));
    ds.put(
        createCommentEntity(
            /*timestamp*/ 8,
            USER_ID_1,
            BUSINESS_ID_1, /*parentId*/
            generateUniqueId(6, USER_ID_0, BUSINESS_ID_1)));

    ds.put(
        createCommentEntity(
            /*timestamp*/ 10,
            USER_ID_0,
            BUSINESS_ID_1, /*parentId*/
            generateUniqueId(9, USER_ID_1, BUSINESS_ID_1)));
    ds.put(
        createCommentEntity(
            /*timestamp*/ 11,
            USER_ID_1,
            BUSINESS_ID_1, /*parentId*/
            generateUniqueId(9, USER_ID_1, BUSINESS_ID_1)));

    // Create a top level comment without replies
    ds.put(createCommentEntity(/*timestamp*/ 100, USER_ID_2, BUSINESS_ID_1));
  }

  private Entity createProfileEntity(String id, String name) {
    Entity profileEntity = new Entity(PROFILE_TASK_NAME, id);
    profileEntity.setProperty(NAME_PROPERTY, name);

    return profileEntity;
  }

  private Entity createCommentEntity(long timestamp, String userId, String businessId) {
    return createCommentEntity(timestamp, userId, businessId, "");
  }

  private Entity createCommentEntity(
      long timestamp, String userId, String businessId, String parentId) {
    String id = generateUniqueId(timestamp, userId, businessId);

    Entity comment = new Entity(COMMENT_TASK_NAME, id);

    comment.setProperty(CONTENT_PROPERTY, id);
    comment.setProperty(TIMESTAMP_PROPERTY, timestamp);
    comment.setProperty(USER_ID_PROPERTY, userId);
    comment.setProperty(BUSINESS_ID_PROPERTY, businessId);
    comment.setProperty(PARENT_ID_PROPERTY, parentId);

    return comment;
  }

  private Comment generateCommentForTest(long timestamp, String userId, String businessId) {
    return generateCommentForTest(timestamp, userId, businessId, "");
  }

  private Comment generateCommentForTest(
      long timestamp, String userId, String businessId, String parentId) {
    String id = generateUniqueId(timestamp, userId, businessId);

    return new Comment(id, id, timestamp, userId, getProfileName(userId, ds), businessId, parentId);
  }

  private String generateUniqueId(long timestamp, String userId, String businessId) {
    return "1" + timestamp + userId + businessId;
  }

  /** Assert that the response by the server was just an empty object */
  private void assertEmptyResponse() {
    assertSameJsonObject("[]", servletResponseWriter.toString());
  }

  /**
   * Runs a standard test in which one parameter in the request is specified, the datastore has the
   * * same data in it that it always does, and a certain json response is expected.
   */
  private void runTest(
      String parameterName, String parameterVal, Comment[] expectedReturnedComments)
      throws IOException {
    initDatastore(ds);
    doReturn(parameterVal).when(request).getParameter(parameterName);

    String expectedResponse = new Gson().toJson(expectedReturnedComments);

    servlet.doGet(request, response);
    String servletResponse = servletResponseWriter.toString();

    assertSameJsonObject(expectedResponse, servletResponse);
  }

  /** Make a valid request on an empty database expecting an empty reponse. */
  @Test
  public void testEmptyDatabase() throws IOException {
    doReturn(USER_ID_0).when(request).getParameter(USER_ID_PROPERTY);

    servlet.doGet(request, response);

    assertEmptyResponse();
  }

  /** Test valid requests that are supposed to return an empty request. */
  @Test
  public void testEmptyResponses() throws IOException {
    // Test for requests that yield empty results on a populated database
    String parameterNames[] =
        new String[] {USER_ID_PROPERTY, BUSINESS_ID_PROPERTY, PARENT_ID_PROPERTY};

    for (String parameterName : parameterNames) {
      // Set a parameter that does not get a result
      doReturn("3").when(request).getParameter(parameterName);

      servlet.doGet(request, response);

      assertEmptyResponse();

      // Clear servletResponseWriter so that we don't get have stuff from the previous loop
      servletResponseWriter.getBuffer().setLength(0);
      reset(request);
    }
  }

  /** Test a request for a certain business's comments. */
  @Test
  public void testBusinessRequest() throws IOException {
    String parameterName = BUSINESS_ID_PROPERTY;
    String parameterVal = "0";

    Comment[] expectedReturnedComments =
        new Comment[] {
          generateCommentForTest(/*timestamp*/ (long) 3, USER_ID_1, BUSINESS_ID_0),
          generateCommentForTest(/*timestamp*/ 0, USER_ID_0, BUSINESS_ID_0),
        };

    runTest(parameterName, parameterVal, expectedReturnedComments);
  }

  /** Test a request for a certain users comments */
  @Test
  public void testUserRequest() throws IOException {
    String parameterName = USER_ID_PROPERTY;
    String parameterVal = "0";

    Comment[] expectedReturnedComments =
        new Comment[] {
          generateCommentForTest(
              /*timestamp*/ 10,
              USER_ID_0,
              BUSINESS_ID_1,
              generateUniqueId(9, USER_ID_1, BUSINESS_ID_1)),
          generateCommentForTest(
              /*timestamp*/ 7,
              USER_ID_0,
              BUSINESS_ID_1,
              generateUniqueId(6, USER_ID_0, BUSINESS_ID_1)),
          generateCommentForTest(/*timestamp*/ 6, USER_ID_0, BUSINESS_ID_1),
          generateCommentForTest(
              /*timestamp*/ 4,
              USER_ID_0,
              BUSINESS_ID_0,
              generateUniqueId(3, USER_ID_1, BUSINESS_ID_0)),
          generateCommentForTest(
              /*timestamp*/ 1,
              USER_ID_0,
              BUSINESS_ID_0,
              generateUniqueId(0, USER_ID_0, BUSINESS_ID_0)),
          generateCommentForTest(/*timestamp*/ 0, USER_ID_0, BUSINESS_ID_0),
        };

    runTest(parameterName, parameterVal, expectedReturnedComments);
  }

  /** Test a request for replies to a certain comment */
  @Test
  public void testReplyRequest() throws IOException {
    String parameterName = PARENT_ID_PROPERTY;
    String parameterVal = generateUniqueId(0, "0", "0");

    Comment[] expectedReturnedComments =
        new Comment[] {
          generateCommentForTest(
              /*timestamp*/ 2, USER_ID_1, BUSINESS_ID_0, /*parentId*/ parameterVal),
          generateCommentForTest(
              /*timestamp*/ 1, USER_ID_0, BUSINESS_ID_0, /*parentId*/ parameterVal),
        };

    runTest(parameterName, parameterVal, expectedReturnedComments);
  }

  /** Test that the server rejects requests without arguments. */
  @Test
  public void testRejectsRequestsWithNoArguments() throws IOException {
    initDatastore(ds);
    servlet.doGet(request, response);

    assertResponseWithArbitraryTextRaised(HttpServletResponse.SC_BAD_REQUEST, response);
  }

  /** Test that the server rejects requests with two arguements. */
  @Test
  public void testRejectsRequestsWithTwoArguments() throws IOException {
    initDatastore(ds);

    doReturn(USER_ID_0).when(request).getParameter(USER_ID_PROPERTY);
    doReturn(BUSINESS_ID_0).when(request).getParameter(BUSINESS_ID_PROPERTY);

    servlet.doGet(request, response);

    assertResponseWithArbitraryTextRaised(HttpServletResponse.SC_BAD_REQUEST, response);
  }

  /** Test that the server rejects requests with two arguements. */
  @Test
  public void testRejectsRequestsWithThreeArguments() throws IOException {
    initDatastore(ds);

    doReturn(USER_ID_0).when(request).getParameter(USER_ID_PROPERTY);
    doReturn(BUSINESS_ID_0).when(request).getParameter(BUSINESS_ID_PROPERTY);
    doReturn(generateUniqueId(0, USER_ID_0, BUSINESS_ID_0))
        .when(request)
        .getParameter(PARENT_ID_PROPERTY);

    servlet.doGet(request, response);

    assertResponseWithArbitraryTextRaised(HttpServletResponse.SC_BAD_REQUEST, response);
  }

  @Test
  public void testShowsUserName() throws IOException {
    long timestamp = 0;
    String userId = "33";
    String username = "Larry";
    String businessId = "0";

    ds.put(createProfileEntity(userId, username));
    ds.put(createCommentEntity(/*timestamp*/ 0, userId, /*businessId*/ businessId));

    doReturn(userId).when(request).getParameter(USER_ID_PROPERTY);

    servlet.doGet(request, response);
    String servletResponse = servletResponseWriter.toString();

    Comment expectedRetrievedComment = generateCommentForTest(0, userId, businessId);
    String expectedResponse = new Gson().toJson(new Comment[] {expectedRetrievedComment});

    assertSameJsonObject(expectedResponse, servletResponse);
  }
}
