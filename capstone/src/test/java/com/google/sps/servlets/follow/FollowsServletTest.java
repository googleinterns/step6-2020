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

import static com.google.sps.data.FollowDatastoreUtil.BUSINESS_ID_PROPERTY;
import static com.google.sps.data.FollowDatastoreUtil.FOLLOW_TASK_NAME;
import static com.google.sps.data.FollowDatastoreUtil.USER_ID_PROPERTY;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
// import com.google.appengine.api.datastore.Query.CompositeFilter;
// import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
// import com.google.appengine.api.datastore.Query.FilterOperator;
// import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
// import java.util.Arrays;
// import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class FollowsServletTest {
  private static final String MOCK_EMAIL = "tutorguy@gmail.com";
  private static final String MOCK_DOMAIN = "microsoft.com";
  private static final String MOCK_USER_ID_1 = "1";
  // private static final String MOCK_USER_ID_2 = "2";
  // private static final String MOCK_BUSINESS_ID_1 = "3";
  // private static final String MOCK_BUSINESS_ID_2 = "4";
  // private static final String NON_EXISTENT_BUSINESS_ID = "99";

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
              new LocalDatastoreServiceTestConfig(), new LocalUserServiceTestConfig())
          // All this is necessary to get the fake userService to return a user
          .setEnvEmail(MOCK_EMAIL)
          .setEnvAuthDomain(MOCK_DOMAIN)
          .setEnvIsLoggedIn(true)
          .setEnvAttributes(
              new HashMap(
                  ImmutableMap.of(
                      "com.google.appengine.api.users.UserService.user_id_key", MOCK_USER_ID_1)));

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;

  private StringWriter servletResponseWriter;
  private FollowServlet servlet;
  private DatastoreService ds;

  @Before
  public void setUp() throws IOException {
    MockitoAnnotations.initMocks(this);
    helper.setUp();

    ds = DatastoreServiceFactory.getDatastoreService();

    ds.put(createMockBusinessEntity(MOCK_BUSINESS_ID_1));
    ds.put(createMockBusinessEntity(MOCK_BUSINESS_ID_2));

    servletResponseWriter = new StringWriter();
    doReturn(new PrintWriter(servletResponseWriter)).when(response).getWriter();

    servlet = new FollowServlet();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }
}


