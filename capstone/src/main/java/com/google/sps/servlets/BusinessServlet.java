package com.google.sps.servlets;

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

  private static final String IS_BUSINESS_PROPERTY = "isBusiness";
  private static final String NAME_PROPERTY = "name";
  private static final String LOCATION_PROPERTY = "location";
  private static final String BIO_PROPERTY = "bio";
  private static final String STORY_PROPERTY = "story";
  private static final String ABOUT_PROPERTY = "about";
  private static final String SUPPORT_PROPERTY = "support";

  UserService userService = UserServiceFactory.getUserService();

  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  public BusinessServlet() {}

  public BusinessServlet(UserService userService, DatastoreService datastore) {
    this.userService = userService;
    this.datastore = datastore;
  }

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

    String keyString = KeyFactory.createKeyString("UserProfile", userId);
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
        entity.hasProperty(IS_BUSINESS_PROPERTY) ? (String) entity.getProperty(IS_BUSINESS_PROPERTY) : "";
    if (isBusiness.equals("No")) {
      response.sendError(
          HttpServletResponse.SC_NOT_FOUND,
          "The profile you were looking for was not found in our records!");
      return;
    }

    // Query all profile properties.
    String id = entity.getKey().getName();
    String name = (String) entity.getProperty(NAME_PROPERTY);
    String location = (String) entity.getProperty(LOCATION_PROPERTY);
    String bio = (String) entity.getProperty(BIO_PROPERTY);
    String story = (String) entity.getProperty(STORY_PROPERTY);
    String about = (String) entity.getProperty(ABOUT_PROPERTY);
    String support = (String) entity.getProperty(SUPPORT_PROPERTY);
    boolean isCurrentUser = userId.equals(id);

    // Create a profile object that contains the properties.
    BusinessProfile profile = new BusinessProfile(id, name, location, bio, story, about, support, isCurrentUser);

    // Send it back to client side as a JSON file.
    response.setContentType("application/json;");
    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(profile));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String id;

    // Check if user is logged in.
    try {
      id = userService.getCurrentUser().getUserId();
    } catch (NullPointerException e) {
      response.sendError(
          HttpServletResponse.SC_NOT_FOUND, "You don't have permission to perform this action!");
      return;
    }

    // Mandatory property "name" needs to be filled out. If not, send an error.
    if (request.getParameter(NAME_PROPERTY) == null) {
      response.sendError(
          HttpServletResponse.SC_NOT_FOUND,
          "Required field: name was not filled out.");
      return;
    }

    // Put entity in datastore.
    datastore.put(setBusinessEntity(request, id));

    response.sendRedirect("/business.html?id=" + id);
  }
  
  // Update properties in datastore.
  public Entity setBusinessEntity(HttpServletRequest request, String id) {
    Entity businessEntity = new Entity("UserProfile", id);

    businessEntity.setProperty(IS_BUSINESS_PROPERTY, getParam(IS_BUSINESS_PROPERTY, request));
    businessEntity.setProperty(NAME_PROPERTY, getParam(NAME_PROPERTY, request));
    businessEntity.setProperty(LOCATION_PROPERTY, getParam(LOCATION_PROPERTY, request));
    businessEntity.setProperty(BIO_PROPERTY, getParam(BIO_PROPERTY, request));
    businessEntity.setProperty(STORY_PROPERTY, getParam(STORY_PROPERTY, request));
    businessEntity.setProperty(ABOUT_PROPERTY, getParam(ABOUT_PROPERTY, request));
    businessEntity.setProperty(SUPPORT_PROPERTY, getParam(SUPPORT_PROPERTY, request));

    return businessEntity;
  }

  public String getParam(String property, HttpServletRequest request) {
    if (request.getParameter(property) == null) {
      return "";
    }

    return request.getParameter(property);
  }
}
