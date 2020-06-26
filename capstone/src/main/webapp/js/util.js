// Copyright 2019 Google LLC
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
