package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
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

/** Servlet responsible for showing a business user profile. */
@WebServlet("/business/*")
public class BusinessServlet extends HttpServlet {

  private static final String USER_TASK = "UserProfile";
  private static final String IS_BUSINESS_PROPERTY = "isBusiness";
  private static final String NAME_PROPERTY = "name";
  private static final String LOCATION_PROPERTY = "location";
  private static final String BIO_PROPERTY = "bio";
  private static final String STORY_PROPERTY = "story";
  private static final String ABOUT_PROPERTY = "about";
  private static final String SUPPORT_PROPERTY = "support";
  private static final String CALENDAR_PROPERTY = "calendarEmail";

  UserService userService = UserServiceFactory.getUserService();

  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

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

    Entity businessEntity = datastore.prepare(businessQuery).asSingleEntity();

    if (businessEntity == null) {
      response.sendError(
          HttpServletResponse.SC_NOT_FOUND,
          "The business you were looking was not found in our records!");
      return;
    }
    String id = (String) businessEntity.getKey().getName();
    String name = (String) businessEntity.getProperty(NAME_PROPERTY);
    String email = (String) businessEntity.getProperty(CALENDAR_PROPERTY);
    String bio = (String) businessEntity.getProperty(BIO_PROPERTY);
    String location = (String) businessEntity.getProperty(LOCATION_PROPERTY);
    String story = (String) businessEntity.getProperty(STORY_PROPERTY);
    String about = (String) businessEntity.getProperty(ABOUT_PROPERTY);
    String support = (String) businessEntity.getProperty(SUPPORT_PROPERTY);
    boolean isCurrentUser = businessID.equals(id);
    BusinessProfile business =
        new BusinessProfile(id, name, location, bio, story, about, email, support, isCurrentUser);

    Gson gson = new Gson();
    response.setContentType("application/json");
    response.getWriter().println(gson.toJson(business));

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Check if user is logged in.
    if (userService.getCurrentUser() == null) {
      response.sendError(
          HttpServletResponse.SC_FORBIDDEN, "You don't have permission to perform this action!");
      return;
    }

    // Mandatory property "name" needs to be filled out. If not, send an error.
    if (request.getParameter(NAME_PROPERTY) == null) {
      response.sendError(
          HttpServletResponse.SC_BAD_REQUEST, "Required field: name was not filled out.");
      return;
    }

    String id = userService.getCurrentUser().getUserId();

    // Update properties in datastore.
    Entity businessEntity = new Entity(USER_TASK, id);

    // If user is a non-business owner, return error.
    if (Objects.toString(request.getParameter(IS_BUSINESS_PROPERTY), "").equals("No")) {
      response.sendError(
          HttpServletResponse.SC_FORBIDDEN, "You don't have permission to perform this action!");
      return;
    }

    String[] propertyNames = {IS_BUSINESS_PROPERTY, NAME_PROPERTY, LOCATION_PROPERTY, BIO_PROPERTY, STORY_PROPERTY, ABOUT_PROPERTY, CALENDAR_PROPERTY, SUPPORT_PROPERTY};
    setEntityProperties(businessEntity, propertyNames);

    // Put entity in datastore.
    datastore.put(businessEntity);

    response.sendRedirect("/business.html?id=" + id);
  }

  private void setEntityProperties(Entity targetEntity, String[] propertyNames)  {
    for (String property: propertyNames) {
      targetEntity.setProperty(property, Objects.toString(request.getParameter(property), ""));
    }
  }
}
