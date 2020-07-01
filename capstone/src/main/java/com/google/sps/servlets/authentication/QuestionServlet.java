package com.google.sps.servlets.authentication;

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
*  If it's a new user logging into the webpage, there will be a
*  question to answer. After the question is answered, the user
*  will be added to the database and stay signed in. Otherwise, 
*  the user will be signed out and is not added to database.
*/
@WebServlet("/question")
public class QuestionServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Obtain the input for whether they are a business owner or not.
    String isBusiness = request.getParameter("isBusiness");
    
    // Create a key from user sign in ID and check if it's in the database.
    UserService userService = UserServiceFactory.getUserService();
    String userId = userService.getCurrentUser().getUserId();

    Key userKey = KeyFactory.createKey("User", userId);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService(); 
    Entity userEntity = new Entity("User", userId);

    // If the user answered the question, add new user into database.
    if (isBusiness.equals("Yes") || isBusiness.equals("No")) {
      userEntity.setProperty("business", isBusiness);
      datastore.put(userEntity);
      response.sendRedirect("/profile.html");
    } else {
      response.sendRedirect("/logout");
    }
  } 
}