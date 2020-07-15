package com.google.sps.servlets;

import static org.mockito.Mockito.doReturn;
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
import com.google.gson.JsonParser;
import com.google.sps.data.BusinessProfile;
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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public class BusinessServletTest {

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  private static final String USER_TASK = "UserProfile";
  private static final String NAME = "Pizzeria";
  private static final String LOCATION = "Mountain View, CA";
  private static final String BIO = "This is my business bio.";
  private static final String STORY = "The pandemic has affected my business in X many ways.";
  private static final String ABOUT = "Here is the Pizzeria's menu.";
  private static final String SUPPORT = "Please donate at X website.";
  private static final String USER_ID = "12345";
  private static final String INVALID_USER_ID = null;
  private static final String EMAIL = "abc@gmail.com";
  private static final String AUTHDOMAIN = "gmail.com";
  private static final String PATHINFO = "/12345";
  private static final String INVALID_PATHINFO = "/notANumber";

  private LocalServiceTestHelper helper;

  private BusinessServlet servlet;
  private DatastoreService datastore;
  private StringWriter servletResponseWriter;

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
    servletResponseWriter = new StringWriter();
    doReturn(new PrintWriter(servletResponseWriter)).when(response).getWriter();
    servlet = new BusinessServlet();
  }

  @After
  public void tearDown() throws Exception {
    helper.tearDown();
  }

  // Set static variables to entity.
  public Entity setUserProfileData() {
    Entity ent = new Entity(USER_TASK, USER_ID);
    ent.setProperty("name", NAME);
    ent.setProperty("location", LOCATION);
    ent.setProperty("bio", BIO);
    ent.setProperty("story", STORY);
    ent.setProperty("about", ABOUT);
    ent.setProperty("calendarEmail", EMAIL);
    ent.setProperty("support", SUPPORT);

    return ent;
  }

  /** Test doGet() for when user enters an invalid URL param. It should return an error. */
  @Test
  public void invalidUrlParamReturnError() throws ServletException, IOException {
    when(request.getPathInfo()).thenReturn(INVALID_PATHINFO);

    servlet.doGet(request, response);

    // verify if a sendError() was performed with the expected values.
    Mockito.verify(response, Mockito.times(1))
        .sendError(Mockito.eq(HttpServletResponse.SC_NOT_FOUND), Mockito.anyString());
  }

  /**
   * Test doGet() for when datastore cannot find entity key. User does not exist in datastore. It
   * should return an error.
   */
  @Test
  public void userNotInDatastoreReturnError()
      throws ServletException, IOException, EntityNotFoundException {
    when(request.getPathInfo()).thenReturn(PATHINFO);

    // Populate the datastore with a business with the wrong target ID.
    Entity someBusiness = new Entity(USER_TASK, USER_ID + "1");
    datastore.put(someBusiness);

    servlet.doGet(request, response);

    // verify if a sendError() was performed with the expected values.
    Mockito.verify(response, Mockito.times(1))
        .sendError(Mockito.eq(HttpServletResponse.SC_NOT_FOUND), Mockito.anyString());
  }

  /** Test doGet() for when user is not a business owner, it should return a response error. */
  @Test
  public void nonBusinessUserReturnError()
      throws ServletException, IOException, EntityNotFoundException {
    when(request.getPathInfo()).thenReturn(PATHINFO);

    Entity fakeBusiness = setUserProfileData();
    fakeBusiness.setProperty("isBusiness", "No");
    datastore.put(fakeBusiness);

    servlet.doGet(request, response);

    // verify if a sendError() was performed with the expected values.
    Mockito.verify(response, Mockito.times(1))
        .sendError(Mockito.eq(HttpServletResponse.SC_NOT_FOUND), Mockito.anyString());
  }

  /**
   * Test doGet() for when user a business owner, it should return a JSON string of business profile
   * page information.
   */
  @Test
  public void BusinessUserReturnJsonFile() throws ServletException, IOException {
    when(request.getPathInfo()).thenReturn(PATHINFO);

    Entity validBusiness = setUserProfileData();
    validBusiness.setProperty("isBusiness", "Yes");
    datastore.put(validBusiness);

    servlet.doGet(request, response);

    BusinessProfile profile =
        new BusinessProfile(USER_ID, NAME, LOCATION, BIO, STORY, ABOUT, EMAIL, SUPPORT, true);

    String servletResponse = servletResponseWriter.toString();

    Gson gson = new Gson();
    String expectedResponse = gson.toJson(profile);

    JsonParser parser = new JsonParser();
    Assert.assertEquals(parser.parse(expectedResponse), parser.parse(servletResponse));
  }

  /**
   * Test doPost() for when the user does not exist and they want to edit a profile. It should
   * return error.
   */
  @Test
  public void notLoggedInUserEditProfileReturnError()
      throws ServletException, IOException, EntityNotFoundException {
    helper.setEnvIsLoggedIn(false);

    servlet.doPost(request, response);

    // verify if a sendError() was performed with the expected values.
    Mockito.verify(response, Mockito.times(1))
        .sendError(Mockito.eq(HttpServletResponse.SC_FORBIDDEN), Mockito.anyString());
  }

  /** Test doPost() for when the user did not fill out the name section. It should return error. */
  @Test
  public void editProfileNameNotFilledReturnError()
      throws ServletException, IOException, EntityNotFoundException {
    servlet.doPost(request, response);

    // verify if a sendError() was performed with the expected values.
    Mockito.verify(response, Mockito.times(1))
        .sendError(Mockito.eq(HttpServletResponse.SC_BAD_REQUEST), Mockito.anyString());
  }

  /**
   * Test doPost() for when user is editing their profile page, it should put correct information
   * into datastore.
   */
  @Test
  public void userEditProfileAddToDatastore() throws Exception {
    when(request.getParameter("isBusiness")).thenReturn("Yes");
    when(request.getParameter("name")).thenReturn(NAME);
    when(request.getParameter("location")).thenReturn(LOCATION);
    when(request.getParameter("bio")).thenReturn(BIO);
    when(request.getParameter("story")).thenReturn(STORY);
    when(request.getParameter("about")).thenReturn(ABOUT);
    when(request.getParameter("calendarEmail")).thenReturn(EMAIL);
    when(request.getParameter("support")).thenReturn(SUPPORT);

    servlet.doPost(request, response);

    String keyString = KeyFactory.createKeyString(USER_TASK, USER_ID);
    Key userKey = KeyFactory.stringToKey(keyString);

    Entity capEntity = datastore.get(userKey);

    Assert.assertEquals(capEntity.getProperty("isBusiness"), "Yes");
    Assert.assertEquals(capEntity.getProperty("name"), NAME);
    Assert.assertEquals(capEntity.getProperty("location"), LOCATION);
    Assert.assertEquals(capEntity.getProperty("bio"), BIO);
    Assert.assertEquals(capEntity.getProperty("story"), STORY);
    Assert.assertEquals(capEntity.getProperty("about"), ABOUT);
    Assert.assertEquals(capEntity.getProperty("calendarEmail"), EMAIL);
    Assert.assertEquals(capEntity.getProperty("support"), SUPPORT);
  }

  /**
   * Test doPost() for when user is editing their profile page, they decided to change to
   * non-business profile. Return error.
   */
  @Test
  public void nonBusinessUserEditProfileAddToDatastore() throws Exception {
    when(request.getParameter("isBusiness")).thenReturn("No");
    when(request.getParameter("name")).thenReturn(NAME);

    servlet.doPost(request, response);

    // verify if a sendError() was performed with the expected values.
    Mockito.verify(response, Mockito.times(1))
        .sendError(Mockito.eq(HttpServletResponse.SC_FORBIDDEN), Mockito.anyString());
  }
}
