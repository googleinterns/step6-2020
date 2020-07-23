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

import com.google.appengine.api.datastore.GeoPt;

// This stores some information about business to display on the home page map.
public final class MapInfo {

  private String id;
  private String name;
  private String location;
  private String bio;
  private GeoPt geoPt;

  /**
   * Business user's information constructor.
   *
   * @param id the unique id of the user.
   * @param name the user's name.
   * @param location the user's location.
   * @param bio the user's bio.
   * @param geoPt the user's lat and long coordinates.
   */
  public MapInfo(String id, String name, String location, String bio, GeoPt geoPt) {
    this.id = id;
    this.name = name;
    this.location = location;
    this.bio = bio;
    this.geoPt = geoPt;
  }
}
