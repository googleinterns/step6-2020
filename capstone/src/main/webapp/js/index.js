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

import { buildElement, getLoginStatus } from '/js/util.js';

window.onload = function() {
  // Fetches all the businesses to be displayed.
  const businessList = document.getElementById('businesses');
  fetch('/businesses').then(response => response.json()).then(businesses => {
    businesses.forEach(business => {
      businessList.appendChild(createCard(business));
    })
  })
  // Get login status of user to display on nav bar.
  getLoginStatus();
}

function createCard(business) {
  let businessCard = document.createElement('div');
  businessCard.classList.add('business-card');
  businessCard.id = 'business-' + business.id;

  businessCard.appendChild(createBusinessLink(business.id, business.name));
  businessCard.appendChild(createBusinessInfo(business.bio));
  businessCard.appendChild(createShowMoreButton(business.id));
  return businessCard;
}

function createBusinessLink(id, name) {
  // Creates a business card header that links to the respective business page.
  let businessLink = buildElement('a', '');
  businessLink.href = 'business.html';
  businessLink.appendChild(buildElement('h2', name));
  return businessLink;
}

function createBusinessInfo(bio) {
  // Creates a description to the business.
  let businessInfo = buildElement('p', bio);
  businessInfo.classList.add('business-info');
  return businessInfo;
}

function createShowMoreButton(id) {
  // Creates the show more button at the bottom of the business card.
  let showMore = buildElement('p', '');
  let showMoreSpan = buildElement('span', 'Show More');
  showMore.appendChild(showMoreSpan);
  showMore.classList.add('show-more');
  showMore.id = id;
  showMore.onclick = function() { showMoreInfo(id); };
  return showMore;
}

function showMoreInfo(id) {
  const showElement = document.getElementById(id);
  const businessCard = document.getElementById('business-' + id);

  // For now, it is just doing this with the first element.
  // This will be handled better with unique IDs assigned via the servlets.
  showElement.style.display = 'none';

  // id.slice(-1) gets the index of the business as specified by the id
  businessCard.style.maxHeight = 'none';
}
