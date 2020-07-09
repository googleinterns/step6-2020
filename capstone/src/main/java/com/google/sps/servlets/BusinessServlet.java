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
import com.google.sps.data.UserProfile;
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
      response.sendError(
          HttpServletResponse.SC_NOT_FOUND,
          "The profile you were looking for was not found in our records!");
      return;
    }

    String userId = idArray[1];

    Key userKey = KeyFactory.createKey("UserProfile", userId);
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
        entity.hasProperty("isBusiness") ? (String) entity.getProperty("isBusiness") : "";
    if (isBusiness.equals("No")) {
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
    String story = entity.hasProperty("story") ? (String) entity.getProperty("story") : "";
    String about = entity.hasProperty("about") ? (String) entity.getProperty("about") : "";
    String support = entity.hasProperty("support") ? (String) entity.getProperty("support") : "";
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
    String id = userService.getCurrentUser().getUserId();

    // Check if user is logged in.
    if (id == null) {
      response.sendError(
          HttpServletResponse.SC_NOT_FOUND, "You don't have permission to perform this action!");
      return;
    }

    // Update properties in datastore.
    Entity businessEntity = new Entity("UserProfile", id);
    businessEntity.setProperty("isBusiness", request.getParameter("isBusiness"));
    businessEntity.setProperty("name", request.getParameter("name"));
    businessEntity.setProperty("location", request.getParameter("location"));
    businessEntity.setProperty("bio", request.getParameter("bio"));
    businessEntity.setProperty("story", request.getParameter("story"));
    businessEntity.setProperty("about", request.getParameter("about"));
    businessEntity.setProperty("support", request.getParameter("support"));

    // Put entity in datastore.
    datastore.put(businessEntity);

    response.sendRedirect("/business.html?id=" + id);
  }
}
