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

@WebServlet("/check_new_user")
public class NewUserServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // TODO: Check if the user is a new user or not. Then redirect accordingly.
    UserService userService = UserServiceFactory.getUserService();
    String userId = userService.getCurrentUser().getUserId();

    Key userKey = KeyFactory.createKey("User", userId);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    try {
      Entity ent = datastore.get(userKey);
    } catch (EntityNotFoundException e) {
      System.err.println("Could not find key: " + userKey);
      Entity userEntity = new Entity("User", userId);
      datastore.put(userEntity);

      response.sendRedirect("/questionnaire.html");
      return;
    }

    response.sendRedirect("/index.html");
  }
}
