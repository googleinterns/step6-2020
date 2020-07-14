package com.google.sps.servlets.authentication;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.common.collect.ImmutableMap;
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
public class NewUserServletTest {
  
  private static final String IS_BUSINESS_PROPERTY = "isBusiness";
  private static final String NAME_PROPERTY = "name";
  private static final String LOCATION_PROPERTY = "location";
  private static final String BIO_PROPERTY = "bio";
  private static final String NAME = "John Doe";
  private static final String ANONYMOUS_NAME = "Anonymous";
  private static final String LOCATION = "Mountain View, CA";
  private static final String BIO = "This is my bio.";
  private static final String USER_ID = "12345";
  private static final String EMAIL = "abc@gmail.com";
  private static final String AUTHDOMAIN = "gmail.com";
  private static final String DEFAULT = "";
  private static final String YES = "Yes";
  private static final String NO = "No";

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  private LocalServiceTestHelper helper =
        new LocalServiceTestHelper(
                new LocalUserServiceTestConfig(), new LocalDatastoreServiceTestConfig())
            .setEnvEmail(EMAIL)
            .setEnvAuthDomain(AUTHDOMAIN)
            .setEnvIsLoggedIn(true)
            .setEnvAttributes(
                new HashMap(
                    ImmutableMap.of(
                        "com.google.appengine.api.users.UserService.user_id_key", USER_ID)));
  private NewUserServlet newUserServlet;
  private DatastoreService datastore;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);    
    helper.setUp();

    datastore = DatastoreServiceFactory.getDatastoreService();
    newUserServlet = new NewUserServlet();
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
  public void newUserAddToDatabase() throws Exception {
    String keyString = KeyFactory.createKeyString("UserProfile", USER_ID);
    Key userKey = KeyFactory.stringToKey(keyString);

    newUserServlet.doGet(request, response);

    Entity capEntity = datastore.get(userKey);
    Assert.assertEquals(capEntity.getProperty(IS_BUSINESS_PROPERTY), NO);
    Assert.assertEquals(capEntity.getProperty(NAME_PROPERTY), ANONYMOUS_NAME);
    Assert.assertEquals(capEntity.getProperty(LOCATION_PROPERTY), DEFAULT);
    Assert.assertEquals(capEntity.getProperty(BIO_PROPERTY), DEFAULT);
  }

  /*
   *  Check the doGet function from New User Servlet. Create a a returning user
   *  and add it to datastore. It should return an entity when called datastore.get().
   **/
  @Test
  public void returningUserIsInDatabase() throws Exception {
    String keyString = KeyFactory.createKeyString("UserProfile", USER_ID);
    Key userKey = KeyFactory.stringToKey(keyString);

    Entity ent = new Entity("UserProfile", USER_ID);
    ent.setProperty(IS_BUSINESS_PROPERTY, NO);
    ent.setProperty(NAME_PROPERTY, NAME);
    ent.setProperty(LOCATION_PROPERTY, LOCATION);
    ent.setProperty(BIO_PROPERTY, BIO);
    datastore.put(ent);

    newUserServlet.doGet(request, response);

    Entity capEntity = datastore.get(userKey);
    Assert.assertEquals(capEntity.getProperty(IS_BUSINESS_PROPERTY), NO);
    Assert.assertEquals(capEntity.getProperty(NAME_PROPERTY), NAME);
    Assert.assertEquals(capEntity.getProperty(LOCATION_PROPERTY), LOCATION);
    Assert.assertEquals(capEntity.getProperty(BIO_PROPERTY), BIO);
  }
}
