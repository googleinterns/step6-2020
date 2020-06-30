package com.google.sps.servlets;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
*  Check if user is a new user signing in or not.
*  If it's a new user, redirect to questionnaire page to determine whether they're a 
*  business owner. 
*  Add new user to database.
*/
@WebServlet("/check_new_user")
public class NewUserServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Create a key from user sign in ID and check if it's in the database.
    UserService userService = UserServiceFactory.getUserService();
    String userId = userService.getCurrentUser().getUserId();

    Key userKey = KeyFactory.createKey("User", userId);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    try {
      Entity ent = datastore.get(userKey);
    } catch (EntityNotFoundException e) {
      // Redirect to the questionnaire page.
      response.sendRedirect("/questionnaire.html");
      return;
    }

    // If it's a returning user, redirect to home page.
    response.sendRedirect("/index.html");
  }
}
