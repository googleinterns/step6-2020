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

commentField = document.getElementById("comment-field");
showCommentsButton = document.getElementById("show-comments-button");

// determines if the comments are currently being displayed
commentsShown = false;

function buildElement(type, content) {
  element = document.createElement(type, content);
  element.innerText = content;

  return element;
}

function getUserId() {
  // TODO [begmoney@]: Figure out a way to get user's id or prompt sign in if user isn't logged in
  return 1;
}

function getUserName(userId) {
  // TODO (bergmoney@): Request username from API
  let users = ["lukas", "winnie", "eashan", "ben", "alyssa"];

  return users[userId];
}

function clearCommentField() {
  commentField.value = "";  
}

function refreshComments() {
  if (commentsShown) {
    getComments.then(
      comments => comments.forEach(showComment)
    );
  }
}

function addComment() {
  postComment(commentField.value, getUserId());

  clearCommentField();
  refreshComments();
}

/** Creates a comment element containing text. */
function showComment(comment) {
  let commentElement = document.createElement('div');
  
  commentElement.className = 'comment'
  commentElement.id = comment.id;
  commentElement.innerHTML = '';
  commentElement.appendChild(buildElement('p', comment.content));
  commentElement.appendChild(buildElement("small", getUserName(comment.userId)));
  commentElement.appendChild(createDeleteButton(comment.id));

  return commentElement;
}

function showComments() {
  showCommentsButton.remove();
  commentsShown = true;
  refreshComments();
}