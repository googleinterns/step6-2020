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

import static com.google.sps.data.ProfileDatastoreUtil.BIO_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.GEO_PT_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.IS_BUSINESS_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.LOCATION_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.NAME_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.PROFILE_TASK_NAME;
import static com.google.sps.data.ProfileDatastoreUtil.LAT_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.LONG_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.SW_LAT_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.SW_LNG_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.NE_LAT_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.NE_LNG_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.YES;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.GeoRegion.Rectangle;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.StContainsFilter;
import com.google.appengine.api.datastore.GeoPt;
import com.google.gson.Gson;
import com.google.sps.data.MapInfo;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet responsible for listing the businesses within the requested map radius. */
@WebServlet("/map")
public class MapServlet extends HttpServlet {

  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    // Get map bounds from request parameter.
    float SW_Lat, SW_Lng, NE_Lat, NE_Lng;

    try {
      SW_Lat = Float.parseFloat(request.getParameter(SW_LAT_PROPERTY));
      SW_Lng = Float.parseFloat(request.getParameter(SW_LNG_PROPERTY));
      NE_Lat = Float.parseFloat(request.getParameter(NE_LAT_PROPERTY));
      NE_Lng = Float.parseFloat(request.getParameter(NE_LNG_PROPERTY));
    } catch (NullPointerException e) {
      response.sendError(
          HttpServletResponse.SC_BAD_REQUEST, "Request parameters are invalid.");
      return;
    }

    GeoPt SW = new GeoPt(SW_Lat, SW_Lng);
    GeoPt NE = new GeoPt(NE_Lat, NE_Lng);

    Rectangle bounds = new Rectangle(SW, NE);

    // Filter businesses that are within the map search bounds.
    Filter propertyFilter = new CompositeFilter(CompositeFilterOperator.AND, Arrays.asList(
                            new FilterPredicate(IS_BUSINESS_PROPERTY, FilterOperator.EQUAL, YES),
                            new StContainsFilter(GEO_PT_PROPERTY, bounds)));

    Query query = new Query(PROFILE_TASK_NAME).setFilter(propertyFilter);

    PreparedQuery results = datastore.prepare(query);

    // Convert entities to Profile objects.
    List<MapInfo> businesses = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      String id = (String) entity.getKey().getName();
      String name = (String) entity.getProperty(NAME_PROPERTY);
      String location = (String) entity.getProperty(LOCATION_PROPERTY);
      String bio = (String) entity.getProperty(BIO_PROPERTY);
      GeoPt geoPt = (GeoPt) entity.getProperty(GEO_PT_PROPERTY);

      MapInfo business = new MapInfo(id, name, location, bio, geoPt);
      businesses.add(business);
    }

    response.setContentType("application/json;");
    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(businesses));
  }
}
