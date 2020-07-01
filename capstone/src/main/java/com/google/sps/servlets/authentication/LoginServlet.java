package com.google.sps.servlets.authentication;

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
*  Authentication for the webpage. Uses Users API for authentication functionality. 
*  Create login/logout url links for client side.
*  When signed in, redirect to a servlet to determine whether it's a new user or not. 
*/
@WebServlet("/auth")
public class LoginServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    User userData;

    // If user is logged in, redirect to home page. 
    // Otherwise, redirect to NewUserServlet to determine whether it's a new user signing in or not.
    if (userService.isUserLoggedIn()) {
      String logoutUrl = userService.createLogoutURL("/logout");
      userData = new User(true, logoutUrl);
    } else {
      String loginUrl = userService.createLoginURL("/check_new_user");
      userData = new User(false, loginUrl);
    }

    response.setContentType("application/json;");
    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(userData));
  }
}
