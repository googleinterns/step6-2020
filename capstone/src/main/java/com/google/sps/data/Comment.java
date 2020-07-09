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

public final class Comment {
  /**
   * Represents a user's comment.
   *
   * @param id the comments identifier
   * @param content the content of the comment
   * @param timestamp when the comment was posted
   * @param userId the user who posted the comment
   * @param businessId the business on who's page the comment was posted
   * @param parentId if the comment is a reply this specifies the id of the comment it is replying
   *     to
   */
  private final String id;

  private final String content;
  private final long timestamp;
  private final String userId;
  private final String businessId;
  private final String parentId;

  public Comment(
      String id,
      String content,
      long timestamp,
      String userId,
      String businessId,
      String parentId) {
    this.id = id;
    this.content = content;
    this.timestamp = timestamp;
    this.userId = userId;
    this.businessId = businessId;
    this.parentId = parentId;
  }

  public Comment(String id, String content, long timestamp, String userId, String businessId) {
    this(id, content, timestamp, userId, businessId, "");
  }
}