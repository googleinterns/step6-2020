// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the 'License');
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an 'AS IS' BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import { loadCommentSection } from '/js/comments.js'
import { getLoginStatus } from '/js/util.js';

// Toggle between view and edit profile options.
window.addEventListener('load', function() {
  // Get login status of user to display on nav bar.
  getLoginStatus();
  displayProfile();

  loadCommentSection(document.getElementById('comment-section'));
})

// Toggle between view and edit profile options.
window.toggleProfile = function() {
  var viewProfile = document.getElementById('view-profile-section');
  var editProfile = document.getElementById('edit-profile-section');

  if (viewProfile.style.display == 'block') {
    viewProfile.style.display = 'none';
    editProfile.style.display = 'block';
  } else {
    editProfile.style.display = 'none';
    viewProfile.style.display = 'block';
  }
}

// If user answered the first question: whether they are a business user or not,
// then show appropriate edit profile form.
window.hasAnswerQuestionnaire = function() {
  var isBusiness = document.getElementById("yes");
  var isNotBusiness = document.getElementById("no");
  
  var name = document.getElementById("name-section");
  var location = document.getElementById("location-section");
  var bio = document.getElementById("bio-section");
  var story = document.getElementById("story-section");
  var about = document.getElementById("about-section");
  var support = document.getElementById("support-section");

  if (isBusiness.checked == true) {
    name.style.display = 'block';
    location.style.display = 'block';
    bio.style.display = 'block';
    story.style.display = 'block';
    about.style.display = 'block';
    support.style.display = 'block';
  } 
  if (isNotBusiness.checked == true) {
    name.style.display = 'block';
    location.style.display = 'block';
    bio.style.display = 'block';
    story.style.display = 'none';
    about.style.display = 'none';
    support.style.display = 'none';
  }

    var submit = document.getElementById("submit-button");
    submit.style.display = 'block';
}

// Display the correct profile information.
function displayProfile() {
  var id = getId();
  fetch('/profile/'+id, {method:"GET"})
    .then(response => response.json())
    .then((userProfile) => {
      createProfile(userProfile.name, userProfile.location, userProfile.bio);
      displayEditButton(userProfile.isCurrentUser);
    });
}

// Obtain the ID from the URL params.
function getId() {
  const urlParams = new URLSearchParams(window.location.search);
  const id = urlParams.get('id'); 
  return id;
}

// Determine whether to display the edit button depends if user is viewing its profile page.
function displayEditButton(isCurrentUser) {
  var editButton =  document.getElementById("edit-button");
  if (isCurrentUser) {
    editButton.style.display = 'block';
  } else {
    editButton.style.display = 'none';
  }
}

// Add correct text to each HTML element of profile page.
function createProfile(name, location, bio) {
  var name_section = document.getElementById("name");
  var location_section = document.getElementById("location");
  var bio_section = document.getElementById("bio");

  name_section.innerText = name;
  location_section.innerText = location;
  bio_section.innerText = bio;
}
