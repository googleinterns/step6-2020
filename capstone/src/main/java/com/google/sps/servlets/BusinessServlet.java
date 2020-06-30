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

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.gson.Gson;
import com.google.sps.data.BusinessProfile;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that handles business information. */
@WebServlet("/business/*")
public class BusinessesServlet extends HttpServlet {

  private static final String TASK_NAME = "Business";
  private static final String BUSINESS_NAME = "businessName";
  private static final String NAME_PROPERTY = "name";
  private static final String EMAIL_PROPERTY = "email";
  private static final String BIO_PROPERTY = "bio";
  private static final String LOCATION_PROPERTY = "location";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Retrieve the business ID that is located in place of the * in the URL.
    // request.getPathInfo() returns "/{id}" and substring(1) would return "{id}" without "/".
    String businessName = request.getPathInfo().substring(1);

    // Retrieve all of the information for a single business to be displayed.
    Query businessQuery =
        new Query(TASK_NAME)
            .setFilter(new FilterPredicate(NAME_PROPERTY, FilterOperator.EQUAL, businessName));
    DataStoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery queryResults = datastore.prepare(businessQuery);
    Entity businessEntity = queryResults.asSingleEntity();

    String name = (String) businessEntity.getProperty(NAME_PROPERTY);
    String email = (String) businessEntity.getProperty(EMAIL_PROPERTY);
    String bio = (String) businessEntity.getProperty(BIO_PROPERTY);
    String location = (String) businessEntity.getProperty(LOCATION_PROPERTY);
    BusinessProfile business = new BusinessProfile(name, email, bio, location);

    Gson gson = new Gson();
    String jsonBusiness = gson.toJson(business);
    response.setContentType("application/json");
    response.getWriter().println(jsonBusiness);
  }
}
