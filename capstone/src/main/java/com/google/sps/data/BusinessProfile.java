package com.google.sps.data;

public final class BusinessProfile {

  private final String id;
  private final String name;
  private final String calendarEmail;
  private final String bio;
  private final String location;
  private final String story;
  private final String about;
  private final String support;

  public BusinessProfile(
      String id,
      String name,
      String calendarEmail,
      String bio,
      String location,
      String story,
      String about,
      String support) {
    this.id = id;
    this.name = name;
    this.calendarEmail = calendarEmail;
    this.bio = bio;
    this.location = location;
    this.story = story;
    this.about = about;
    this.support = support;
  }
}
