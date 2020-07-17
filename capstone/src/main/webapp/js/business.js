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

window.addEventListener('load', function() {
  // window.location.search returns the search string of the URL.
  // In this case, window.location.search = ?id={businessID}
  const url = new URLSearchParams(window.location.search);
  const businessId = url.get('id');
  setLoginOrLogoutUrl();
  setProfileUrl();
  constructBusinessProfile(businessId);
  loadCommentSection(document.getElementById('comment-section'));
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

  let submit = document.getElementById("submit-button");
  submit.style.display = 'block';
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

// Set the correct values for both view and edit sections.
function constructBusinessProfile(id) {
  const profileInfo = document.getElementById('view-business-section');
  fetch('/business/' + id)
      .then(response => {
          if (!response.ok) {
            // Redirect to BusinessServlet, which displays appropriate error.
            window.location.href = '/business/' + id;
          }
          return response.json();
      }).then(info => {
        if (info.isCurrentUser) {
          document.getElementById('edit-button').style.display = 'block';
        } else {
          document.getElementById('edit-button').style.display = 'none';
        }

        ['name', 'location', 'story', 'bio', 'about', 'support'].forEach(property => {
          document.getElementById('business-' + property).innerText = info[property];
          document.getElementById('edit-' + property).value = info[property];
        })
      })
}

window.toggleProfile = function() {
  const viewProfile = document.getElementById('view-business-section');
  const editProfile = document.getElementById('edit-business-section');

  viewProfile.style.display = 'none';
  editProfile.style.display = 'block';
}
