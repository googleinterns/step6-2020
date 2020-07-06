package com.google.sps.servlets;

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
import com.google.gson.Gson;
import com.google.sps.data.Profile;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet responsible for showing a profile. */
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

    Key userKey = KeyFactory.createKey("Profile", userId);
    Entity entity = datastore.get(userKey);

    // Query all profile properties.
    String id = entity.getKey().getId();
    String name = entity.getProperty("name");
    String location = entity.getProperty("location");
    String bio = entity.getProperty("bio");
    String story = entity.getProperty("story");
    String about = entity.getProperty("about");
    String support = entity.getProperty("support");

    // Create a profile object that contains the properties.
    Profile profile = new Profile(id, name, location, bio, story, about, support);

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
    String story = request.getParameter("story");
    String about = request.getParameter("about");
    String support = request.getParameter("support");

    // Update properties in datastore.
    Entity profileEntity = new Entity("Profile", id);
    profileEntity.setProperty("isBusiness", isBusiness);
    profileEntity.setProperty("name", name);
    profileEntity.setProperty("location", location);
    profileEntity.setProperty("bio", bio);
    profileEntity.setProperty("story", story);
    profileEntity.setProperty("about", about);
    profileEntity.setProperty("support", support);

    // Put entity in datastore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(profileEntity);

    response.sendRedirect("/profile.html");
  }
}
