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

function millisElapsedSince(ts) {
  let current = new Date();
  return current.getTime() - ts;
}

/** Perform integer division, rounding the floating point result down. */
export function div(a, b) {
  return Math.floor(a / b);
}

function getMonthDiff(now, then) {
  let yearDiff = now.getFullYear() - then.getFullYear();
  
  // Could be negative (e.g. when comparing January 2020 with December 2019)
  let monthDiff = now.getMonth() - then.getMonth();

  return yearDiff * 12 + monthDiff;
}

/** 
* Format timestamp by showing the minutes/hours/days/weeks/months/years passed since date */
export function showTimeElapsedSince(then) {
  var moment = require('moment');
  let thenMoment = moment(then);
  let nowMoment = moment(now);

  return nowMoment.diff(thenMoment, 'days');
}