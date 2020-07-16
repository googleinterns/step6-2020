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
import { setLoginOrLogoutUrl, setProfileUrl } from '/js/util.js';

window.onload = function() {
  // window.location.search returns the search string of the URL.
  // In this case, window.location.search = ?id={businessID}
  const url = new URLSearchParams(window.location.search);
  const businessId = url.get('id');
  setLoginOrLogoutUrl();
  setProfileUrl();
  constructBusinessProfile(businessId);
  loadCommentSection(document.getElementById('comment-section'));
}

// Set the correct values for both view and edit sections.
function constructBusinessProfile(id) {
  const profileInfo = document.getElementById('view-business-section');
  fetch('/business/' + id).then(response => response.json()).then(info => {
    if (info.isCurrentUser) {
      document.getElementById('edit-button').style.display = 'block';
    } else {
      document.getElementById('edit-button').style.display = 'none';
    }

    document.getElementById('business-name').innerText = info.name;
    document.getElementById('edit-name').value = info.name;

    document.getElementById('business-location').innerText = info.location;
    document.getElementById('edit-location').value = info.location;

    document.getElementById('business-story').innerText = info.story;
    document.getElementById('edit-story').value = info.story;

    document.getElementById('business-bio').innerText = info.bio;
    document.getElementById('edit-bio').value = info.bio;

    document.getElementById('business-about').innerText = info.about;
    document.getElementById('edit-about').value = info.about;

    document.getElementById('business-support').innerText = info.support;
    document.getElementById('edit-support').value = info.support;
  })
}

window.toggleProfile = function() {
  const viewProfile = document.getElementById('view-business-section');
  const editProfile = document.getElementById('edit-business-section');

  viewProfile.style.display = 'none';
  editProfile.style.display = 'block';
}
