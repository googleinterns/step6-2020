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

import { loadCommentSection } from '/js/comments.js';

window.onload = function() {
  // window.location.search returns the search string of the URL.
  // In this case, window.location.search = ?id={businessID}
  const url = new URLSearchParams(window.location.search);
  const businessId = url.get('id');
  constructBusinessProfile(businessId);
  loadCommentSection(document.getElementById('comment-section'));
}

function constructBusinessProfile(id) {
  const profileInfo = document.getElementById('view-profile-section');
  fetch('/business/' + id).then(response => response.json()).then(info => {
    document.getElementById("profile-name").innerText = info.name;
    document.getElementById("profile-location").innerText = info.location;
    document.getElementById("profile-bio").innerText = info.bio;
    // Opens a draft of the email to the business.
    document.getElementById("profile-email").href = 'mailto:' + info.email;
  })
}
