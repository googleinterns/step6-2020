package com.google.sps.data;

public final class BusinessProfile {

  private final String name;
  private final String email;
  private final String bio;
  private final String location;

  public BusinessProfile(String name, String email, String bio, String location) {
    this.name = name;
    this.email = email;
    this.bio = bio;
    this.location = location;
  }
}
