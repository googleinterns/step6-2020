package com.google.sps.data;

/** Individual user login data. */
public final class User {
  // Whether the user is logged in or not.
  private final boolean isLoggedin;

  // The logged-in or logged-out URL links.
  private final String url;

  /**
   * User's information constructor.
   *
   * @param isLoggedin boolean value whether user is logged in or not.
   * @param url the logged-in or logged-out URL links.
   */
  public User(boolean isLoggedin, String url) {
    this.isLoggedin = isLoggedin;
    this.url = url;
  }
}
