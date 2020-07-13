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

public final class CommentDatastore {
  public static Comment generateComment(Entity commentEntity) {
    String id = commentEntity.getKey().getName();
    String content = (String) commentEntity.getProperty(DatastoreNames.CONTENT_PROPERTY);
    long timestamp = (long) commentEntity.getProperty(DatastoreNames.TIMESTAMP_PROPERTY);
    String userId = (String) commentEntity.getProperty(DatastoreNames.USER_ID_PROPERTY);
    String businessId = (String) commentEntity.getProperty(DatastoreNames.BUSINESS_ID_PROPERTY);
    String parentId = (String) commentEntity.getProperty(DatastoreNames.PARENT_ID_PROPERTY);

    return new Comment(id, content, timestamp, userId, businessId, parentId);
  }
}
