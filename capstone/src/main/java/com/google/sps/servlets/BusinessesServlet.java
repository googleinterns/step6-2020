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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;
import com.google.sps.data.BusinessProfile;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that handles business information. */
@WebServlet("/businesses")
public class BusinessesServlet extends HttpServlet {

  private static final String USER_TASK = "UserProfile";
  private static final String IS_BUSINESS_PROPERTY = "isBusiness";
  private static final String NAME_PROPERTY = "name";
  private static final String BIO_PROPERTY = "bio";
  private static final String LOCATION_PROPERTY = "location";
  private static final String STORY_PROPERTY = "story";
  private static final String ABOUT_PROPERTY = "about";
  private static final String SUPPORT_PROPERTY = "support";
  private static final String CALENDAR_PROPERTY = "calendarEmail";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Retrieve the name and bio of all businesses to be displayed on the page.
    Query businessQuery = new Query(USER_TASK);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery queryResults = datastore.prepare(businessQuery);
    ArrayList<BusinessProfile> businesses = new ArrayList();

    // Construct list of businesses from datastore.
    for (Entity businessEntity : queryResults.asIterable()) {
      String id = (String) businessEntity.getKey().getName();
      String name = (String) businessEntity.getProperty(NAME_PROPERTY);
      String email = (String) businessEntity.getProperty(CALENDAR_PROPERTY);
      String bio = (String) businessEntity.getProperty(BIO_PROPERTY);
      String location = (String) businessEntity.getProperty(LOCATION_PROPERTY);
      String story = (String) businessEntity.getProperty(STORY_PROPERTY);
      String about = (String) businessEntity.getProperty(ABOUT_PROPERTY);
      String support = (String) businessEntity.getProperty(SUPPORT_PROPERTY);
      BusinessProfile business =
          new BusinessProfile(id, name, email, bio, location, story, about, support);
      businesses.add(business);
    }

    Gson gson = new Gson();
    String jsonBusinesses = gson.toJson(businesses);
    response.setContentType("application/json");
    response.getWriter().println(jsonBusinesses);
  }
}
