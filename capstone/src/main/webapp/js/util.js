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

/** Perform integer division, rounding the floating point result down. */
export function div(a, b) {
  return Math.floor(a / b);
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

/** Helper function for building the login/logout link. */
function buildLoginOrLogoutLink(url, isLoggedin) {    
  const aElement = document.createElement('a');
  aElement.setAttribute('href', url);
  if (isLoggedin) {
    aElement.innerText = 'Logout';
  } else {
    aElement.innerText = 'Login';
  }
  return aElement;
}

/** Helper function for building the profile link. */
function buildProfileLink(isBusiness, userId) {
  const aElement = document.createElement('a');
  var url;
  aElement.innerText = 'Profile';
  if (isBusiness === 'Yes') {
    url = 'business.html?id=' + userId;
  } else {
    url = 'profile.html?id=' + userId;
  }

  aElement.setAttribute('href', url);

  return aElement;
}
