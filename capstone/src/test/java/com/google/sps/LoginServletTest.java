package com.google.sps;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.users.dev.LocalUserService;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.sps.data.User;
import com.google.sps.servlets.authentication.LoginServlet;
import com.google.sps.servlets.authentication.LogoutServlet;
import java.io.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
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

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private UserService userService;

  @Before
  public void setUp() throws Exception  {
    MockitoAnnotations.initMocks(this);
  }
  
  /* 
  *  Test for when user is logged in, it should return isUserLoggedin boolean value
  *  true and the logout URL.
  **/
  @Test
  public void loggedInUserReturnsLogOutUrl() throws ServletException, IOException  {
    LocalServiceTestHelper helper =
    new LocalServiceTestHelper(new LocalUserServiceTestConfig())
        .setEnvIsAdmin(true).setEnvIsLoggedIn(true);
    helper.setUp();

    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);

    LoginServlet userServlet = new LoginServlet();
    userServlet.doGet(request, response);

    User LoggedInUser = new User(true, "/logout");

    String responseString = stringWriter.getBuffer().toString().trim();
    JsonElement responseJsonElement = new JsonParser().parse(responseString);

    Gson gson = new GsonBuilder().create();
    JsonElement userJsonElement = gson.toJsonTree(LoggedInUser);

    JsonObject responseJsonObject = responseJsonElement.getAsJsonObject();
    JsonObject userJsonObject = userJsonElement.getAsJsonObject();
    
    Assert.assertEquals(responseJsonObject, userJsonObject);
    helper.tearDown();
  }
  
  /* 
  *  Test for when user is logged out, it should return isUserLoggedin boolean value
  *  false and the login URL.
  **/
  @Test
  public void loggedOutUserReturnsLogInUrl() throws ServletException, IOException  {
    LocalServiceTestHelper helper =
    new LocalServiceTestHelper(new LocalUserServiceTestConfig())
        .setEnvIsAdmin(true).setEnvIsLoggedIn(false);
    helper.setUp();

    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);
    
    String loginUrl = "loginUrl";
    when(userService.createLoginURL("/index.html")).thenReturn(loginUrl);

    LoginServlet userServlet = new LoginServlet(userService);
    userServlet.doGet(request, response);

    User LoggedOutUser = new User(false, loginUrl);

    String responseString = stringWriter.getBuffer().toString().trim();
    JsonElement responseJsonElement = new JsonParser().parse(responseString);

    Gson gson = new GsonBuilder().create();
    JsonElement userJsonElement = gson.toJsonTree(LoggedOutUser);

    JsonObject responseJsonObject = responseJsonElement.getAsJsonObject();
    JsonObject userJsonObject = userJsonElement.getAsJsonObject();
    
    Assert.assertEquals(responseJsonObject, userJsonObject);
    helper.tearDown();
  }
} 
