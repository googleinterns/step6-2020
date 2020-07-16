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

/** Individual user login data. */
public final class User {
  // Whether the user is logged in or not.
  private final boolean isLoggedin;

  // The logged-in or logged-out URL links.
  private final String url;

  // The user's id.
  private final String userId;

  // Whether the user is a business or not.
  private final String isBusiness;

  /**
   * User's information constructor.
   *
   * @param isLoggedin boolean value whether user is logged in or not.
   * @param url the logged-in or logged-out URL links.
   * @param userId the user's id.
   * @param isBusiness whether the user is a business or not.
   */
  public User(boolean isLoggedin, String url, String userId, String isBusiness) {
    this.isLoggedin = isLoggedin;
    this.url = url;
    this.userId = userId;
    this.isBusiness = isBusiness;
  }
}
