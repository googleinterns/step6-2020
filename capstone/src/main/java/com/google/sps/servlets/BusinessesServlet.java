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

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import com.google.sps.data.BusinessProfile;
import java.io.IOException;
import java.util.Arrays;
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
    Filter propertyFilter =
        new FilterPredicate("isBusiness", FilterOperator.EQUAL, "Yes");
    Query query = new Query("UserProfile").setFilter(propertyFilter);

    PreparedQuery results = datastore.prepare(query);

    // Retreive entities with a filter on business users.
    List<Entity> entities = results.asList(FetchOptions.Builder.withDefaults());

    // Convert entities to Profile objects.
    List<BusinessProfile> profiles = new ArrayList<>();
    for (Entity entity : entities) {
      String id = (String) entity.getKey().getName();
      String name = (String) entity.getProperty("name");
      String location = (String) entity.getProperty("location");
      String bio = (String) entity.getProperty("bio");
      String story = (String) entity.getProperty("story");
      String about = (String) entity.getProperty("about");
      String support = (String) entity.getProperty("support");

      BusinessProfile profile = new BusinessProfile(id, name, location, bio, story, about, support, false);
      profiles.add(profile);
    }

    response.setContentType("application/json;");
    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(profiles));
  }
}
