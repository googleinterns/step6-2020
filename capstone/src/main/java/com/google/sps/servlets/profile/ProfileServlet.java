package com.google.sps.servlets.profile;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.UserProfile;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet responsible for showing a non-business user profile. */
@WebServlet("/profile/*")
public class ProfileServlet extends HttpServlet {
  private static final String IS_BUSINESS_PROPERTY = "isBusiness";
  private static final String NAME_PROPERTY = "name";
  private static final String LOCATION_PROPERTY = "location";
  private static final String BIO_PROPERTY = "bio";

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

    String urlId = pathSegments[1];

    String keyString = KeyFactory.createKeyString("UserProfile", urlId);
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

    // If urlId is a business owner id, redirect to "profile not found" page.
    String isBusiness =
        entity.hasProperty(IS_BUSINESS_PROPERTY)
            ? (String) entity.getProperty(IS_BUSINESS_PROPERTY)
            : "";
    if (isBusiness.equals("Yes")) {
      response.sendError(
          HttpServletResponse.SC_NOT_FOUND,
          "The profile you were looking for was not found in our records!");
      return;
    }

    // Query all profile properties.
    String userId = userService.getCurrentUser().getUserId();
    String name =
        entity.hasProperty(NAME_PROPERTY)
            ? (String) entity.getProperty(NAME_PROPERTY)
            : "Anonymous";
    String location =
        entity.hasProperty(LOCATION_PROPERTY) ? (String) entity.getProperty(LOCATION_PROPERTY) : "";
    String bio = entity.hasProperty(BIO_PROPERTY) ? (String) entity.getProperty(BIO_PROPERTY) : "";
    boolean isCurrentUser = userId.equals(urlId);

    // Create a profile object that contains the properties.
    UserProfile profile = new UserProfile(userId, name, location, bio, isCurrentUser);

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

    // Mandatory property "name" needs to be filled out. If not, send an error.
    if (request.getParameter(NAME_PROPERTY) == null) {
      response.sendError(
          HttpServletResponse.SC_NOT_FOUND, "Required field: name was not filled out.");
      return;
    }

    String id = userService.getCurrentUser().getUserId();

    // Update properties in datastore.
    Entity profileEntity = new Entity("UserProfile", id);

    // If user is a business owner, return error.
    if (getParam(IS_BUSINESS_PROPERTY, request).equals("Yes")) {
      response.sendError(
          HttpServletResponse.SC_NOT_FOUND, "You don't have permission to perform this action!");
      return;
    }
    profileEntity.setProperty(IS_BUSINESS_PROPERTY, getParam(IS_BUSINESS_PROPERTY, request));
    profileEntity.setProperty(NAME_PROPERTY, getParam(NAME_PROPERTY, request));
    profileEntity.setProperty(LOCATION_PROPERTY, getParam(LOCATION_PROPERTY, request));
    profileEntity.setProperty(BIO_PROPERTY, getParam(BIO_PROPERTY, request));

    // Put entity in datastore.
    datastore.put(profileEntity);
    response.sendRedirect("/profile.html?id=" + id);
  }

  public String getParam(String property, HttpServletRequest request) {
    if (request.getParameter(property) == null) {
      return "";
    }

    return request.getParameter(property);
  }
}
