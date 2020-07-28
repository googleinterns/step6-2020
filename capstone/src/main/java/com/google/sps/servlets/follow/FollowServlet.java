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
import static com.google.sps.data.FollowDatastoreUtil.buildFollowEntity;
import static com.google.sps.data.ProfileDatastoreUtil.IS_BUSINESS_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.PROFILE_TASK_NAME;
import static com.google.sps.data.ProfileDatastoreUtil.YES;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.Arrays;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet for adding and removing 'follows', that is instances of a User following a business. */
@WebServlet("/follow")
public class FollowServlet extends HttpServlet {
  private final int ENTITY_COUNTING_LIMIT = 1;

  private UserService userService = UserServiceFactory.getUserService();
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    User currentUser = userService.getCurrentUser();

    // Verify that the user is logged in
    if (currentUser == null) {
      response.sendError(
          HttpServletResponse.SC_UNAUTHORIZED, "User must be logged in to follow a business.");
      return;
    }

    String businessId = request.getParameter(BUSINESS_ID_PROPERTY);

    if (businessId == null) {
      response.sendError(
          HttpServletResponse.SC_BAD_REQUEST,
          "Please specify the ID of the business you would like to follow.");
      return;
    }
    if (!doesBusinessExist(businessId)) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "Business not found.");
      return;
    }

    String userId = currentUser.getUserId();
    if (followExistsInDatastore(userId, businessId)) {
      response.sendError(
          HttpServletResponse.SC_BAD_REQUEST, "Cannot follow the same business twice.");
      return;
    }
    if (userId.equals(businessId)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You cannot follow yourself.");
      return;
    }

    datastore.put(buildFollowEntity(userId, businessId));

    response.sendRedirect("/business.html?id=" + businessId);
  }

  private boolean doesBusinessExist(String businessId) {
    Key key = KeyFactory.createKey(PROFILE_TASK_NAME, businessId);

    try {
      Entity businessEntity = datastore.get(key);
      return businessEntity.getProperty(IS_BUSINESS_PROPERTY).equals(YES);
    } catch (EntityNotFoundException e) {
      return false;
    }
  }

  private boolean followExistsInDatastore(String userId, String businessId) {
    int numberOfMatches = prepareFollowQuery(userId, businessId).countEntities(withDefaults());

    return numberOfMatches > 0;
  }

  private PreparedQuery prepareFollowQuery(String userId, String businessId) {
    Query followQuery =
        new Query(FOLLOW_TASK_NAME)
            .setFilter(
                new CompositeFilter(
                    CompositeFilterOperator.AND,
                    Arrays.asList(
                        new FilterPredicate(USER_ID_PROPERTY, FilterOperator.EQUAL, userId),
                        new FilterPredicate(
                            BUSINESS_ID_PROPERTY, FilterOperator.EQUAL, businessId))));

    return datastore.prepare(followQuery);
  }

  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    User currentUser = userService.getCurrentUser();
    if (currentUser == null) {
      response.sendError(
          HttpServletResponse.SC_UNAUTHORIZED, "User must be logged in to unfollow a business.");
      return;
    }

    String businessId = request.getParameter(BUSINESS_ID_PROPERTY);
    if (businessId == null) {
      response.sendError(
          HttpServletResponse.SC_BAD_REQUEST,
          "Please specify the ID of the business you would like to unfollow.");
      return;
    }

    String userId = currentUser.getUserId();
    if (!followExistsInDatastore(userId, businessId)) {
      response.sendError(
          HttpServletResponse.SC_NOT_FOUND,
          "In order to unfollow a business you must be following it.");
      return;
    }

    Entity followToDelete = prepareFollowQuery(userId, businessId).asList(withDefaults()).get(0);

    datastore.delete(followToDelete.getKey());

    response.sendRedirect("/business.html?id=" + businessId);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    User currentUser = userService.getCurrentUser();
    if (currentUser == null) {
      response.sendError(
          HttpServletResponse.SC_UNAUTHORIZED, "User must be logged in to make a get request.");
      return;
    }

    String businessId = request.getParameter(BUSINESS_ID_PROPERTY);
    if (businessId == null) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Must specify a business ID.");
      return;
    }

    response.setContentType("application/json;");
    response
        .getWriter()
        .println(new Gson().toJson(followExistsInDatastore(currentUser.getUserId(), businessId)));
  }
}
