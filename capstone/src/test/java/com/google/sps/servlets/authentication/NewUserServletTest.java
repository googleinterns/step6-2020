package com.google.sps.servlets.authentication;

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
import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public class NewUserServletTest {

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalUserServiceTestConfig())
          .setEnvIsAdmin(true)
          .setEnvIsLoggedIn(true);

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private UserService userService;

  @Mock private DatastoreService datastore;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    helper.setUp();

    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);
  }

  @After
  public void tearDown() throws Exception {
    helper.tearDown();
  }

  /*
   *  Check the doGet function from New User Servlet. Create a a new user
   *  and add it to datastore. It should return a EntityNotFoundException.
   *  Then it should be added to the datastore.
   **/
  @Test
  public void newUserAddToDatabase() throws ServletException, IOException, EntityNotFoundException {
    String userId = "12345";
    String email = "abc@gmail.com";
    String authDomain = "gmail.com";
    User user = new User(email, authDomain, userId);
    when(userService.getCurrentUser()).thenReturn(user);

    Key userKey = KeyFactory.createKey("User", userId);

    when(datastore.get(userKey)).thenThrow(EntityNotFoundException.class);

    Entity ent = new Entity("User", userId);

    NewUserServlet userServlet = new NewUserServlet(userService, datastore);
    userServlet.doGet(request, response);

    verify(datastore).put(ent);
  }

  /*
   *  Check the doGet function from New User Servlet. Create a a returning user
   *  and add it to datastore. It should return an entity when called datastore.get().
   **/
  @Test
  public void returningUserIsInDatabase()
      throws ServletException, IOException, EntityNotFoundException {
    String userId = "12345";
    String email = "abc@gmail.com";
    String authDomain = "gmail.com";
    User user = new User(email, authDomain, userId);
    when(userService.getCurrentUser()).thenReturn(user);

    Key userKey = KeyFactory.createKey("User", userId);
    Entity ent = new Entity("User", userId);

    when(datastore.get(userKey)).thenReturn(ent);

    NewUserServlet userServlet = new NewUserServlet(userService, datastore);
    userServlet.doGet(request, response);

    verify(datastore, never()).put(any(Entity.class));
  }
}