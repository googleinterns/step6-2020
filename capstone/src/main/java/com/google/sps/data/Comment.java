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

import java.text.SimpleDateFormat;

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
  private final String name;
  private final boolean hasReplies;
  private final String timestampStr;

  public Comment(
      String id,
      String content,
      long timestamp,
      String userId,
      String name,
      String businessId,
      String parentId,
      boolean hasReplies) {
    this.id = id;
    this.content = content;
    this.timestamp = timestamp;
    this.userId = userId;
    this.name = name;
    this.businessId = businessId;
    this.parentId = parentId;
    this.hasReplies = hasReplies;
    // Epoch timestamp is formatted in UTC time
    this.timestampStr = new SimpleDateFormat("MM/dd/yy HH:mm").format(timestamp);
  }
}
