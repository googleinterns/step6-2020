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

package com.google.sps.util;

import static com.google.sps.data.CommentDatastoreUtil.BUSINESS_ID_PROPERTY;
import static com.google.sps.data.CommentDatastoreUtil.COMMENT_TASK_NAME;
import static com.google.sps.data.CommentDatastoreUtil.CONTENT_PROPERTY;
import static com.google.sps.data.CommentDatastoreUtil.HAS_REPLIES_PROPERTY;
import static com.google.sps.data.CommentDatastoreUtil.PARENT_ID_PROPERTY;
import static com.google.sps.data.CommentDatastoreUtil.TIMESTAMP_PROPERTY;
import static com.google.sps.data.CommentDatastoreUtil.USER_ID_PROPERTY;

import com.google.appengine.api.datastore.Entity;

public class CommentTestUtil {
  public static Entity createCommentEntity(
      long timestamp, String userId, String businessId, boolean hasReplies) {
    Entity commentEntity = createCommentEntity(timestamp, userId, businessId, "");

    commentEntity.setProperty(HAS_REPLIES_PROPERTY, hasReplies);

    return commentEntity;
  }

  public static Entity createCommentEntity(
      long timestamp, String userId, String businessId, String parentId) {
    String id = generateUniqueCommentId(timestamp, userId, businessId);

    Entity comment = new Entity(COMMENT_TASK_NAME, id);

    comment.setProperty(CONTENT_PROPERTY, id);
    comment.setProperty(TIMESTAMP_PROPERTY, timestamp);
    comment.setProperty(USER_ID_PROPERTY, userId);
    comment.setProperty(BUSINESS_ID_PROPERTY, businessId);
    comment.setProperty(PARENT_ID_PROPERTY, parentId);
    comment.setProperty(HAS_REPLIES_PROPERTY, false);

    return comment;
  }

  public static String generateUniqueCommentId(long timestamp, String userId, String businessId) {
    return "1" + timestamp + userId + businessId;
  }
}
