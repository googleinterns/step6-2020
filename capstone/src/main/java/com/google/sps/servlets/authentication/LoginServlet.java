package com.google.sps.servlets.authentication;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.User;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Authentication for the webpage. Uses Users API for authentication functionality. Create
 * login/logout url links for client side. When signed in, redirect to a servlet to determine
 * whether it's a new user or not.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
  
  private UserService userService = UserServiceFactory.getUserService();
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  
  private static final String IS_BUSINESS = "isBusiness";
  private static final String SUPPORT_PROPERTY = "support";
  private final String loginUrl = userService.createLoginURL("/check_new_user");
  private final String logoutUrl = "/logout";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    User userData;
    Entity entity;

    if (userService.isUserLoggedIn()) {
      String userId = userService.getCurrentUser().getUserId();
      String keyString = KeyFactory.createKeyString("UserProfile", userId);
      Key userKey = KeyFactory.stringToKey(keyString);

      try {
        entity = datastore.get(userKey);
      } catch (EntityNotFoundException e) {
        response.sendError(
          HttpServletResponse.SC_NOT_FOUND,
          "The profile you were looking for was not found in our records!");
        return;
      }
      
      String isBusiness = (String) entity.getProperty(IS_BUSINESS);
      userData = new User(true, logoutUrl, userId, isBusiness);
    } else {
      userData = new User(false, loginUrl, null, null);
    }

    response.setContentType("application/json;");
    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(userData));
  }
}
