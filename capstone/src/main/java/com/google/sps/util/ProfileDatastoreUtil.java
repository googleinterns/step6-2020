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

package com.google.sps.data;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public final class ProfileDatastoreUtil {
  public static final String PROFILE_TASK_NAME = "UserProfile";

  public static final String IS_BUSINESS_PROPERTY = "isBusiness";
  public static final String NAME_PROPERTY = "name";
  public static final String LOCATION_PROPERTY = "location";
  public static final String BIO_PROPERTY = "bio";
  public static final String STORY_PROPERTY = "story";
  public static final String ABOUT_PROPERTY = "about";
  public static final String SUPPORT_PROPERTY = "support";
  public static final String ANONYMOUS_NAME = "Anonymous";
  public static final String NULL_STRING = "";
  public static final String NO = "No";
  public static final String YES = "Yes";

  /** Get the username associated with a given Id */
  public static String getProfileName(String userId, DatastoreService datastore)
      throws IllegalArgumentException {
    Key userKey = KeyFactory.createKey(PROFILE_TASK_NAME, userId);

    Entity userProfile;
    try {
      userProfile = datastore.get(userKey);
    } catch (EntityNotFoundException e) {
      throw new IllegalArgumentException(
          "Database does not contain an entity with the userId " + userId);
    }

    return (String) userProfile.getProperty(NAME_PROPERTY);
  }
}
