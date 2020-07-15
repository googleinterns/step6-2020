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

  private static final String IS_BUSINESS_PROPERTY = "isBusiness";
  private static final String NAME_PROPERTY = "name";
  private static final String LOCATION_PROPERTY = "location";
  private static final String BIO_PROPERTY = "bio";
  private static final String ANONYMOUS_NAME = "Anonymous";
  private static final String DEFAULT = "";
  private static final String NO = "No";

  UserService userService = UserServiceFactory.getUserService();

  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Create a key from user sign in ID and check if it's in the database.
    String userId = userService.getCurrentUser().getUserId();

    String keyString = KeyFactory.createKeyString("UserProfile", userId);
    Key userKey = KeyFactory.stringToKey(keyString);

    try {
      Entity entity = datastore.get(userKey);
    } catch (EntityNotFoundException e) {
      // Add user to database with default values.
      Entity userEntity = new Entity("UserProfile", userId);
      userEntity.setProperty(IS_BUSINESS_PROPERTY, NO);
      userEntity.setProperty(NAME_PROPERTY, ANONYMOUS_NAME);
      userEntity.setProperty(LOCATION_PROPERTY, DEFAULT);
      userEntity.setProperty(BIO_PROPERTY, DEFAULT);

      datastore.put(userEntity);
    }

    response.sendRedirect("/index.html");
  }
}
