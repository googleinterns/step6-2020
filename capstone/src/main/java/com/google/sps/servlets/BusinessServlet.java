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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.BusinessProfile;
import java.io.IOException;
import java.util.Objects;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that handles business information. */
@WebServlet("/business/*")
public class BusinessServlet extends HttpServlet {

  private static final String USER_TASK = "UserProfile";
  private static final String IS_BUSINESS_PROPERTY = "isBusiness";
  private static final String NAME_PROPERTY = "name";
  private static final String BIO_PROPERTY = "bio";
  private static final String LOCATION_PROPERTY = "location";
  private static final String STORY_PROPERTY = "story";
  private static final String ABOUT_PROPERTY = "about";
  private static final String SUPPORT_PROPERTY = "support";
  private static final String CALENDAR_PROPERTY = "calendarEmail";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Retrieve the business ID that is located in place of the * in the URL.
    // request.getPathInfo() returns "/{id}" and substring(1) would return "{id}" without "/".
    String businessID = request.getPathInfo().substring(1);

    // Retrieve all of the information for a single business to be displayed.
    Query businessQuery =
        new Query(USER_TASK)
            .setFilter(
                CompositeFilterOperator.and(
                    FilterOperator.EQUAL.of(IS_BUSINESS_PROPERTY, "Yes"),
                    FilterOperator.EQUAL.of(
                        Entity.KEY_RESERVED_PROPERTY,
                        KeyFactory.createKey(USER_TASK, businessID))));

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery queryResults = datastore.prepare(businessQuery);
    Entity businessEntity = queryResults.asSingleEntity();

    if (businessEntity != null) {
      String id = (String) businessEntity.getKey().getName();
      String name = (String) businessEntity.getProperty(NAME_PROPERTY);
      String email = (String) businessEntity.getProperty(CALENDAR_PROPERTY);
      String bio = (String) businessEntity.getProperty(BIO_PROPERTY);
      String location = (String) businessEntity.getProperty(LOCATION_PROPERTY);
      String story = (String) businessEntity.getProperty(STORY_PROPERTY);
      String about = (String) businessEntity.getProperty(ABOUT_PROPERTY);
      String support = (String) businessEntity.getProperty(SUPPORT_PROPERTY);
      BusinessProfile business =
          new BusinessProfile(id, name, email, bio, location, story, about, support);

      Gson gson = new Gson();
      String jsonBusiness = gson.toJson(business);
      response.setContentType("application/json");
      response.getWriter().println(jsonBusiness);
    } else {
      response.sendError(
          HttpServletResponse.SC_NOT_FOUND,
          "The business you were looking was not found in our records!");
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Check if user is logged in.
    UserService userService = UserServiceFactory.getUserService();
    if (userService.getCurrentUser() == null) {
      response.sendError(
          HttpServletResponse.SC_NOT_FOUND, "You don't have permission to perform this action!");
      return;
    }

    String id = userService.getCurrentUser().getUserId();

    // Mandatory property "name" needs to be filled out. If not, send an error.
    if (request.getParameter(NAME_PROPERTY) == null) {
      response.sendError(
          HttpServletResponse.SC_BAD_REQUEST, "Required field: name was not filled out.");
      return;
    }

    // Update properties in datastore.
    Entity businessEntity = new Entity(USER_TASK, id);

    businessEntity.setProperty(
        IS_BUSINESS_PROPERTY, Objects.toString(request.getParameter(IS_BUSINESS_PROPERTY), ""));
    businessEntity.setProperty(NAME_PROPERTY, request.getParameter(NAME_PROPERTY));
    businessEntity.setProperty(
        LOCATION_PROPERTY, Objects.toString(request.getParameter(LOCATION_PROPERTY), ""));
    businessEntity.setProperty(
        BIO_PROPERTY, Objects.toString(request.getParameter(BIO_PROPERTY), ""));
    businessEntity.setProperty(
        STORY_PROPERTY, Objects.toString(request.getParameter(STORY_PROPERTY), ""));
    businessEntity.setProperty(
        ABOUT_PROPERTY, Objects.toString(request.getParameter(ABOUT_PROPERTY), ""));
    businessEntity.setProperty(
        SUPPORT_PROPERTY, Objects.toString(request.getParameter(SUPPORT_PROPERTY), ""));

    // Verify that the email can be used to generate a google calendar.
    String calendarEmail = Objects.toString(request.getParameter(CALENDAR_PROPERTY), "");
    if (calendarEmail.contains("@gmail.com") || calendarEmail.contains("@google.com")) {
      businessEntity.setProperty(CALENDAR_PROPERTY, calendarEmail);
    } else {
      businessEntity.setProperty(CALENDAR_PROPERTY, "");
    }

    // Put entity in datastore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(businessEntity);

    response.sendRedirect("/business.html?id=" + id);
  }
}
