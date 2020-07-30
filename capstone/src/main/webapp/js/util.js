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

/** Wrap a value in a promise to simulate a server request. */
export function wrapInPromise(val) {
  return new Promise((resolve, reject) => resolve(val));
}

/** Build html element of specified type and content */
export function buildElement(type, content) {
  let element = document.createElement(type);
  element.innerText = content;

  return element;
}

/** Build button with a given class name, action when clicked, and label */
export function buildButton(className, clickAction, label) {
  let button = document.createElement('button');

  button.className = className;
  button.addEventListener('click', clickAction);
  button.innerText = label;

  return button;
}

/** Update log-in status of users in nav bar. */
export function setLoginOrLogoutUrl() {
  fetch('/login').then(response => response.json()).then((user) => {
    const loginElement = document.getElementById('auth-button');
    loginElement.appendChild(buildLoginOrLogoutLink(user.url, user.isLoggedin));
  });
}

/** Update profile link of users in nav bar. */
export function setProfileUrl() {
  fetch('/login').then(response => response.json()).then((user) => {
    const profileElement = document.getElementById('profile-button');
    if (user.userId == null) { 
      profileElement.style.display = 'none';
      return; 
    }

    profileElement.appendChild(buildProfileLink(user.isBusiness, user.userId));
  });
}

export function buildLinkElement(url, text) {
  const linkElement = document.createElement('a');

  linkElement.innerText = text;
  linkElement.href = url;

  return linkElement;
}

/** Helper function for building the login/logout link. */
function buildLoginOrLogoutLink(url, isLoggedin) {    
  let label;

  if (isLoggedin) {
    label = 'Logout';
  } else {
    label = 'Login';
  }
  
  const link = buildLinkElement(url, label);
  link.classList.add('nav-link');

  return link;
}

/** Helper function for building the profile link. */
function buildProfileLink(isBusiness, userId) {
  const aElement = document.createElement('a');
  var url;
  aElement.innerText = 'Profile';
  aElement.classList.add('nav-link');
  if (isBusiness === 'Yes') {
    url = 'business.html?id=' + userId;
  } else {
    url = 'profile.html?id=' + userId;
  }

  aElement.setAttribute('href', url);

  return aElement;
}

export function checkUserLoggedIn() {
  return getJsonObject('/login').then(user => user.isLoggedin);
}

/** Use fetch to get a Json Object and then unpack that object */
export function getJsonObject(url, parameters = {}) {
  return makeGetRequest(url, parameters).then(response => response.json());
}

export function makeRequest(url, parameters, type) {
  if (Object.keys(parameters).length > 0) {
    // Add parameter fields to the url as query parameters
    
    // Create query parameter strings to be added to the url
    let queryParamStrings = Object.keys(parameters).map(key => key + '=' + parameters[key]);
    
    url += '?' + queryParamStrings.join('&');
  }
  
  return fetch(url, {method: type});
}

export function makePostRequest(url, parameters) {
  return makeRequest(url, parameters, 'POST');
}

export function makeGetRequest(url, parameters = {}) {
  if (Object.keys(parameters).length > 0) {
    // Add parameter fields to the url as query parameters
    
    // Create query parameter strings to be added to the url
    let queryParamStrings = Object.keys(parameters).map(key => key + '=' + parameters[key]);
    
    url += '?' + queryParamStrings.join('&');
  }
  
  return fetch(url);
}
