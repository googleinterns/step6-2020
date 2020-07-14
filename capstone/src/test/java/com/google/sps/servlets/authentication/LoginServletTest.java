package com.google.sps.servlets.authentication;

import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.common.collect.ImmutableMap;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.sps.data.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public class LoginServletTest {
  private static final String USER_ID = "12345";
  private static final String EMAIL = "abc@gmail.com";
  private static final String AUTHDOMAIN = "gmail.com";
  private static final String LOG_IN_URL = "/_ah/login?continue=%2Fcheck_new_user";
  private static final String LOG_OUT_URL = "/logout";
  private static final String IS_BUSINESS_PROPERTY = "isBusiness";
  private static final String NO = "No";

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  private LocalServiceTestHelper helper;
  private LoginServlet loginServlet;
  private DatastoreService datastore;
  private StringWriter stringWriter;
  private PrintWriter printWriter;

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

    loginServlet = new LoginServlet();
    datastore = DatastoreServiceFactory.getDatastoreService();

    stringWriter = new StringWriter();
    printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);
  }

  @After
  public void tearDown() throws Exception {
    helper.tearDown();
  }

  /*
   *  Test for when user is logged in, it should return isUserLoggedin boolean value
   *  true and the logout URL.
   **/
  @Test
  public void loggedInUserReturnsLogOutUrl() throws ServletException, IOException {
    String keyString = KeyFactory.createKeyString("UserProfile", USER_ID);
    Key userKey = KeyFactory.stringToKey(keyString);
    Entity ent = new Entity("UserProfile", USER_ID);
    ent.setProperty(IS_BUSINESS_PROPERTY, NO);
    datastore.put(ent);
    
    loginServlet.doGet(request, response);

    User LoggedInUser = new User(true, LOG_OUT_URL, USER_ID, NO);

    String responseString = stringWriter.getBuffer().toString().trim();
    JsonElement responseJsonElement = new JsonParser().parse(responseString);

    Gson gson = new GsonBuilder().create();
    JsonElement userJsonElement = gson.toJsonTree(LoggedInUser);

    JsonObject responseJsonObject = responseJsonElement.getAsJsonObject();
    JsonObject userJsonObject = userJsonElement.getAsJsonObject();

    Assert.assertEquals(responseJsonObject, userJsonObject);
  }

  /*
   *  Test for when user is logged out, it should return isUserLoggedin boolean value
   *  false and the login URL.
   **/
  @Test
  public void loggedOutUserReturnsLogInUrl() throws ServletException, IOException {
    helper =
        new LocalServiceTestHelper(
                new LocalUserServiceTestConfig())
            .setEnvEmail(EMAIL)
            .setEnvAuthDomain(AUTHDOMAIN)
            .setEnvIsLoggedIn(false);
    helper.setUp();

    loginServlet.doGet(request, response);
    
    User LoggedOutUser = new User(false, LOG_IN_URL, null, null);

    String responseString = stringWriter.getBuffer().toString().trim();
    JsonElement responseJsonElement = new JsonParser().parse(responseString);

    Gson gson = new GsonBuilder().create();
    JsonElement userJsonElement = gson.toJsonTree(LoggedOutUser);

    JsonObject responseJsonObject = responseJsonElement.getAsJsonObject();
    JsonObject userJsonObject = userJsonElement.getAsJsonObject();

    Assert.assertEquals(responseJsonObject, userJsonObject);
  }
}
