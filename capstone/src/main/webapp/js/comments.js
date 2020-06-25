// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

function init() {
  commentField = document.getElementById("comment-field");
  commentContainer = document.getElementById("comments");
  
  // determines if the comments are currently being displayed
  commentsShown = false;
}

function getComments() {
  // TODO (bergmoney@): make get request to comments servlet
  comments = [
    {userId: 1, content: 'Hii', timestamp: 1},
    {userId: 2, content: 'I am sorry about what happened to your business', timestamp: 2}
  ]

  return wrapInPromise(comments);
}

function postComment(content, userId) {
  //TODO (bergmoney@): Post comment to servlet
  console.log("Post comment \"" + content + "\" by user " + userId);
}

function getUserId() {
  // TODO [begmoney@]: Figure out a way to get user's id or prompt sign in if user isn't logged in
  return 1;
}

function getUserName(userId) {
  // TODO (bergmoney@): Request username from API
  let users = ["lukas", "winnie", "eashan", "ben", "alyssa"];

  return wrapInPromise(users[userId]);
}

/** Wrap a value in a promise to simulate a server request. */
function wrapInPromise(val) {
  return new Promise((resolve, reject) => resolve(val));
}

/** Build html element of specified type and content */
function buildElement(type, content) {
  let element = document.createElement(type, content);
  element.innerText = content;

  return element;
}

/** If comments are being shown, populate them with the most up to date values. */
function refreshComments() {
  if (commentsShown) {
    commentContainer.innerHTML = '';
    getComments().then(
      comments => comments.forEach(
        comment => commentContainer.appendChild(buildCommentElement(comment))
      )
    );
  }
}

function addUserComment() {
  postComment(commentField.value, getUserId());

  commentField.value = "";
  refreshComments();
}

function buildCommentElement(comment) {
  let commentElement = document.createElement('div');
  
  commentElement.className = 'comment'
  commentElement.id = comment.id;
  commentElement.innerHTML = '';
  commentElement.appendChild(buildElement('p', comment.content));
  
  getUserName(comment.userId).then(
    userName =>  commentElement.appendChild(buildElement("small", userName))
  );

  return commentElement;
}

function showComments() {
  document.getElementById("show-comments-button").remove();
  commentsShown = true;
  refreshComments();
}
