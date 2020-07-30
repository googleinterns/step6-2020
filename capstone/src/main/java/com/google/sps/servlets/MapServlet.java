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
import static com.google.sps.data.ProfileDatastoreUtil.IS_BUSINESS_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.LOCATION_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.LAT_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.LONG_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.NAME_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.NE_LAT_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.NE_LNG_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.PROFILE_TASK_NAME;
import static com.google.sps.data.ProfileDatastoreUtil.SW_LAT_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.SW_LNG_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.YES;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.GeoRegion.Rectangle;
import com.google.appengine.api.datastore.Query.StContainsFilter;
import com.google.gson.Gson;
import com.google.sps.data.MapInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
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
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameters are invalid.");
      return;
    }

    // Filter businesses that are within the map search bounds.
    Filter latFilter =
            CompositeFilterOperator.and(
              new FilterPredicate(IS_BUSINESS_PROPERTY, FilterOperator.EQUAL, YES),
              CompositeFilterOperator.and(
                new FilterPredicate(LAT_PROPERTY, FilterOperator.GREATER_THAN_OR_EQUAL, SW_Lat),
                new FilterPredicate(LAT_PROPERTY, FilterOperator.LESS_THAN_OR_EQUAL, NE_Lat)));

    // Convert entities to Profile objects.
    Query latQuery = new Query(PROFILE_TASK_NAME).setFilter(latFilter);
    PreparedQuery latResults = datastore.prepare(latQuery);
    Set<MapInfo> latList = new HashSet<MapInfo>();
    for (Entity entity : latResults.asIterable()) {
      System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
      System.out.println("Entered for loop #1 ");
      String id = (String) entity.getKey().getName();
      String name = (String) entity.getProperty(NAME_PROPERTY);
      String location = (String) entity.getProperty(LOCATION_PROPERTY);
      String lat = (String) entity.getProperty(LAT_PROPERTY);
      String lng = (String) entity.getProperty(LONG_PROPERTY);
      
      MapInfo business = new MapInfo(id, name, location, lat, lng);
      latList.add(business);
    }

    // Filter businesses that are within the map search bounds.
    Filter lngFilter =
            CompositeFilterOperator.and(
              new FilterPredicate(IS_BUSINESS_PROPERTY, FilterOperator.EQUAL, YES),
              CompositeFilterOperator.and(
                new FilterPredicate(LONG_PROPERTY, FilterOperator.GREATER_THAN_OR_EQUAL, SW_Lng),
                new FilterPredicate(LONG_PROPERTY, FilterOperator.LESS_THAN_OR_EQUAL, NE_Lng)));

    // Convert entities to Profile objects.
    Query lngQuery = new Query(PROFILE_TASK_NAME).setFilter(lngFilter);
    PreparedQuery lngResults = datastore.prepare(lngQuery);
    Set<MapInfo> lngList = new HashSet<MapInfo>();
    for (Entity entity : lngResults.asIterable()) {
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
      System.out.println("Entered for loop #1 ");
      String id = (String) entity.getKey().getName();
      String name = (String) entity.getProperty(NAME_PROPERTY);
      String location = (String) entity.getProperty(LOCATION_PROPERTY);
      String lat = (String) entity.getProperty(LAT_PROPERTY);
      String lng = (String) entity.getProperty(LONG_PROPERTY);
      
      MapInfo business = new MapInfo(id, name, location, lat, lng);
      lngList.add(business);
    }

    System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
    System.out.println("lat list is: " + latList);
    System.out.println("lng list is: " + lngList);

    // Compare the two results 
    List<MapInfo> resultsList = lngList.stream()
                                      .filter(latList::contains)
                                      .collect(Collectors.toList());

    response.setContentType("application/json;");
    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(latList));
  }
}
