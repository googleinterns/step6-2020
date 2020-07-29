// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import static com.google.sps.data.ProfileDatastoreUtil.ABOUT_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.BIO_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.CALENDAR_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.GEO_PT_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.IS_BUSINESS_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.LOCATION_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.NAME_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.NO;
import static com.google.sps.data.ProfileDatastoreUtil.PROFILE_TASK_NAME;
import static com.google.sps.data.ProfileDatastoreUtil.STORY_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.SUPPORT_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.YES;
import static org.mockito.Mockito.doReturn;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.SearchService;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.search.StatusCode;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalSearchServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.sps.data.BusinessProfile;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for SearchServlet. */
public class SearchServletTest {

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig(), new LocalSearchServiceTestConfig());

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  private static final String NAME = "Famous Pizzeria";
  private static final String NAME_2 = "Infamous Pizzeria";
  private static final String LOCATION = "Mountain View, CA";
  private static final String LAT = "45.0";
  private static final String LONG = "45.0";
  private static final String BIO = "This is my business bio.";
  private static final String STORY = "The pandemic has affected my business in X many ways.";
  private static final String ABOUT = "Here is the Pizzeria's menu.";
  private static final String SUPPORT = "Please donate at X website.";
  private static final String USER_ID_1 = "12345";
  private static final String USER_ID_2 = "67890";
  private static final String WRONG_USER = "54321";
  private static final String EMAIL = "abc@gmail.com";

  private StringWriter servletResponseWriter;
  private SearchServlet servlet;
  private DatastoreService datastore;
  private GeoPt GEO_PT;

  @Before
  public void setUp() throws IOException {
    MockitoAnnotations.initMocks(this);
    helper.setUp();

    datastore = DatastoreServiceFactory.getDatastoreService();
    servletResponseWriter = new StringWriter();
    doReturn(new PrintWriter(servletResponseWriter)).when(response).getWriter();
    servlet = new SearchServlet();
    GEO_PT = new GeoPt(Float.parseFloat(LAT), Float.parseFloat(LONG));
  }

  @After
  public void tearDown() {
    helper.getLocalService("search").stop();
    helper.tearDown();
  }

  public Entity setBusinessData(String id, String name) {
    Entity ent = new Entity(PROFILE_TASK_NAME, id);
    ent.setProperty(NAME_PROPERTY, name);
    ent.setProperty(LOCATION_PROPERTY, LOCATION);
    ent.setProperty(GEO_PT_PROPERTY, GEO_PT);
    ent.setProperty(BIO_PROPERTY, BIO);
    ent.setProperty(STORY_PROPERTY, STORY);
    ent.setProperty(ABOUT_PROPERTY, ABOUT);
    ent.setProperty(CALENDAR_PROPERTY, EMAIL);
    ent.setProperty(SUPPORT_PROPERTY, SUPPORT);

    return ent;
  }

  public void createDocument(String id, String name) {
    SearchService searchService = SearchServiceFactory.getSearchService();
    Index index = searchService.getIndex(IndexSpec.newBuilder().setName("Business"));
    Document document =
        Document.newBuilder()
            .setId(id)
            .addField(Field.newBuilder().setName("name").setTokenizedPrefix(name))
            .build();

    try {
      index.put(document);
    } catch (PutException e) {
      if (StatusCode.TRANSIENT_ERROR.equals(e.getOperationResult().getCode())) {
        index.put(document);
      }
    }
  }

  /** Test retrieving a business by exact match to business name. */
  @Test
  public void testDoGetExactMatch() throws IOException {
    doReturn(NAME).when(request).getParameter("searchItem");

    Entity business = setBusinessData(USER_ID_1, NAME);
    business.setProperty(IS_BUSINESS_PROPERTY, YES);
    datastore.put(business);

    createDocument(USER_ID_1, NAME);

    servlet.doGet(request, response);

    List<BusinessProfile> expectedResults = new ArrayList<>();
    BusinessProfile expectedProfile =
        new BusinessProfile(USER_ID_1, NAME, LOCATION, BIO, STORY, ABOUT, EMAIL, SUPPORT, false);
    expectedResults.add(expectedProfile);
    String servletResponse = servletResponseWriter.toString();

    Gson gson = new Gson();
    JsonParser parser = new JsonParser();
    Assert.assertEquals(parser.parse(servletResponse), parser.parse(gson.toJson(expectedResults)));
  }

  /**
   * Test retrieving a business by partial match to business name.
   *
   * <p>A partial match occurs if the search item matches any starting n characters of any word in
   * the index. Ex. "Piz" matches "Pizzeria" "Piz" matches "Famous Pizzeria" "piz" matches
   * "Pizzeria"
   */
  @Test
  public void testDoGetPartialMatch() throws IOException {
    doReturn("Pizzeria").when(request).getParameter("searchItem");

    Entity business = setBusinessData(USER_ID_1, NAME);
    business.setProperty(IS_BUSINESS_PROPERTY, YES);
    datastore.put(business);

    createDocument(USER_ID_1, NAME);

    servlet.doGet(request, response);

    List<BusinessProfile> expectedResults = new ArrayList<>();
    BusinessProfile expectedProfile =
        new BusinessProfile(USER_ID_1, NAME, LOCATION, BIO, STORY, ABOUT, EMAIL, SUPPORT, false);
    expectedResults.add(expectedProfile);
    String servletResponse = servletResponseWriter.toString();

    Gson gson = new Gson();
    JsonParser parser = new JsonParser();
    Assert.assertEquals(parser.parse(servletResponse), parser.parse(gson.toJson(expectedResults)));
  }

  /** Test retrieving multiple businesses by partial match to business name. */
  @Test
  public void testDoGetMultiplePartialMatch() throws IOException {
    doReturn("pizzeria").when(request).getParameter("searchItem");

    Entity business1 = setBusinessData(USER_ID_1, NAME);
    business1.setProperty(IS_BUSINESS_PROPERTY, YES);
    datastore.put(business1);

    Entity business2 = setBusinessData(USER_ID_2, NAME_2);
    business2.setProperty(IS_BUSINESS_PROPERTY, YES);
    datastore.put(business2);

    createDocument(USER_ID_1, NAME);
    createDocument(USER_ID_2, NAME_2);

    servlet.doGet(request, response);

    List<BusinessProfile> expectedResults = new ArrayList<>();
    BusinessProfile expectedProfile1 =
        new BusinessProfile(USER_ID_1, NAME, LOCATION, BIO, STORY, ABOUT, EMAIL, SUPPORT, false);
    BusinessProfile expectedProfile2 =
        new BusinessProfile(USER_ID_2, NAME_2, LOCATION, BIO, STORY, ABOUT, EMAIL, SUPPORT, false);
    expectedResults.add(expectedProfile1);
    expectedResults.add(expectedProfile2);
    String servletResponse = servletResponseWriter.toString();

    Gson gson = new Gson();
    JsonParser parser = new JsonParser();
    Assert.assertEquals(parser.parse(servletResponse), parser.parse(gson.toJson(expectedResults)));
  }

  /** Test retrieving business with a user account decoy. */
  @Test
  public void testDoGetPartialMatchWithUserAccount() throws IOException {
    doReturn("pizzeria").when(request).getParameter("searchItem");

    Entity business1 = setBusinessData(USER_ID_1, NAME);
    business1.setProperty(IS_BUSINESS_PROPERTY, YES);
    datastore.put(business1);

    Entity business2 = setBusinessData(USER_ID_2, NAME_2);
    business2.setProperty(IS_BUSINESS_PROPERTY, NO);
    datastore.put(business2);

    createDocument(USER_ID_1, NAME);
    createDocument(USER_ID_2, NAME_2);

    servlet.doGet(request, response);

    List<BusinessProfile> expectedResults = new ArrayList<>();
    BusinessProfile expectedProfile1 =
        new BusinessProfile(USER_ID_1, NAME, LOCATION, BIO, STORY, ABOUT, EMAIL, SUPPORT, false);
    expectedResults.add(expectedProfile1);
    String servletResponse = servletResponseWriter.toString();

    Gson gson = new Gson();
    JsonParser parser = new JsonParser();
    Assert.assertEquals(parser.parse(servletResponse), parser.parse(gson.toJson(expectedResults)));
  }
}
