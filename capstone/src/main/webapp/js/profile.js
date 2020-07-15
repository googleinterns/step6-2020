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
import { setLoginOrLogoutUrl, setProfileUrl } from '/js/util.js';

// Toggle between view and edit profile options.
window.addEventListener('load', function() {
  // Get login status of user to display on nav bar.
  setLoginOrLogoutUrl();
  setProfileUrl();
  displayProfile();

  loadCommentSection(document.getElementById('comment-section'));
})

// Toggle between view and edit profile options.
window.toggleProfile = function() {
  let viewProfile = document.getElementById('view-profile-section');
  let editProfile = document.getElementById('edit-profile-section');

  if (viewProfile.style.display == 'block') {
    viewProfile.style.display = 'none';
    editProfile.style.display = 'block';

    // Display the edit form with default values.
    displayEditProfileValues();
  } else {
    editProfile.style.display = 'none';
    viewProfile.style.display = 'block';
  }
}

// If user answered the first question: whether they are a business user or not,
// then show appropriate edit profile form.
window.hasAnswerQuestionnaire = function() {
  let isBusiness = document.getElementById("yes");
  let isNotBusiness = document.getElementById("no");
  
  let basicQuesionnaire = document.getElementById("basic-questionnaire");
  let businessQuesionnaire = document.getElementById("business-questionnaire");

  if (isBusiness.checked == true) {
    basicQuesionnaire.style.display = 'block';
    businessQuesionnaire.style.display = 'block';
  } 
  if (isNotBusiness.checked == true) {
    basicQuesionnaire.style.display = 'block';
    businessQuesionnaire.style.display = 'none';
  }

  let submit = document.getElementById("submit-button");
  submit.style.display = 'block';
}

// Display the correct profile information.
function displayProfile() {
  let id = getId();
  fetch('/profile/'+id)
    .then(response => response.json())
    .then((userProfile) => {
      createProfile(userProfile.name, userProfile.location, userProfile.bio);
      displayEditButton(userProfile.isCurrentUser);
    });
}

// Display edit form values from datastore.
function displayEditProfileValues() {
  let id = getId();
  fetch('/profile/'+id)
    .then(response => response.json())
    .then((userProfile) => {
      setEditValues(userProfile.name, userProfile.location, userProfile.bio);
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
  let editButton =  document.getElementById("edit-button");
  if (isCurrentUser) {
    editButton.style.display = 'block';
  } else {
    editButton.style.display = 'none';
  }
}

// Add correct text to each HTML element of profile page.
function createProfile(name, location, bio) {
  let name_section = document.getElementById("name");
  let location_section = document.getElementById("location");
  let bio_section = document.getElementById("bio");

  name_section.innerText = name;
  location_section.innerText = location;
  bio_section.innerText = bio;
}

// Add correct values for each section when user is editing.
function setEditValues(name, location, bio) {
  let name_section = document.getElementById("edit-name");
  let location_section = document.getElementById("edit-location");
  let bio_section = document.getElementById("edit-bio");

  name_section.value = name;
  location_section.value = location;
  bio_section.value = bio;
}
