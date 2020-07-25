// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the 'License');
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an 'AS IS' BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withDefaults;
import static com.google.sps.data.FollowDatastoreUtil.BUSINESS_ID_PROPERTY;
import static com.google.sps.data.FollowDatastoreUtil.FOLLOW_TASK_NAME;
import static com.google.sps.data.FollowDatastoreUtil.USER_ID_PROPERTY;

import com.google.sps.data.Follow;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.gson.Gson;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/** Servlet for listing 'follows', that is instances of a User following a business. */
@WebServlet("/follows")
public class FollowsServlet extends HttpServlet {
  private final String[] POSSIBLE_FILTER_PARAMETERS = 
      new String[] {BUSINESS_ID_PROPERTY, USER_ID_PROPERTY};
  
  private UserService userService = UserServiceFactory.getUserService();
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
    List<String> requestFilters = 
        Arrays.stream(POSSIBLE_FILTER_PARAMETERS)
            .filter(parameter -> request.getParameter(parameter) != null)
            .collect(Collectors.toList());
    
    if (requestFilters.size() != 1) {
      response.sendError(
            HttpServletResponse.SC_BAD_REQUEST, 
            "Must specify either businessId or userId, but not both.");
      return;
    }

    String filterParameter = requestFilters.get(0);
    Query query = new Query(FOLLOW_TASK_NAME).setFilter(new FilterPredicate(filterParameter, FilterOperator.EQUAL, request.getParameter(filterParameter)));

    List<Entity> followEntities = datastore.prepare(query).asList(withDefaults());
    List<Follow> follows = followEntities.stream().map(followEntity -> generateFollow(followEntity)).collect(Collectors.toList());


    response.setContentType("application/json;");
    response.getWriter().println(new Gson().toJson(follows));    
  }

  private Follow generateFollow(Entity followEntity) {
    String userId = (String) followEntity.getProperty(USER_ID_PROPERTY);
    String businessId = (String) followEntity.getProperty(BUSINESS_ID_PROPERTY);

    return new Follow(userId, businessId);
  }
}