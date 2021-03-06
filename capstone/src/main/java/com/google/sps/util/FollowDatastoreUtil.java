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

import com.google.appengine.api.datastore.Entity;

public final class FollowDatastoreUtil {
  public static final String FOLLOW_TASK_NAME = "Follow";
  public static final String BUSINESS_ID_PROPERTY = "businessId";
  public static final String USER_ID_PROPERTY = "userId";

  public static Entity buildFollowEntity(String userId, String businessId) {
    Entity followEntity = new Entity(FOLLOW_TASK_NAME);

    followEntity.setProperty(USER_ID_PROPERTY, userId);
    followEntity.setProperty(BUSINESS_ID_PROPERTY, businessId);

    return followEntity;
  }
}
