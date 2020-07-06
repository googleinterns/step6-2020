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
 * Authentication for the webpage. Uses Users API for authentication functionality. Create
 * login/logout url links for client side. When signed in, redirect to a servlet to determine
 * whether it's a new user or not.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

  UserService userService = UserServiceFactory.getUserService();

  public LoginServlet() {}

  public LoginServlet(UserService userService) {
    this.userService = userService;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String loginUrl = userService.createLoginURL("/check_new_user");
    String logoutUrl = "/logout";
    User userData;

    if (userService.isUserLoggedIn()) {
      userData = new User(true, logoutUrl);
    } else {
      userData = new User(false, loginUrl);
    }

    response.setContentType("application/json;");
    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(userData));
  }
}
