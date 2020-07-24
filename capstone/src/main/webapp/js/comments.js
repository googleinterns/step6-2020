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

import { 
  buildElement,
  buildButton,
  getJsonObject, 
  } from '/js/util.js';


// The div in which comments are shown
let commentContainer = undefined;

// The property and corresponding value by which comments are filtered
let filterProperty = undefined;
let filterValue = undefined;

/** Build form for submitting comments. */
export function buildCommentForm(userIsLoggedIn, businessId, parentId=null) {
  const form = document.createElement('form');

  form.action = '/comment';
  form.method = 'post';

  const textField = buildCommentTextArea(userIsLoggedIn);

  form.appendChild(textField);

  if (userIsLoggedIn) {
    form.appendChild(buildFormSubmitButton());
  }

  form.appendChild(buildHiddenStaticFormField('parentId', parentId));
  form.appendChild(buildHiddenStaticFormField('businessId', businessId));

  return form;
}

function buildFormSubmitButton() {
  const button = document.createElement('input');

  button.type = 'submit';
  button.value = 'Submit';
  button.target = 'body';

  return button;
}

/** 
* Build a hidden form field that contains information that will be sent along in the post request 
*/
function buildHiddenStaticFormField(name, value) {
  const field = document.createElement('input');

  field.type = 'hidden';
  field.name = name;
  field.value = value;

  return field;
}

/** 
* Load comment section given the parentDiv in which to load the comments, the property to filter 
* comments by ('businessId' or 'userId') and the value to filter the comments by 
*/
export function loadCommentList(userIsLoggedIn, filterProperty_, filterValue_) {  
  commentContainer = document.createElement('div');
  commentContainer.id = 'comments';

  filterProperty = filterProperty_;
  filterValue = filterValue_;
  
  showComments(userIsLoggedIn);

  return commentContainer
}

/** Build text field in which to enter a commment. */
function buildCommentTextArea(userIsLoggedIn) {
  const commentTextArea = document.createElement('textarea');
  commentTextArea.cols = 70;
  commentTextArea.name = 'content';
  
  if (userIsLoggedIn) {
    commentTextArea.placeholder = 'Write a comment';
  } else {
    commentTextArea.placeholder = 'Please log in to write a comment';
  }

  commentTextArea.rows = 3;

  return commentTextArea;
}

/** Given a comment object build a comment element on the web page. */
async function buildCommentElement(comment) {
  const commentElement = document.createElement('div');
  
  commentElement.className = 'comment'
  commentElement.id = comment.id;
  commentElement.appendChild(buildElement('small', comment.name + ' says:'));
  commentElement.appendChild(document.createElement('br'));
  commentElement.innerHTML += comment.content + '\n';
  
  return commentElement;
}

/** Build a div containing a 'show replies' button that can be expanded to show all replies. */
function buildRepliesDiv(commentId) {
  const div = document.createElement('div');

  div.className = 'replies';
  div.innerHTML = '';
  div.appendChild(buildButton('show-replies-button', () => showReplies(commentId), 'Show replies'));

  return div;
}

function showReplyTextArea(parentId, businessId) {
  const replyToCommentDiv = 
      document.getElementById(parentId).querySelector('.reply-to-comment-div');

  replyToCommentDiv.innerHTML = '';
  replyToCommentDiv.appendChild(buildCommentForm(true, businessId, parentId));
}

/** 
* Build div for the field in which you can reply to a given comment. 
* If the user is not logged in the div will tell the user to log in to reply.
*/
function buildReplyToCommentDiv(parentId, businessId) {
  const div = document.createElement('div');

  div.className = 'reply-to-comment-div';
  
  div.appendChild(
    buildButton(
      'reply-to-comment-button', 
      () => showReplyTextArea(parentId, businessId), 
      'Reply',
    ));
  
  return div
}

async function buildTopLevelCommentElement(comment, userIsLoggedIn) {
  const commentElement = await buildCommentElement(comment);
  
  commentElement.appendChild(document.createElement('br'));
  if (userIsLoggedIn) {
    commentElement.appendChild(
          buildReplyToCommentDiv(comment.id, comment.businessId));
  }
  
  commentElement.appendChild(buildRepliesDiv(comment.id));

  return commentElement;
}

function showComments(userIsLoggedIn) {
  const params = {};
  params[filterProperty] = filterValue;

  getJsonObject('/comments', params)
      .then(
        comments => comments.forEach(
          comment => 
            buildTopLevelCommentElement(comment, userIsLoggedIn).then(commentElement =>
              commentContainer.appendChild(commentElement)
            )
        )
      );
}

async function showReplies(commentId) {
  const replyDiv = document.getElementById(commentId).querySelector('.replies');

  replyDiv.innerHTML = '';

  getJsonObject('/comments', {'parentId' : commentId})
      .then(replies => replies.forEach(reply =>
          buildCommentElement(reply).then(commentElement =>
            replyDiv.appendChild(commentElement))));
}
