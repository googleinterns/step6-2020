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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that manages indidual comments */
@WebServlet("/comment/*")
public class CommentServlet extends HttpServlet {

  private final List<String> REQUIRED_PARAMETERS =
      new ArrayList<>(Arrays.asList(CONTENT_PROPERTY, BUSINESS_ID_PROPERTY));

  private UserService userService = UserServiceFactory.getUserService();
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Check if request has the right parameters
    for (String parameter : REQUIRED_PARAMETERS) {
      // Check if parameter is in the request
      if (request.getParameter(parameter) == null) {
        response.sendError(
            HttpServletResponse.SC_BAD_REQUEST,
            "Parameter \'" + parameter + "\' missing in request.");
        return;
      }
    }

    // Verify that a user is logged in
    User currentUser = userService.getCurrentUser();
    if (currentUser != null) {
      datastore.put(buildCommentEntity(request, currentUser.getUserId()));
    } else {
      response.sendError(
          HttpServletResponse.SC_UNAUTHORIZED, "User must be logged in to post comment");
    }

    response.sendRedirect("/business.html?id=" + request.getParameter(BUSINESS_ID_PROPERTY));
  }

  private Entity buildCommentEntity(HttpServletRequest request, String userId) {
    Entity commentEntity = new Entity(COMMENT_TASK_NAME);

    REQUIRED_PARAMETERS.forEach(
        parameter -> commentEntity.setProperty(parameter, request.getParameter(parameter)));

    commentEntity.setProperty(USER_ID_PROPERTY, userId);

    if (request.getParameter(PARENT_ID_PROPERTY) != null) {
      commentEntity.setProperty(PARENT_ID_PROPERTY, request.getParameter(PARENT_ID_PROPERTY));
    } else {
      commentEntity.setProperty(PARENT_ID_PROPERTY, "");
    }

    commentEntity.setProperty(TIMESTAMP_PROPERTY, System.currentTimeMillis());

    return commentEntity;
  }
}
