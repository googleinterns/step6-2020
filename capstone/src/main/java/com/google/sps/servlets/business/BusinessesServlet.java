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
import static com.google.sps.data.ProfileDatastoreUtil.IS_BUSINESS_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.LOCATION_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.NAME_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.PROFILE_TASK_NAME;
import static com.google.sps.data.ProfileDatastoreUtil.STORY_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.SUPPORT_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.YES;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.gson.Gson;
import com.google.sps.data.BusinessProfile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet responsible for listing all business profiles. */
@WebServlet("/businesses")
public class BusinessesServlet extends HttpServlet {

  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Query profile entities from datastore.
    Filter propertyFilter = new FilterPredicate(IS_BUSINESS_PROPERTY, FilterOperator.EQUAL, YES);
    Query query = new Query(PROFILE_TASK_NAME).setFilter(propertyFilter);

    PreparedQuery results = datastore.prepare(query);

    // Convert entities to Profile objects.
    List<BusinessProfile> profiles = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      String id = (String) entity.getKey().getName();
      String name = (String) entity.getProperty(NAME_PROPERTY);
      String location = (String) entity.getProperty(LOCATION_PROPERTY);
      String bio = (String) entity.getProperty(BIO_PROPERTY);
      String story = (String) entity.getProperty(STORY_PROPERTY);
      String about = (String) entity.getProperty(ABOUT_PROPERTY);
      String calendarEmail = (String) entity.getProperty(CALENDAR_PROPERTY);
      String support = (String) entity.getProperty(SUPPORT_PROPERTY);

      BusinessProfile profile =
          new BusinessProfile(id, name, location, bio, story, about, calendarEmail, support, false);
      profiles.add(profile);
    }

    response.setContentType("application/json;");
    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(profiles));
  }
}
