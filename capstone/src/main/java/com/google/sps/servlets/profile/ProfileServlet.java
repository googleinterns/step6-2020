package com.google.sps.servlets.profile;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.UserProfile;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet responsible for showing a non-business user profile. */
@WebServlet("/profile/*")
public class ProfileServlet extends HttpServlet {
  
  UserService userService = UserServiceFactory.getUserService();

  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  public ProfileServlet() {}

  public ProfileServlet(UserService userService, DatastoreService datastore) {
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

    // If userId is a business owner id, redirect to "profile not found" page.
    String isBusiness = entity.hasProperty("isBusiness") ? (String) entity.getProperty("isBusiness") : "";
    if (isBusiness.equals("Yes")) {
      response.sendError(
          HttpServletResponse.SC_NOT_FOUND,
          "The profile you were looking for was not found in our records!");
      return;
    }

    // Query all profile properties.
    String id = entity.getKey().getName();
    String name = entity.hasProperty("name") ? (String) entity.getProperty("name") : "Anonymous";
    String location = entity.hasProperty("location") ? (String) entity.getProperty("location") : "";
    String bio = entity.hasProperty("bio") ? (String) entity.getProperty("bio") : "";
    boolean isCurrentUser = userId.equals(id);

    // Create a profile object that contains the properties.
    UserProfile profile = new UserProfile(id, name, location, bio, isCurrentUser);

    // Send it back to client side as a JSON file.
    response.setContentType("application/json;");
    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(profile));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String id = userService.getCurrentUser().getUserId();

    // Check if user is logged in.
    if (id == null) {
      response.sendError(
          HttpServletResponse.SC_NOT_FOUND,
          "You don't have permission to perform this action!");
      return;
    }

    // Mandatory property "name" needs to be filled out. If not, send an error.
    if (request.getParameter("name") == null) {
      response.sendError(
          HttpServletResponse.SC_NOT_FOUND,
          "Required field: name was not filled out.");
      return;
    }
    
    // Update properties in datastore.
    Entity profileEntity = new Entity("UserProfile", id);
    profileEntity.setProperty("isBusiness", request.getParameter("isBusiness"));
    profileEntity.setProperty("name", request.getParameter("name"));
    profileEntity.setProperty("location", request.getParameter("location"));
    profileEntity.setProperty("bio", request.getParameter("bio"));

    // Put entity in datastore.
    datastore.put(profileEntity);

    response.sendRedirect("/profile.html?id=" + id);
  }
}
