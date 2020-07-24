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
  buildLinkElement,
  getJsonObject, 
  } from '/js/util.js';

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

/** Load a list of comments that the user posted */
export function loadUserCommentList(userId) {
  commentContainer = document.createElement('div');
  commentContainer.id = 'comments';

  getJsonObject('/comments', {'userId': userId})
      .then(comments => comments.forEach(comment => 
          commentContainer.appendChild(buildUserPageCommentWrapper(comment))
      ));

  return commentContainer;
}

/** 
* Load comment section given the parentDiv in which to load the comments, the property to filter 
* comments by ('businessId' or 'userId') and the value to filter the comments by 
*/
export function loadCommentList(userIsLoggedIn, businessId) {
  commentContainer = document.createElement('div');
  commentContainer.id = 'comments';
  
  getJsonObject('/comments', {'businessId': businessId})
      .then(comments => comments.forEach(comment => 
          commentContainer.appendChild(buildTopLevelCommentElement(comment, userIsLoggedIn))
      ));

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

/** 
* Given a comment object build a comment element on the web page. 
* This function is meant for comments appearing on a list of comments the user posted.
*/
function buildUserPageCommentWrapper(comment) {
  const commentWrapper = document.createElement('div');

  commentWrapper.class = 'comment-wrapper';

  getJsonObject('/business/' + comment.businessId).then(business => {
    commentWrapper.appendChild(
        buildLinkElement('/business.html?id=' + comment.businessId, business.name));
    commentWrapper.appendChild(buildCommentElement(comment));
  });

  return commentWrapper;
}

/** 
* Given a comment object build a comment element on the web page. 
* This function is meant for comments appearing on the page they where posted.
*/
function buildCommentElement(comment) {
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
function buildReplyToCommentDiv(parentId, userIsLoggedIn, businessId) {
  const div = document.createElement('div');

  div.className = 'reply-to-comment-div';
  
  div.innerHTML = '';

  if (userIsLoggedIn) {
    div.appendChild(
      buildButton(
        'reply-to-comment-button', 
        () => showReplyTextArea(parentId, businessId), 
        'Reply',
      ));
  } else {
    div.appendChild(
      buildButton('reply-to-comment-button', null, 'Please log in to write a comment'));
  }
  
  return div
}

function buildTopLevelCommentElement(comment, userIsLoggedIn) {
  const commentElement = buildCommentElement(comment);
  
  commentElement.appendChild(document.createElement('br'));
  commentElement.appendChild(
      buildReplyToCommentDiv(comment.id, userIsLoggedIn, comment.businessId));
  commentElement.appendChild(buildRepliesDiv(comment.id));
  

  return commentElement;
}

/** Show replies to a specific comment and display it below the comment */
async function showReplies(commentId) {
  const replyDiv = document.getElementById(commentId).querySelector('.replies');

  replyDiv.innerHTML = '';

  getJsonObject('/comments', {'parentId' : commentId})
      .then(replies => replies.forEach(reply =>
          replyDiv.appendChild(buildCommentElement(reply))
      ));
}
