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
    String[] idArray = request.getPathInfo().split("/");
    if (idArray.length < 2) {
        return;
    }

    String userId = idArray[1];

    Key userKey = KeyFactory.createKey("UserProfile", userId);
    Entity entity;

    try {
      entity = datastore.get(userKey);
    } catch (EntityNotFoundException e) {
      System.err.println("Could not find key: " + userKey);
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
    String name = entity.hasProperty("name") ? (String) entity.getProperty("name") : "";
    String location = entity.hasProperty("location") ? (String) entity.getProperty("location") : "";
    String bio = entity.hasProperty("bio") ? (String) entity.getProperty("bio") : "";
    boolean isCurrentUser = checkIsCurrentUser(userId, id);

    // Create a profile object that contains the properties.
    UserProfile profile = new UserProfile(id, name, location, bio, isCurrentUser);

    // Send it back to client side as a JSON file.
    response.setContentType("application/json;");
    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(profile));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // When user edits their profile page, fetch the data from the edit profile form.
    String id = userService.getCurrentUser().getUserId();
    String isBusiness = request.getParameter("isBusiness");
    String name = request.getParameter("name");
    String location = request.getParameter("location");
    String bio = request.getParameter("bio");

    // Update properties in datastore.
    Entity profileEntity = new Entity("UserProfile", id);
    profileEntity.setProperty("isBusiness", isBusiness);
    profileEntity.setProperty("name", name);
    profileEntity.setProperty("location", location);
    profileEntity.setProperty("bio", bio);

    // Put entity in datastore.
    datastore.put(profileEntity);

    response.sendRedirect("/index.html");
  }
  
  // Check if the current user is the same as the fetched user profile data.
  public boolean checkIsCurrentUser(String currentUserId, String fetchId) {
    return currentUserId.equals(fetchId);
  }
}
