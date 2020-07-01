package com.google.sps.data;

public final class BusinessProfile {

  private final long id;
  private final String name;
  private final String email;
  private final String bio;
  private final String location;

  public BusinessProfile(long id, String name, String email, String bio, String location) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.bio = bio;
    this.location = location;
  }
}
