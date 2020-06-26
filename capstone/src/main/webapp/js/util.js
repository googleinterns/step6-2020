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
  let now = new Date();

  let milliSecondDiff = now - then;
  let secondDiff = div(milliSecondDiff, 1000);

  let minuteDiff = div(secondDiff, 60);
  if (minuteDiff < 1) {
    return secondDiff + 's'
  }

  let hourDiff = div(minuteDiff, 60);
  if (hourDiff < 1) {
    return minuteDiff + ' m';
  }

  let dayDiff = div(hourDiff, 24);
  if (dayDiff < 1) {
    return hourDiff + 'h';
  }

  let weekDiff = div(dayDiff, 7);
  if (weekDiff < 1) {
    return dayDiff + 'd';
  }

  let monthDiff = getMonthDiff(now, then);
  if (monthDiff < 1) {
    return weekDiff + 'wk';
  }

  let yearDiff = div(monthDiff, 12);
  if (yearDiff < 1) {
    return monthDiff + 'mo';
  }

  return yearDiff + 'yr';
}