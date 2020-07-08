package com.google.sps.servlets.authentication;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Check if user is a new user signing in or not. If it's a new user, add new user to database. */
@WebServlet("/check_new_user")
public class NewUserServlet extends HttpServlet {

  UserService userService = UserServiceFactory.getUserService();

  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  public NewUserServlet() {}

  public NewUserServlet(UserService userService, DatastoreService datastore) {
    this.userService = userService;
    this.datastore = datastore;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Create a key from user sign in ID and check if it's in the database.
    String userId = userService.getCurrentUser().getUserId();

    Key userKey = KeyFactory.createKey("UserProfile", userId);

    try {
      Entity entity = datastore.get(userKey);
    } catch (EntityNotFoundException e) {
      // Add user to database.
      Entity userEntity = new Entity("UserProfile", userId);
      datastore.put(userEntity);
    }

    response.sendRedirect("/index.html");
  }
}
