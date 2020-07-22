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

package com.google.sps.servlets.authentication;

import static com.google.sps.data.ProfileDatastoreUtil.ANONYMOUS_NAME;
import static com.google.sps.data.ProfileDatastoreUtil.BIO_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.GEO_PT_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.IS_BUSINESS_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.LOCATION_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.NAME_PROPERTY;
import static com.google.sps.data.ProfileDatastoreUtil.NO;
import static com.google.sps.data.ProfileDatastoreUtil.NULL_STRING;
import static com.google.sps.data.ProfileDatastoreUtil.PROFILE_TASK_NAME;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Check if user is a new user signing in or not. If it's a new user, add new user to database. */
@WebServlet("/check_new_user")
public class NewUserServlet extends HttpServlet {

  UserService userService = UserServiceFactory.getUserService();

  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Create a key from user sign in ID and check if it's in the database.
    String userId = userService.getCurrentUser().getUserId();

    String keyString = KeyFactory.createKeyString(PROFILE_TASK_NAME, userId);
    Key userKey = KeyFactory.stringToKey(keyString);

    try {
      Entity entity = datastore.get(userKey);
    } catch (EntityNotFoundException e) {
      // Add user to database with default values.
      Entity userEntity = new Entity(PROFILE_TASK_NAME, userId);
      userEntity.setProperty(IS_BUSINESS_PROPERTY, NO);
      userEntity.setProperty(NAME_PROPERTY, ANONYMOUS_NAME);
      userEntity.setProperty(LOCATION_PROPERTY, NULL_STRING);
      userEntity.setProperty(GEO_PT_PROPERTY, NULL_STRING);
      userEntity.setProperty(BIO_PROPERTY, NULL_STRING);

      datastore.put(userEntity);
    }

    response.sendRedirect("/index.html");
  }
}
