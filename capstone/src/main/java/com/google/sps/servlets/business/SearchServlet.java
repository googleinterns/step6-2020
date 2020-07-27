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
import static com.google.sps.data.ProfileDatastoreUtil.LAT_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.LOCATION_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.LONG_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.NAME_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.PROFILE_TASK_NAME;
import static com.google.sps.data.ProfileDatastoreUtil.STORY_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.SUPPORT_PROPERTY;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchException;
import com.google.appengine.api.search.SearchService;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.search.StatusCode;
import com.google.gson.Gson;
import com.google.sps.data.BusinessProfile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet responsible for returning appropriate search results. */
@WebServlet("/search")
public class SearchServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String searchItem = request.getParameter("searchItem");

    SearchService searchService = SearchServiceFactory.getSearchService();
    Index index = searchService.getIndex(IndexSpec.newBuilder().setName("Business"));

    List<BusinessProfile> businesses = new ArrayList<>();
    try {
      Results<ScoredDocument> searchResults = index.search(
          com.google.appengine.api.search.Query
          .newBuilder().build("name:\"" + searchItem + "\""));

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      for (ScoredDocument document: searchResults) {
        String businessId = document.getId();
        Query businessQuery =
            new Query(PROFILE_TASK_NAME)
                .setFilter(
                    CompositeFilterOperator.and(
                        FilterOperator.EQUAL.of(IS_BUSINESS_PROPERTY, "Yes"),
                        FilterOperator.EQUAL.of(
                            Entity.KEY_RESERVED_PROPERTY,
                            KeyFactory.createKey(PROFILE_TASK_NAME, businessId))));
        Entity businessEntity = datastore.prepare(businessQuery).asSingleEntity();
        if (businessEntity == null) {
          continue;
        }
        String id = businessEntity.getKey().getName();
        String name = (String) businessEntity.getProperty(NAME_PROPERTY);
        String email = (String) businessEntity.getProperty(CALENDAR_PROPERTY);
        String bio = (String) businessEntity.getProperty(BIO_PROPERTY);
        String location = (String) businessEntity.getProperty(LOCATION_PROPERTY);
        String story = (String) businessEntity.getProperty(STORY_PROPERTY);
        String about = (String) businessEntity.getProperty(ABOUT_PROPERTY);
        String support = (String) businessEntity.getProperty(SUPPORT_PROPERTY);

        BusinessProfile business =
            new BusinessProfile(id, name, location, bio, story, about, email, support, false);
        businesses.add(business);
      }
    } catch (SearchException e) {
      response.sendError(
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "The server was unable to handle the search request.");
      return;
    }

    response.setContentType("application/json");
    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(businesses));
  }
}
