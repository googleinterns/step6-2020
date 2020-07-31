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

import { buildCommentForm, loadCommentList } from '/js/comments.js';
import { 
  buildButton,
  checkUserLoggedIn,
  getJsonObject,
  makeRequest,
  setLoginOrLogoutUrl,
  setProfileUrl,
  } from '/js/util.js';

const calendarBaseURL = 'https://calendar.google.com/calendar/embed?src=';

function getBusinessId() {
  const url = new URLSearchParams(window.location.search);
  return url.get('id');
}

window.addEventListener('load', function() {
  // window.location.search returns the search string of the URL.
  // In this case, window.location.search = ?id={businessID}
  setLoginOrLogoutUrl();
  setProfileUrl();
  const businessId = getBusinessId();
  constructBusinessProfile(businessId);

  const commentSection = document.getElementById('comment-section');
  checkUserLoggedIn().then(userIsLoggedIn => {
    commentSection.appendChild(buildCommentForm(userIsLoggedIn, businessId));
    commentSection.appendChild(loadCommentList('businessId', businessId));
    initFollowButton();
  })
})

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
}

// Submit the edit-profile form to servlet based on whether they're a business or not.
window.submitProfileForm = function() {
  let form = document.getElementById('edit-profile');
  form.method = 'POST';
  
  if(document.getElementById('yes').checked) {
    form.action = '/business';
  } else {
    form.action = '/profile';
  }
  
  if (document.getElementById('edit-location').value.length == 0) {
    form.submit();
  } else {
    convertToLatLong().then((results) => {
      form.submit();
    }) 
  }
}

// Convert address to latitude and longitude values.
async function convertToLatLong() {
  let address = document.getElementById('edit-location').value;
  let lat = document.getElementById('edit-lat');
  let long = document.getElementById('edit-long');

  let results = await geocoderPromise({'address': address});
  lat.value = results[0].geometry.location.lat();
  long.value = results[0].geometry.location.lng();
}

function geocoderPromise(request) {
  let geocoder = new google.maps.Geocoder();

  return new Promise((resolve, reject) => {
    geocoder.geocode(request, function(results, status) {
      if (status == google.maps.GeocoderStatus.OK) {
        resolve(results);
      } else {
        reject();
      }
    })
  })
}

// Set the correct values for both view and edit sections.
function constructBusinessProfile(id) {
  const profileInfo = document.getElementById('view-business-section');
  const profileButton = document.getElementById('profile-button');

  fetch('/business/' + id)
      .then(response => {
          if (!response.ok) {
            // Redirect to BusinessServlet, which displays the appropriate error.
            window.location.href = '/business/' + id;
          }
          return response.json();
      }).then(info => {
        if (info.isCurrentUser) {
          document.getElementById('edit-button').style.display = 'block';
          document.getElementById('getStarted-button').style.display = 'block';
          profileButton.classList.add('active');
        } else {
          document.getElementById('edit-button').style.display = 'none';
          document.getElementById('getStarted-button').style.display = 'none';
          profileButton.classList.remove('active');
        }
        
        const calendarDiv = document.getElementById('business-calendar');
        if (info.calendarEmail != '') {
          calendarDiv.style.display = 'block';
          let calendar = document.createElement('iframe');
          calendar.src = calendarBaseURL + info.calendarEmail;
          calendar.height = '500px';
          calendar.width = '700px';
          calendarDiv.appendChild(calendar);
          
          document.getElementById('edit-calendar').value = info.calendarEmail;
          const previewCalendar = document.getElementById('edit-calendar-preview');
          previewCalendar.style.display = 'block';
          previewCalendar.src = calendarBaseURL + info.calendarEmail;
          document.getElementById('calendar-warning').style.display = 'block';
        }

        createProfileMap(info['location']);

        ['name', 'location', 'story', 'bio', 'about', 'support'].forEach(property => {
          document.getElementById('business-' + property).innerText = info[property];
          document.getElementById('edit-' + property).value = info[property];
        })
      })
}

function initFollowButton() {
  getJsonObject('/follow', {'businessId' : getBusinessId()}).then(isFollowingBusiness => {
    if (!isFollowingBusiness) {
      // Change follow button to unfollow button
      setFollowButtonToFollow();

    } else {
      // Keep it as the follow button and add the eventlistener
      setFollowButtonToUnfollow();
    }
  });
}

function setFollowButtonToFollow() {
  const button = document.getElementById('follow-button');

  button.className = "btn btn-light float-right";
  button.innerText = 'Follow';

  button.removeEventListener('click', unfollowBusiness);
  button.addEventListener('click', followBusiness);
}

function setFollowButtonToUnfollow() {
  const button = document.getElementById('follow-button');

  button.className = "btn btn-dark float-right";
  button.innerText = 'Unfollow'; 
  
  button.removeEventListener('click', followBusiness);
  button.addEventListener('click', unfollowBusiness);
}

function followBusiness() {
  const url = new URLSearchParams(window.location.search);
  const businessId = url.get('id');
  
  makeRequest('/follow', {'businessId': businessId}, 'POST').then(setFollowButtonToUnfollow);
}

function unfollowBusiness() {
  const url = new URLSearchParams(window.location.search);
  const businessId = url.get('id');

  makeRequest('/follow', {'businessId': businessId}, 'DELETE').then(setFollowButtonToFollow);
}

window.toggleProfile = function() {
  const viewProfile = document.getElementById('view-business-section');
  const editProfile = document.getElementById('edit-business-section');

  viewProfile.style.display = 'none';
  editProfile.style.display = 'block';
}

window.previewCalendar = function() {
  const calendar = document.getElementById('edit-calendar-preview');
  const email = document.getElementById('edit-calendar').value;
  const warningMessage = document.getElementById('calendar-warning');
  if (email == '') {
    calendar.style.display = 'none';
    warningMessage.style.display = 'none';
  } else {
    calendar.style.display = 'block';
    calendar.src = calendarBaseURL + email;
    warningMessage.style.display = 'block';
  }
}

// Create the mini map on profile page.
function createProfileMap(address) {
  // Default center at MTV, California.
  let map = new google.maps.Map(document.getElementById('map'), {
    zoom: 8,
    center: {lat: 37.3861, lng: -122.0839}
  });
  let geocoder = new google.maps.Geocoder();
  let bounds = new google.maps.LatLngBounds();
  geocodeAddress(address, geocoder, map, bounds);
}

// Helper function to geocode the location and place a marker on map.
function geocodeAddress(address, geocoder, resultsMap, bounds) {
  let mapElement = document.getElementById('map');

  geocoder.geocode({'address': address}, function(results, status) {
    if (status === 'OK') {
      if (bounds.isEmpty()) bounds = results[0].geometry.bounds;
      else bounds.union(results[0].geometry.bounds);
      mapElement.style.display = 'block';
      let marker = new google.maps.Marker({
        map: resultsMap,
        position: results[0].geometry.location
      });     
      resultsMap.fitBounds(bounds);
    } else {
      mapElement.style.display = 'none';
    }
  });
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
