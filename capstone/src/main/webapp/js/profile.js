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

window.onload = () => loadCommentSection(document.getElementById('comment-section'));

// Toggle between view and edit profile options.
function toggleProfile() {
  var viewProfile = document.getElementById('view-profile-section');
  var editProfile = document.getElementById('edit-profile-section');

  if (viewProfile.style.display == 'block') {
    viewProfile.style.display = 'none';
    editProfile.style.display = 'block';
  } else {
    editProfile.style.display = 'none';
    viewProfile.style.display = 'block';
  }
}
