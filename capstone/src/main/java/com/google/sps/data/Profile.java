package com.google.sps.data;

public final class Profile {

  private String id;
  private String name;
  private String location;
  private String bio;
  private String story;
  private String about;
  private String support;

  public Profile(String id, String name, String location, String bio, String story = "", String about = "", String support = "") {
    this.id = id;
    this.name = name;
    this.location = location;
    this.bio = bio;
    this.story = story;
    this.about = about;
    this.support = support;
  }
}
