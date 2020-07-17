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

import static com.google.sps.data.CommentDatastoreUtil.BUSINESS_ID_PROPERTY;
import static com.google.sps.data.CommentDatastoreUtil.COMMENT_TASK_NAME;
import static com.google.sps.data.CommentDatastoreUtil.PARENT_ID_PROPERTY;
import static com.google.sps.data.CommentDatastoreUtil.TIMESTAMP_PROPERTY;
import static com.google.sps.data.CommentDatastoreUtil.USER_ID_PROPERTY;
import static com.google.sps.data.CommentDatastoreUtil.generateComment;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that provides a list of comments */
@WebServlet("/comments")
public class CommentsServlet extends HttpServlet {

  // TODO (bergmoney@) make limit variable when supporting pagination
  private static final int COMMENT_LIMIT = 20;

  private final String INVALID_ARGUMENT_MESSAGE =
      "Requests must have exactly one of the following parameters: "
          + USER_ID_PROPERTY
          + ", "
          + BUSINESS_ID_PROPERTY
          + ", or "
          + PARENT_ID_PROPERTY;

  private static final Set<String> FILTER_PROPERTIES =
      Stream.of(USER_ID_PROPERTY, BUSINESS_ID_PROPERTY, PARENT_ID_PROPERTY)
          .collect(Collectors.toSet());

  DatastoreService datastore;

  public CommentsServlet() {
    this(DatastoreServiceFactory.getDatastoreService());
  }

  public CommentsServlet(DatastoreService ds) {
    this.datastore = ds;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IllegalArgumentException, IOException {
    // Extract the filter parameter from the request
    List<String> filterParameters =
        FILTER_PROPERTIES.stream()
            .filter(propertyName -> request.getParameter(propertyName) != null)
            .collect(Collectors.toList());
    if (filterParameters.size() != 1) {
      // We allow only a single parameter
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, INVALID_ARGUMENT_MESSAGE);
      return;
    }
    String filterProperty = filterParameters.get(0);

    List<Entity> entities = runCommentsQuery(filterProperty, request.getParameter(filterProperty));

    List<Comment> comments =
        entities.stream()
            .map(entity -> generateComment(entity, datastore))
            .collect(Collectors.toList());

    String jsonComments = new Gson().toJson(comments);

    response.setContentType("application/json;");
    response.getWriter().println(jsonComments);
  }

  private List<Entity> runCommentsQuery(String filterProperty, String filterValue)
      throws IllegalArgumentException {
    Query query =
        new Query(COMMENT_TASK_NAME)
            .setFilter(buildFilter(filterProperty, filterValue))
            .addSort(TIMESTAMP_PROPERTY, SortDirection.DESCENDING);
    return datastore.prepare(query).asList(FetchOptions.Builder.withLimit(COMMENT_LIMIT));
  }

  /**
   * Build a filter on the comments based on parameters supplied in the get request. If comments are
   * filtered by business we additionally filter out replies.
   */
  private Filter buildFilter(String filterProperty, String filterValue) {
    Filter primaryFilter = new FilterPredicate(filterProperty, FilterOperator.EQUAL, filterValue);

    if (filterProperty.equals(BUSINESS_ID_PROPERTY)) {
      // If the user requests comments for a particular business page we return only top level
      // comments
      return new CompositeFilter(
          CompositeFilterOperator.AND,
          Arrays.asList(
              primaryFilter,
              // Create additional filter for top level comments only
              new FilterPredicate(PARENT_ID_PROPERTY, FilterOperator.EQUAL, "")));
    } else {
      return primaryFilter;
    }
  }
}
