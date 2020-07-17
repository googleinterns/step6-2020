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
  
  viewProfile.style.display = 'none';
  editProfile.style.display = 'block';
}

// If user answered the first question: whether they are a business user or not,
// then show appropriate edit profile form.
window.hasAnswerQuestionnaire = function() {
  let isBusiness = document.getElementById("yes");
  let isNotBusiness = document.getElementById("no");
  
  let businessQuesionnaire = document.getElementById("business-questionnaire");

  if (isBusiness.checked == true) {
    businessQuesionnaire.style.display = 'block';
  } 
  if (isNotBusiness.checked == true) {
    businessQuesionnaire.style.display = 'none';
  }

  let submit = document.getElementById('submit-button');
  submit.style.display = 'block';
}

// Display the correct profile information.
function displayProfile() {
  let id = getId();
  fetch('/profile/'+id)
    .then(response => {
      if (!response.ok) {
        throw new Error("404 error");
      }
      return response.json();
    })
    .then((userProfile) => {
      createProfile(userProfile.name, userProfile.location, userProfile.bio);
      displayEditButton(userProfile.isCurrentUser);
      setEditValues(userProfile.name, userProfile.location, userProfile.bio);
    }).catch((e) => {
      redirectToDefaultPage(id);
    });
}

function redirectToDefaultPage(id) {
  location.assign('/profile/' + id);
}

// Submit the edit-profile form to servlet based on whether they're a business or not.
window.submitProfileForm = function() {
  let form = document.getElementById('edit-profile');
  form.method = 'POST';

  if(document.getElementById('yes').checked) {
    form.action = '/business';
    return;
  }
  
  form.action = '/profile';
}


// Obtain the ID from the URL params.
function getId() {
  const urlParams = new URLSearchParams(window.location.search);
  const id = urlParams.get('id'); 
  return id;
}

// Determine whether to display the edit button depends if user is viewing its profile page.
function displayEditButton(isCurrentUser) {
  let editButton =  document.getElementById('edit-button');
  if (isCurrentUser) {
    editButton.style.display = 'block';
  } else {
    editButton.style.display = 'none';
  }
}

// Add correct text to each HTML element of profile page.
function createProfile(name, location, bio) {
  let nameSection = document.getElementById('name');
  let locationSection = document.getElementById('location');
  let bioSection = document.getElementById('bio');

  nameSection.innerText = name;
  locationSection.innerText = location;
  bioSection.innerText = bio;

  createProfileMap(location);
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

// Bias the autocomplete object to the user's geographical location,
// as supplied by the browser's 'navigator.geolocation' object.
window.geolocate = function() {
  // Create the autocomplete object, restricting the search predictions to
  // geographical location types.
  let autocomplete = new google.maps.places.Autocomplete(
      document.getElementById('edit-location'), {types: ['geocode']});

  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(function(position) {
      let geolocation = {
        lat: position.coords.latitude,
        lng: position.coords.longitude
      };
      let circle = new google.maps.Circle(
          {center: geolocation, radius: position.coords.accuracy});
      autocomplete.setBounds(circle.getBounds());
    });
  }
}

// Create the mini map on profile page.
function createProfileMap(address) {
  let map = new google.maps.Map(document.getElementById('map'), {
    zoom: 8,
    center: {lat: -34.397, lng: 150.644}
  });
  let geocoder = new google.maps.Geocoder();
  let bounds = new google.maps.LatLngBounds();
  geocodeAddress(address, geocoder, map, bounds);
}

// Helper function to geocode the location and place a marker on map.
function geocodeAddress(address, geocoder, resultsMap, bounds) {
  geocoder.geocode({'address': address}, function(results, status) {
    if (status === 'OK') {
      if (bounds.isEmpty()) bounds = results[0].geometry.bounds;
      else bounds.union(results[0].geometry.bounds);
      let marker = new google.maps.Marker({
        map: resultsMap,
        position: results[0].geometry.location
      });     
      resultsMap.fitBounds(bounds);
    } else {
      alert('Geocode was not successful for the following reason: ' + status);
    }
  });
}
