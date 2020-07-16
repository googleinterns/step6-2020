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

import static com.google.sps.data.ProfileDatastoreUtil.ABOUT_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.BIO_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.IS_BUSINESS_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.LOCATION_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.NAME_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.NO;
import static com.google.sps.data.ProfileDatastoreUtil.PROFILE_TASK_NAME;
import static com.google.sps.data.ProfileDatastoreUtil.STORY_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.SUPPORT_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.YES;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.BusinessProfile;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet responsible for showing a business user profile. */
@WebServlet("/business/*")
public class BusinessServlet extends HttpServlet {
  UserService userService = UserServiceFactory.getUserService();

  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Obtain userId from param URL.
    String[] pathSegments = request.getPathInfo().split("/");
    if (pathSegments.length < 2) {
      response.sendError(
          HttpServletResponse.SC_NOT_FOUND,
          "The profile you were looking for was not found in our records!");
      return;
    }

    String userId = pathSegments[1];

    String keyString = KeyFactory.createKeyString(PROFILE_TASK_NAME, userId);
    Key userKey = KeyFactory.stringToKey(keyString);
    Entity entity;

    try {
      entity = datastore.get(userKey);
    } catch (EntityNotFoundException e) {
      System.err.println("Could not find key: " + userKey);
      response.sendError(
          HttpServletResponse.SC_NOT_FOUND,
          "The profile you were looking for was not found in our records!");
      return;
    }

    // If userId is not a business owner id, redirect to "profile not found" page.
    String isBusiness =
        entity.hasProperty(IS_BUSINESS_PROPERTY)
            ? (String) entity.getProperty(IS_BUSINESS_PROPERTY)
            : "";
    if (!isBusiness.equals(YES)) {
      response.sendError(
          HttpServletResponse.SC_NOT_FOUND,
          "The profile you were looking for was not found in our records!");
      return;
    }

    // Query all profile properties.
    String id = entity.getKey().getName();
    String name =
        entity.hasProperty(NAME_PROPERTY)
            ? (String) entity.getProperty(NAME_PROPERTY)
            : "Anonymous";
    String location =
        entity.hasProperty(LOCATION_PROPERTY) ? (String) entity.getProperty(LOCATION_PROPERTY) : "";
    String bio = entity.hasProperty(BIO_PROPERTY) ? (String) entity.getProperty(BIO_PROPERTY) : "";
    String story =
        entity.hasProperty(STORY_PROPERTY) ? (String) entity.getProperty(STORY_PROPERTY) : "";
    String about =
        entity.hasProperty(ABOUT_PROPERTY) ? (String) entity.getProperty(ABOUT_PROPERTY) : "";
    String support =
        entity.hasProperty(SUPPORT_PROPERTY) ? (String) entity.getProperty(SUPPORT_PROPERTY) : "";
    boolean isCurrentUser = userId.equals(id);

    // Create a profile object that contains the properties.
    BusinessProfile profile =
        new BusinessProfile(id, name, location, bio, story, about, support, isCurrentUser);

    // Send it back to client side as a JSON file.
    response.setContentType("application/json;");
    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(profile));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Check if user is logged in.
    if (userService.getCurrentUser() == null) {
      response.sendError(
          HttpServletResponse.SC_NOT_FOUND, "You don't have permission to perform this action!");
      return;
    }

    String id = userService.getCurrentUser().getUserId();

    // Mandatory property "name" needs to be filled out. If not, send an error.
    if (request.getParameter(NAME_PROPERTY) == null) {
      response.sendError(
          HttpServletResponse.SC_NOT_FOUND, "Required field: name was not filled out.");
      return;
    }

    // Update properties in datastore.
    Entity businessEntity = new Entity(PROFILE_TASK_NAME, id);

    // If user is a non-business owner, return error.
    if (getParam(IS_BUSINESS_PROPERTY, request).equals(NO)) {
      response.sendError(
          HttpServletResponse.SC_NOT_FOUND, "You don't have permission to perform this action!");
      return;
    }

    businessEntity.setProperty(IS_BUSINESS_PROPERTY, getParam(IS_BUSINESS_PROPERTY, request));
    businessEntity.setProperty(NAME_PROPERTY, request.getParameter("name"));
    businessEntity.setProperty(LOCATION_PROPERTY, getParam(LOCATION_PROPERTY, request));
    businessEntity.setProperty(BIO_PROPERTY, getParam(BIO_PROPERTY, request));
    businessEntity.setProperty(STORY_PROPERTY, getParam(STORY_PROPERTY, request));
    businessEntity.setProperty(ABOUT_PROPERTY, getParam(ABOUT_PROPERTY, request));
    businessEntity.setProperty(SUPPORT_PROPERTY, getParam(SUPPORT_PROPERTY, request));

    // Put entity in datastore.
    datastore.put(businessEntity);

    response.sendRedirect("/business.html?id=" + id);
  }

  public String getParam(String property, HttpServletRequest request) {
    if (request.getParameter(property) == null) {
      return "";
    }

    return request.getParameter(property);
  }
}
