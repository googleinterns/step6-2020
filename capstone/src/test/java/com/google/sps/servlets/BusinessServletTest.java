package com.google.sps.servlets;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.sps.data.BusinessProfile;
import java.io.*;
import java.util.HashMap;
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
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public class BusinessServletTest {

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  private static final String NAME = "Pizzeria";
  private static final String NO_NAME = null;
  private static final String LOCATION = "Mountain View, CA";
  private static final String BIO = "This is my business bio.";
  private static final String STORY = "The pandemic has affected my business in X many ways.";
  private static final String ABOUT = "Here is the Pizzeria's menu.";
  private static final String SUPPORT = "Please donate at X website.";
  private static final String USER_ID = "12345";
  private static final String INVALID_USER_ID = null;
  private static final String EMAIL = "abc@gmail.com";
  private static final String AUTHDOMAIN = "gmail.com";
  private static final String PATHINFO = "business/12345";
  private static final String INVALID_PATHINFO = "business";

  private LocalServiceTestHelper helper;

  private BusinessServlet userServlet;
  private DatastoreService datastore;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    helper =
        new LocalServiceTestHelper(
                new LocalUserServiceTestConfig(), new LocalDatastoreServiceTestConfig())
            .setEnvEmail(EMAIL)
            .setEnvAuthDomain(AUTHDOMAIN)
            .setEnvIsLoggedIn(true)
            .setEnvAttributes(
                new HashMap(
                    ImmutableMap.of(
                        "com.google.appengine.api.users.UserService.user_id_key", USER_ID)));
    helper.setUp();

    datastore = DatastoreServiceFactory.getDatastoreService();
    userServlet = new BusinessServlet();
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
  public void userNotInDatastoreReturnError()
      throws ServletException, IOException, EntityNotFoundException {
    when(request.getPathInfo()).thenReturn(PATHINFO);

    String keyString = KeyFactory.createKeyString("UserProfile", USER_ID);
    Key userKey = KeyFactory.stringToKey(keyString);
    Entity ent = new Entity("UserProfile", USER_ID);

    userServlet.doGet(request, response);

    // verify if a sendError() was performed with the expected values.
    Mockito.verify(response, Mockito.times(1))
        .sendError(Mockito.eq(HttpServletResponse.SC_NOT_FOUND), Mockito.anyString());
  }

  /*
   *  Test doGet() for when user is not a business owner, it should return a response error.
   **/
  @Test
  public void nonBusinessUserReturnError()
      throws ServletException, IOException, EntityNotFoundException {
    when(request.getPathInfo()).thenReturn(PATHINFO);

    String keyString = KeyFactory.createKeyString("UserProfile", USER_ID);
    Key userKey = KeyFactory.stringToKey(keyString);
    Entity ent = setUserProfileData();

    String isBusiness = "No";
    ent.setProperty("isBusiness", isBusiness);

    datastore.put(ent);

    userServlet.doGet(request, response);

    // verify if a sendError() was performed with the expected values.
    Mockito.verify(response, Mockito.times(1))
        .sendError(Mockito.eq(HttpServletResponse.SC_NOT_FOUND), Mockito.anyString());
  }

  /*
   *  Test doGet() for when user a business owner, it should return a JSON file of business profile page information.
   **/
  @Test
  public void BusinessUserReturnJsonFile() throws ServletException, IOException {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);
    when(request.getPathInfo()).thenReturn(PATHINFO);

    String keyString = KeyFactory.createKeyString("UserProfile", USER_ID);
    Key userKey = KeyFactory.stringToKey(keyString);
    Entity ent = setUserProfileData();

    String isBusiness = "Yes";
    boolean isCurrentUser = true;
    ent.setProperty("isBusiness", isBusiness);

    datastore.put(ent);

    userServlet.doGet(request, response);

    // verify that it sends a JSON file to response.
    BusinessProfile profile =
        new BusinessProfile(USER_ID, NAME, LOCATION, BIO, STORY, ABOUT, SUPPORT, isCurrentUser);

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
  public void notLoggedInUserEditProfileReturnError()
      throws ServletException, IOException, EntityNotFoundException {

    helper = new LocalServiceTestHelper(new LocalUserServiceTestConfig());
    helper.setUp();

    userServlet.doPost(request, response);

    // verify if a sendError() was performed with the expected values.
    Mockito.verify(response, Mockito.times(1))
        .sendError(Mockito.eq(HttpServletResponse.SC_NOT_FOUND), Mockito.anyString());
  }

  /* PASS
   *  Test doPost() for when the user did not fill out the name section. It should return error.
   **/
  @Test
  public void editProfileNameNotFilledReturnError()
      throws ServletException, IOException, EntityNotFoundException {
    String isBusiness = "Yes";

    when(request.getParameter("isBusiness")).thenReturn(isBusiness);
    when(request.getParameter("name")).thenReturn(NO_NAME);
    when(request.getParameter("location")).thenReturn(LOCATION);
    when(request.getParameter("bio")).thenReturn(BIO);

    Entity ent = createUserProfile();

    userServlet.doPost(request, response);

    // verify if a sendError() was performed with the expected values.
    Mockito.verify(response, Mockito.times(1))
        .sendError(Mockito.eq(HttpServletResponse.SC_NOT_FOUND), Mockito.anyString());
  }

  /*
   *  Test doPost() for when user is editing their profile page, it should put correct information into datastore.
   **/
  @Test
  public void userEditProfileAddToDatastore() throws Exception {
    String isBusiness = "Yes";

    when(request.getParameter("isBusiness")).thenReturn(isBusiness);
    when(request.getParameter("name")).thenReturn(NAME);
    when(request.getParameter("location")).thenReturn(LOCATION);
    when(request.getParameter("bio")).thenReturn(BIO);
    when(request.getParameter("story")).thenReturn(STORY);
    when(request.getParameter("about")).thenReturn(ABOUT);
    when(request.getParameter("support")).thenReturn(SUPPORT);

    userServlet.doPost(request, response);

    String keyString = KeyFactory.createKeyString("UserProfile", USER_ID);
    Key userKey = KeyFactory.stringToKey(keyString);

    Entity capEntity = datastore.get(userKey);

    Assert.assertEquals(capEntity.getProperty("isBusiness"), isBusiness);
    Assert.assertEquals(capEntity.getProperty("name"), NAME);
    Assert.assertEquals(capEntity.getProperty("location"), LOCATION);
    Assert.assertEquals(capEntity.getProperty("bio"), BIO);
    Assert.assertEquals(capEntity.getProperty("story"), STORY);
    Assert.assertEquals(capEntity.getProperty("about"), ABOUT);
    Assert.assertEquals(capEntity.getProperty("support"), SUPPORT);
  }

  /*
   *  Test doPost() for when user is editing their profile page, they decided to change to non-business profile.
   *  Return error.
   **/
  @Test
  public void nonBusinessUserEditProfileAddToDatastore() throws Exception {
    String isBusiness = "No";

    when(request.getParameter("isBusiness")).thenReturn(isBusiness);
    when(request.getParameter("name")).thenReturn(NAME);
    when(request.getParameter("location")).thenReturn(LOCATION);
    when(request.getParameter("bio")).thenReturn(BIO);
    when(request.getParameter("story")).thenReturn(STORY);
    when(request.getParameter("about")).thenReturn(ABOUT);
    when(request.getParameter("support")).thenReturn(SUPPORT);

    userServlet.doPost(request, response);

    // verify if a sendError() was performed with the expected values.
    Mockito.verify(response, Mockito.times(1))
        .sendError(Mockito.eq(HttpServletResponse.SC_NOT_FOUND), Mockito.anyString());
  }

  // Create an entity with USERID.
  public Entity createUserProfile() {
    String keyString = KeyFactory.createKeyString("UserProfile", USER_ID);
    Key userKey = KeyFactory.stringToKey(keyString);
    return new Entity("UserProfile", USER_ID);
  }

  // Set static variables to entity.
  public Entity setUserProfileData() {
    Entity ent = new Entity("UserProfile", USER_ID);
    ent.setProperty("name", NAME);
    ent.setProperty("location", LOCATION);
    ent.setProperty("bio", BIO);
    ent.setProperty("story", STORY);
    ent.setProperty("about", ABOUT);
    ent.setProperty("support", SUPPORT);

    return ent;
  }
}
