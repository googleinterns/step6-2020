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

import static com.google.sps.data.CommentDatastore.BUSINESS_ID_PROPERTY;
import static com.google.sps.data.CommentDatastore.COMMENT_ENTITY_NAME;
import static com.google.sps.data.CommentDatastore.CONTENT_PROPERTY;
import static com.google.sps.data.CommentDatastore.PARENT_ID_PROPERTY;
import static com.google.sps.data.CommentDatastore.TIMESTAMP_PROPERTY;
import static com.google.sps.data.CommentDatastore.USER_ID_PROPERTY;
import static com.google.sps.data.CommentDatastore.generateComment;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CommentsServletTest {
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
    servletResponseWriter = new StringWriter();
    doReturn(new PrintWriter(servletResponseWriter)).when(response).getWriter();
    servlet = new CommentsServlet(ds);
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  private void initDatastore(DatastoreService ds) {
    // Create four top level comments on two businesses between two users
    // On each business, the first two comments are by the user 0 and the second two are by user 1
    // Each top level comment has two replies, the first by user zero and the second by user 1
    // Create top level comments
    ds.put(createCommentEntity(/*timestamp*/ 0, /*userId*/ 0, /*businessId*/ 0));
    ds.put(createCommentEntity(/*timestamp*/ 3, /*userId*/ 1, /*businessId*/ 0));
    ds.put(createCommentEntity(/*timestamp*/ 6, /*userId*/ 0, /*businessId*/ 1));
    ds.put(createCommentEntity(/*timestamp*/ 9, /*userId*/ 1, /*businessId*/ 1));

    // Create two replies to each top level comment
    ds.put(createCommentEntity(/*timestamp*/ 1, /*userId*/ 0, /*businessId*/ 0, /*parentId*/ 1000));
    ds.put(createCommentEntity(/*timestamp*/ 2, /*userId*/ 1, /*businessId*/ 0, /*parentId*/ 1000));

    ds.put(createCommentEntity(/*timestamp*/ 4, /*userId*/ 0, /*businessId*/ 0, /*parentId*/ 1310));
    ds.put(createCommentEntity(/*timestamp*/ 5, /*userId*/ 1, /*businessId*/ 0, /*parentId*/ 1310));

    ds.put(createCommentEntity(/*timestamp*/ 7, /*userId*/ 0, /*businessId*/ 1, /*parentId*/ 1601));
    ds.put(createCommentEntity(/*timestamp*/ 8, /*userId*/ 1, /*businessId*/ 1, /*parentId*/ 1601));

    ds.put(
        createCommentEntity(/*timestamp*/ 10, /*userId*/ 0, /*businessId*/ 1, /*parentId*/ 1911));
    ds.put(
        createCommentEntity(/*timestamp*/ 11, /*userId*/ 1, /*businessId*/ 1, /*parentId*/ 1911));

    // Create a top level comment without replies
    ds.put(createCommentEntity(/*timestamp*/ 100, /*userId*/ 2, /*businessId*/ 1, 0));
  }

  private Entity createCommentEntity(long timestamp, long userId, long businessId) {
    Entity comment = new Entity(COMMENT_ENTITY_NAME);

    String id = "1" + timestamp + userId + businessId;

    comment.setProperty(CONTENT_PROPERTY, id);
    comment.setProperty(TIMESTAMP_PROPERTY, timestamp);
    comment.setProperty(USER_ID_PROPERTY, String.valueOf(userId));
    comment.setProperty(BUSINESS_ID_PROPERTY, String.valueOf(businessId));
    comment.setProperty("id", id);
    comment.setProperty(PARENT_ID_PROPERTY, "");

    return comment;
  }

  private Entity createCommentEntity(long timestamp, long userId, long businessId, long parentId) {
    Entity comment = createCommentEntity(timestamp, userId, businessId);

    comment.setProperty(PARENT_ID_PROPERTY, String.valueOf(parentId));

    return comment;
  }

  /** Assert that the response by the server was just an empty JSON object */
  private void assertEmptyResponse() {
    assertSameJsonObject("[]", servletResponseWriter.toString());
  }

  /** Assert that two JSON strings specify the same JSON object */
  private void assertSameJsonObject(String a, String b) {
    JsonParser parser = new JsonParser();
    Assert.assertEquals(parser.parse(a), parser.parse(b));
  }

  private void assertResponseWithArbitraryTextRaised(int targetResponse) throws IOException {
    Mockito.verify(response, Mockito.times(1))
        .sendError(Mockito.eq(targetResponse), Mockito.anyString());
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
    doReturn("0").when(request).getParameter(USER_ID_PROPERTY);

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
          generateComment(createCommentEntity(/*timestamp*/ 3, /*userId*/ 1, /*businessId*/ 0)),
          generateComment(createCommentEntity(/*timestamp*/ 0, /*userId*/ 0, /*businessId*/ 0)),
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
          generateComment(
              createCommentEntity(/*timestamp*/ 10, /*userId*/ 0, /*businessId*/ 1, 1911)),
          generateComment(
              createCommentEntity(
                  /*timestamp*/ 7, /*userId*/ 0, /*businessId*/ 1, /*parentId*/ 1601)),
          generateComment(createCommentEntity(/*timestamp*/ 6, /*userId*/ 0, /*businessId*/ 1)),
          generateComment(
              createCommentEntity(
                  /*timestamp*/ 4, /*userId*/ 0, /*businessId*/ 0, /*parentId*/ 1310)),
          generateComment(
              createCommentEntity(
                  /*timestamp*/ 1, /*userId*/ 0, /*businessId*/ 0, /*parentId*/ 1000)),
          generateComment(createCommentEntity(/*timestamp*/ 0, /*userId*/ 0, /*businessId*/ 0)),
        };

    runTest(parameterName, parameterVal, expectedReturnedComments);
  }

  /** Test a request for replies to a certain comment */
  @Test
  public void testReplyRequest() throws IOException {
    String parameterName = PARENT_ID_PROPERTY;
    String parameterVal = "1000";

    Comment[] expectedReturnedComments =
        new Comment[] {
          generateComment(
              createCommentEntity(
                  /*timestamp*/ 2, /*userId*/ 1, /*businessId*/ 0, /*parentId*/ 1000)),
          generateComment(
              createCommentEntity(
                  /*timestamp*/ 1, /*userId*/ 0, /*businessId*/ 0, /*parentId*/ 1000)),
        };

    runTest(parameterName, parameterVal, expectedReturnedComments);
  }

  /** Test that the server rejects requests without arguments. */
  @Test
  public void testRejectsRequestsWithNoArguments() throws IOException {
    initDatastore(ds);
    servlet.doGet(request, response);

    assertResponseWithArbitraryTextRaised(HttpServletResponse.SC_BAD_REQUEST);
  }

  /** Test that the server rejects requests with two arguements. */
  @Test
  public void testRejectsRequestsWithTwoArguments() throws IOException {
    initDatastore(ds);

    doReturn("0").when(request).getParameter(USER_ID_PROPERTY);
    doReturn("0").when(request).getParameter(BUSINESS_ID_PROPERTY);

    servlet.doGet(request, response);

    assertResponseWithArbitraryTextRaised(HttpServletResponse.SC_BAD_REQUEST);
  }

  /** Test that the server rejects requests with two arguements. */
  @Test
  public void testRejectsRequestsWithThreeArguments() throws IOException {
    initDatastore(ds);

    doReturn("0").when(request).getParameter(USER_ID_PROPERTY);
    doReturn("0").when(request).getParameter(BUSINESS_ID_PROPERTY);
    doReturn("100").when(request).getParameter(PARENT_ID_PROPERTY);

    servlet.doGet(request, response);

    assertResponseWithArbitraryTextRaised(HttpServletResponse.SC_BAD_REQUEST);
  }
}
