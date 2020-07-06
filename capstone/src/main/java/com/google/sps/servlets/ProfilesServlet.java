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

/** Servlet responsible for listing all profiles. */
@WebServlet("/profiles")
public class ProfilesServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Query profile entities from datastore.
    Query query = new Query("Profile");

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    // Retreive entities.
    List<Entity> entities = results.asList();

    // Convert entities to Profile objects.
    List<Profile> profiles = new ArrayList<>();
    for (Entity entity : entities) {
      String id = entity.getKey().getId();
      String name = entity.getProperty("name");
      String location = entity.getProperty("location");
      String bio = entity.getProperty("bio");
      String story = entity.getProperty("story");
      String about = entity.getProperty("about");
      String support = entity.getProperty("support");

      Profile profile = new Profile(id, name, location, bio, story, about, support);
      profiles.add(profile);
    }

    response.setContentType("application/json;");
    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(profiles));
  }
}