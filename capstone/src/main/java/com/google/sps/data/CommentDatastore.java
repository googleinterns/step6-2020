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
  public static final String COMMENT_ENTITY_NAME = "Comment";

  public static final String CONTENT_PROPERTY = "content";
  public static final String TIMESTAMP_PROPERTY = "timestamp";
  public static final String USER_ID_PROPERTY = "userId";
  public static final String BUSINESS_ID_PROPERTY = "businessId";
  public static final String PARENT_ID_PROPERTY = "parentId";

  public static Comment generateComment(Entity commentEntity) {
    String id = commentEntity.getKey().getName();
    String content = (String) commentEntity.getProperty(CONTENT_PROPERTY);
    long timestamp = (long) commentEntity.getProperty(TIMESTAMP_PROPERTY);
    String userId = (String) commentEntity.getProperty(USER_ID_PROPERTY);
    String businessId = (String) commentEntity.getProperty(BUSINESS_ID_PROPERTY);
    String parentId = (String) commentEntity.getProperty(PARENT_ID_PROPERTY);

    return new Comment(id, content, timestamp, userId, businessId, parentId);
  }
}
