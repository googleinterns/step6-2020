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

import { setLoginOrLogoutUrl, setProfileUrl } from '/js/util.js';

window.addEventListener('DOMContentLoaded', (event) => {
  // Get login status of user to display on nav bar.
  setLoginOrLogoutUrl();
  
  // Update and set profile nav link.
  setProfileUrl();
})
