package com.google.sps.data;

// This stores information about non-business owner user profile page.
public final class UserProfile {

  private String id;
  private String name;
  private String location;
  private String bio;
  private boolean isCurrentUser;

  /**
   * User's information constructor.
   *
   * @param id the unique id of the user.
   * @param name the user's name.
   * @param location the user's location.
   * @param bio the user's bio.
   * @param isCurrentUser determines whether the user's profile is the current logged-in user.
   */
  public UserProfile(String id, String name, String location, String bio, boolean isCurrentUser) {
    this.id = id;
    this.name = name;
    this.location = location;
    this.bio = bio;
    this.isCurrentUser = isCurrentUser;
  }
}
