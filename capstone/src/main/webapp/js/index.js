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

import { buildElement, setLoginOrLogoutUrl, setProfileUrl } from '/js/util.js';

let map, infoWindow;
let markers = [];
let autocomplete;
let MARKER_PATH = 'https://developers.google.com/maps/documentation/javascript/images/marker_green';

window.onload = function() {
  // Fetches all the businesses to be displayed.
  populateBusinessList();

  // Get login status of user to display on nav bar.
  setLoginOrLogoutUrl();
  
  // Update and set profile nav link.
  setProfileUrl();

  createHomePageMap();
}

function populateBusinessList() {
  const businessList = document.getElementById('businesses');
  fetch('/businesses').then(response => response.json()).then(businesses => {
    businesses.forEach(business => {
      businessList.appendChild(createCard(business));
    })
  })
}

function createCard(business) {
  let businessCard = document.createElement('div');
  businessCard.classList.add('business-card');
  businessCard.classList.add('card');
  businessCard.id = 'business-' + business.id;

  businessCard.appendChild(createBusinessLink(business.id, business.name));
  businessCard.appendChild(createBusinessInfo(business.bio));
  businessCard.appendChild(createShowMoreButton(business.id));
  return businessCard;
}

function createBusinessLink(id, name) {
  // Creates a business card header that links to the respective business page.
  let businessLink = buildElement('a', '');
  businessLink.href = 'business.html?id=' + id;
  businessLink.appendChild(buildElement('h2', name));
  businessLink.classList.add('card-title');
  return businessLink;
}

function createBusinessInfo(bio) {
  // Creates a description to the business.
  let businessInfo = buildElement('p', bio);
  businessInfo.classList.add('business-info');
  businessInfo.classList.add('card-text');
  return businessInfo;
}

function createShowMoreButton(id) {
  // Creates the show more button at the bottom of the business card.
  let showMore = buildElement('p', '');
  let showMoreSpan = buildElement('span', 'Show More');
  showMoreSpan.id = 'span-' + id;
  showMore.appendChild(showMoreSpan);
  showMore.classList.add('show-more');
  showMore.id = id;
  showMore.onclick = function() { showMoreInfo(id); };
  return showMore;
}

function showMoreInfo(id) {
  const showElement = document.getElementById('span-' + id);
  const businessCard = document.getElementById('business-' + id);

  if (showElement.innerText == 'Show More') {
    showElement.innerText = 'Show Less';
    businessCard.style.maxHeight = 'none';
  } else {
    showElement.innerText = 'Show More';
    businessCard.style.maxHeight = '150px';
  }
}

// Create the home page map.
function createHomePageMap() {
  // Default USA center map.
  map = new google.maps.Map(document.getElementById('map'), {
    zoom: 4,
    center: {lat: 37.1, lng: -95.7}
  });

  infoWindow = new google.maps.InfoWindow({
    content: document.getElementById('info-content')
  });

  // Create the autocomplete object and associate it with the UI input control.
  autocomplete = new google.maps.places.Autocomplete(
    (document.getElementById('autocomplete')), {
      types: ['geocode']
    });

  autocomplete.addListener('place_changed', onPlaceChanged);
}

// When the user enters a location, get the place details for the location and
// zoom the map in on the location.
function onPlaceChanged() {
  let place = autocomplete.getPlace();
  if (place.geometry) {
    map.panTo(place.geometry.location);
    map.fitBounds(place.geometry.viewport);
    search();
  } else {
    document.getElementById('autocomplete').placeholder = 'Enter a location';
  }
}

// Search for businesses in the selected city, within the viewport of the map.
function search() {
  let bounds = map.getBounds();
  let NEPoint = bounds.getNorthEast();
  let SWPoint = bounds.getSouthWest();

  let SW_Lat = SWPoint.lat();
  let SW_Lng = SWPoint.lng();
  let NE_Lat = NEPoint.lat();
  let NE_Lng = NEPoint.lng();

  fetch('/map?SW_Lat='+SW_Lat+'&SW_Lng='+SW_Lng+'&NE_Lat='+NE_Lat+'&NE_Lng='+NE_Lng)
    .then(response => response.json())
    .then(results => {
    
      clearResults();
      clearMarkers();

      // Create a marker for each business found, and
      // assign a letter of the alphabetic to each marker icon.
      for(let i = 0; i < results.length; i++) {
        let markerLetter = String.fromCharCode('A'.charCodeAt(0) + (i % 26));
        let markerIcon = MARKER_PATH + markerLetter + '.png';
        let coordinates = new google.maps.LatLng(results[i].geoPt.latitude, results[i].geoPt.longitude);

        // Use marker animation to drop the icons incrementally on the map.
        markers[i] = new google.maps.Marker({
          position: coordinates,
          animation: google.maps.Animation.DROP,
          icon: markerIcon
        });

        // If the user clicks a business marker, show the details of that business
        // in an info window.
        markers[i].placeResult = results[i];
        google.maps.event.addListener(markers[i], 'click', showInfoWindow.bind(markers[i], results[i].id, results[i].name, results[i].location));
        setTimeout(dropMarker(i), i * 100);
        addResult(results[i], results[i].id, i);
      }
  });
}

// Clear the markers from the map.
function clearMarkers() {
  for (let i = 0; i < markers.length; i++) {
    if (markers[i]) {
      markers[i].setMap(null);
    }
  }
  markers = [];
}

// Drop a marker on the map.
function dropMarker(i) {
  return function() {
    markers[i].setMap(map);
  };
}

// Add it to the results table next to the map.
function addResult(result, id, i) {
  let results = document.getElementById('results');
  let markerLetter = String.fromCharCode('A'.charCodeAt(0) + (i % 26));
  let markerIcon = MARKER_PATH + markerLetter + '.png';

  let tr = document.createElement('tr');
  tr.style.backgroundColor = (i % 2 === 0 ? '#F0F0F0' : '#FFFFFF');
  tr.onclick = function() {
    window.location.assign('/business.html?id='+id);
  };

  let iconTd = document.createElement('td');
  let nameTd = document.createElement('td');
  let icon = document.createElement('img');
  icon.src = markerIcon;
  icon.setAttribute('class', 'placeIcon');
  let name = document.createTextNode(result.name);
  iconTd.appendChild(icon);
  nameTd.appendChild(name);
  tr.appendChild(iconTd);
  tr.appendChild(nameTd);
  results.appendChild(tr);
}

function clearResults() {
  let results = document.getElementById('results');
  while (results.childNodes[0]) {
    results.removeChild(results.childNodes[0]);
  }
}

// Get the place details for a business. Show the information in an info window,
// anchored on the marker for the hotel that the user selected.
function showInfoWindow(id, name, location) {
  let marker = this;
  infoWindow.open(map, marker);
  buildIWContent(id, name, location);
}

// Load the place information into the HTML elements used by the info window.
function buildIWContent(id, name, location) {
  document.getElementById('map-name').textContent = name;
  document.getElementById('map-location').textContent = location;
  document.getElementById('map-link').href = '/business.html?id=' + id;
}

window.fetchSearchResults = function() {
  const searchItem = document.getElementById('business-text-search').value;
  const businessList = document.getElementById('businesses');
  
  // Clear business list for update.
  businessList.innerHTML = '';
  if (searchItem == '') {
    // Reset business list to original view of all businesses.
    populateBusinessList();
  } else {
    fetch('/search?searchItem=' + searchItem)
        .then(response => {
            if (!response.ok) {
              // Redirect to SearchServlet, which displays the appropriate error.
              window.location.href = '/search?searchItem=' + searchItem;
            }
            return response.json();
        }).then(businesses => {
            businesses.forEach(business => {
              businessList.appendChild(createCard(business));
            })
        });
  }
}
