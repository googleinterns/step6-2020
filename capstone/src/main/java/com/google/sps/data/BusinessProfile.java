package com.google.sps.data;

// This stores information about business owner user profile page.
public final class BusinessProfile {

  private String id;
  private String name;
  private String location;
  private String bio;
  private String story;
  private String about;
  private String support;
  private boolean isCurrentUser;

  /**
   * Business user's information constructor.
   *
   * @param id the unique id of the user.
   * @param name the user's name.
   * @param location the user's location.
   * @param bio the user's bio.
   * @param story the user's pandemic story.
   * @param about the user's additional business details.
   * @param support information on how others can support their business.
   * @param isCurrentUser whether this user is the loggedin current user.
   */
  public BusinessProfile(String id, String name, String location, String bio, String story, String about, String support, boolean isCurrentUser) {
    this.id = id;
    this.name = name;
    this.location = location;
    this.bio = bio;
    this.story = story;
    this.about = about;
    this.support = support;
    this.isCurrentUser = isCurrentUser;
  }
}
