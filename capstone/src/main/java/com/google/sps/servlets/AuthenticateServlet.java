package com.google.sps.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.User;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/auth")
public class AuthenticateServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    User userData;
    if (userService.isUserLoggedIn()) {
      String logoutUrl = userService.createLogoutURL("/index.html");
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
