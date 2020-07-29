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
  const commentContainer = document.createElement('div');
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
  const commentContainer = document.createElement('div');
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

  commentTextArea.class = "container-fluid";
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

function buildCommentElement(comment) {
  const commentElement = document.createElement('div');

  commentElement.className = 'card';
  commentElement.appendChild(buildCommentBody(comment));

  return commentElement;
}

/** 
* Given a comment object build a comment element on the web page. 
* This function is meant for comments appearing on the page they where posted.
*/
function buildCommentBody(comment) {
  const commentBody = document.createElement('div');
  commentBody.className = 'card-body'

  const headerElement = buildElement('h5', comment.name + '  ');
  headerElement.appendChild(buildElement('small', comment.timestampStr)); 

  commentBody.appendChild(headerElement);
  commentBody.appendChild(buildElement('p', comment.content));
  
  return commentBody;
}

/** Build a div containing a 'show replies' button that can be expanded to show all replies. */
function buildRepliesDiv(commentId) {
  const div = document.createElement('div');

  div.className = 'replies';
  div.innerHTML = '';
  div.appendChild(buildButton('show-replies-button btn btn-danger', () => showReplies(commentId), 'Show replies', 'btn btn-danger'));

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
      'reply-to-comment-button btn btn-danger', 
      () => showReplyTextArea(parentId, businessId), 
      'Reply',
    ));
  
  return div
}

function buildTopLevelCommentElement(comment, userIsLoggedIn) {
  const commentElement = document.createElement('div');

  commentElement.className = 'card';
  
  const commentBody = buildCommentBody(comment);
  commentBody.appendChild(document.createElement('br'));
  if (userIsLoggedIn) {
    commentBody.appendChild(
          buildReplyToCommentDiv(comment.id, comment.businessId));
  }
  if (comment.hasReplies) {
    commentBody.appendChild(buildRepliesDiv(comment.id));
  }

  commentElement.appendChild(commentBody);

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
