package com.google.sps.servlets.profile;

import static org.mockito.Mockito.when;

import com.google.appengine.api.users.UserService;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.sps.data.User;
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
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public class ProfilesServletTest {

  private LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalUserServiceTestConfig())
          .setEnvIsAdmin(true)
          .setEnvIsLoggedIn(true);

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private UserService userService;

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
   *  Test doGet() that it returns a lists all user profiles.
   **/
  @Test
  public void businessUserReturnError() throws ServletException, IOException {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);

  }
}