package com.google.sps.servlets.profile;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.sps.data.UserProfile;
import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public class ProfileServletTest {

  private LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalUserServiceTestConfig())
          .setEnvIsAdmin(true)
          .setEnvIsLoggedIn(true);

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private UserService userService;

  @Mock private DatastoreService datastore;

  private static final String NAME = "John Doe";
  private static final String NO_NAME = null;
  private static final String LOCATION = "Mountain View, CA";
  private static final String BIO = "This is my bio.";
  private static final String USER_ID = "12345";
  private static final String INVALID_USER_ID = null;
  private static final String EMAIL = "abc@gmail.com";
  private static final String AUTHDOMAIN = "gmail.com";
  private static final String PATHINFO = "profile/12345";
  private static final String INVALID_PATHINFO = "profile";


  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    helper.setUp();
  }

  @After
  public void tearDown() throws Exception {
    helper.tearDown();
  }

  /*
   *  Test doGet() for when user enters an invalid URL param. It should return an error.
   **/
  @Test
  public void invalidUrlParamReturnError() throws ServletException, IOException {
    when(request.getPathInfo()).thenReturn(INVALID_PATHINFO);

    ProfileServlet userServlet = new ProfileServlet(userService, datastore);
    userServlet.doGet(request, response);

    // verify if a sendError() was performed with the expected values.
    Mockito.verify(response, Mockito.times(1))
        .sendError(Mockito.eq(HttpServletResponse.SC_NOT_FOUND), Mockito.anyString());
  }

  /*
   *  Test doGet() for when datastore cannot find entity key. User does not exist in datastore.
   *  It should return an error.
   **/
  @Test
  public void userNotInDatastoreReturnError() throws ServletException, IOException, EntityNotFoundException {
    when(request.getPathInfo()).thenReturn(PATHINFO);

    // Create an entity with this USER_ID.
    String keyString = KeyFactory.createKeyString("UserProfile", USER_ID);
    Key userKey = KeyFactory.stringToKey(keyString);
    Entity ent = new Entity("UserProfile", USER_ID);

    when(datastore.get(userKey)).thenThrow(EntityNotFoundException.class);
  
    ProfileServlet userServlet = new ProfileServlet(userService, datastore);
    userServlet.doGet(request, response);

    // verify if a sendError() was performed with the expected values.
    Mockito.verify(response, Mockito.times(1))
        .sendError(Mockito.eq(HttpServletResponse.SC_NOT_FOUND), Mockito.anyString());
  }

  /*
   *  Test doGet() for when user is a business owner, it should return a response error.
   **/
  @Test
  public void businessUserReturnError() throws ServletException, IOException, EntityNotFoundException {
    when(request.getPathInfo()).thenReturn(PATHINFO);

    // Create an entity with this USER_ID and set it's property "isBusiness" to "Yes".
    // Then add this to datastore.
    Key userKey = KeyFactory.createKey("UserProfile", USER_ID);
    Entity ent = new Entity("UserProfile", USER_ID);

    String isBusiness = "Yes";

    ent.setProperty("isBusiness", isBusiness);
    ent.setProperty("name", NAME);
    ent.setProperty("location", LOCATION);
    ent.setProperty("bio", BIO);

    try {    
      when(datastore.get(userKey)).thenReturn(ent);
    } catch (EntityNotFoundException e) {
      System.out.println("Could not find key: " + userKey);
      return;
    }

    ProfileServlet userServlet = new ProfileServlet(userService, datastore);
    userServlet.doGet(request, response);

    // verify if a sendError() was performed with the expected values.
    Mockito.verify(response, Mockito.times(1))
        .sendError(Mockito.eq(HttpServletResponse.SC_NOT_FOUND), Mockito.anyString());
  }

  /*
   *  Test doGet() for when user not a business owner, it should return a JSON file of profile page information.
   **/
  @Test
  public void nonBusinessUserReturnJsonFile() throws ServletException, IOException {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);
    when(request.getPathInfo()).thenReturn(PATHINFO);

    // Create an entity with this USER_ID and set it's property "isBusiness" to "No".
    // Then add this to datastore.
    Key userKey = KeyFactory.createKey("UserProfile", USER_ID);
    Entity ent = new Entity("UserProfile", USER_ID);

    String isBusiness = "No";
    boolean isCurrentUser = true;

    ent.setProperty("isBusiness", isBusiness);
    ent.setProperty("name", NAME);
    ent.setProperty("location", LOCATION);
    ent.setProperty("bio", BIO);

    try {    
      when(datastore.get(userKey)).thenReturn(ent);
    } catch (EntityNotFoundException e) {
      System.out.println("Could not find key: " + userKey);
      return;
    }

    ProfileServlet userServlet = new ProfileServlet(userService, datastore);
    userServlet.doGet(request, response);

    // verify that it sends a JSON file to response.
    UserProfile profile = new UserProfile(USER_ID, NAME, LOCATION, BIO, isCurrentUser);

    String responseString = stringWriter.getBuffer().toString().trim();
    JsonElement responseJsonElement = new JsonParser().parse(responseString);

    Gson gson = new GsonBuilder().create();
    JsonElement userJsonElement = gson.toJsonTree(profile);

    JsonObject responseJsonObject = responseJsonElement.getAsJsonObject();
    JsonObject userJsonObject = userJsonElement.getAsJsonObject();

    Assert.assertEquals(responseJsonObject, userJsonObject);
  }

  /*
   *  Test doPost() for when the user does not exist and they want to edit a profile. It should return error.
   **/
  @Test
  public void editProfileUserNotFoundReturnError() throws ServletException, IOException, EntityNotFoundException {
    User user = new User(EMAIL, AUTHDOMAIN, INVALID_USER_ID);
    when(userService.getCurrentUser()).thenReturn(user);

    ProfileServlet userServlet = new ProfileServlet(userService, datastore);
    userServlet.doPost(request, response);

    // verify if a sendError() was performed with the expected values.
    Mockito.verify(response, Mockito.times(1))
        .sendError(Mockito.eq(HttpServletResponse.SC_NOT_FOUND), Mockito.anyString());
  }

  /*
   *  Test doPost() for when the user did not fill out the name section. It should return error.
   **/
  @Test
  public void editProfileNameNotFilledReturnError() throws ServletException, IOException, EntityNotFoundException {
    User user = new User(EMAIL, AUTHDOMAIN, INVALID_USER_ID);
    when(userService.getCurrentUser()).thenReturn(user);

    String isBusiness = "No";

    when(request.getParameter("isBusiness")).thenReturn(isBusiness);
    when(request.getParameter("name")).thenReturn(NO_NAME);
    when(request.getParameter("location")).thenReturn(LOCATION);
    when(request.getParameter("bio")).thenReturn(BIO);

    Key userKey = KeyFactory.createKey("UserProfile", USER_ID);
    Entity ent = new Entity("UserProfile", USER_ID);

    ProfileServlet userServlet = new ProfileServlet(userService, datastore);
    userServlet.doPost(request, response);

    // verify if a sendError() was performed with the expected values.
    Mockito.verify(response, Mockito.times(1))
        .sendError(Mockito.eq(HttpServletResponse.SC_NOT_FOUND), Mockito.anyString());
  }

  /*
   *  Test doPost() for when user is editing their profile page, it should put correct information into datastore.
   **/
  @Test
  public void userEditProfileAddToDatastore() throws ServletException, IOException {
    User user = new User(EMAIL, AUTHDOMAIN, USER_ID);
    when(userService.getCurrentUser()).thenReturn(user);

    String isBusiness = "No";

    when(request.getParameter("isBusiness")).thenReturn(isBusiness);
    when(request.getParameter("name")).thenReturn(NAME);
    when(request.getParameter("location")).thenReturn(LOCATION);
    when(request.getParameter("bio")).thenReturn(BIO);

    Key userKey = KeyFactory.createKey("UserProfile", USER_ID);
    Entity ent = new Entity("UserProfile", USER_ID);

    ProfileServlet userServlet = new ProfileServlet(userService, datastore);
    userServlet.doPost(request, response);

    ArgumentCaptor<Entity> captor = ArgumentCaptor.forClass(Entity.class);
    verify(datastore).put(captor.capture());

    Entity capEntity = captor.getValue();

    Assert.assertEquals(capEntity.getProperty("isBusiness"), isBusiness);
    Assert.assertEquals(capEntity.getProperty("name"), NAME);
    Assert.assertEquals(capEntity.getProperty("location"), LOCATION);
    Assert.assertEquals(capEntity.getProperty("bio"), BIO);
  }
}
