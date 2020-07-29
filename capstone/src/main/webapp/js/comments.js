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
export function loadUserPageCommentList(userId) {
  const commentContainer = document.createElement('div');

  getJsonObject('/comments', {'userId': userId})
      .then(comments => comments.forEach(comment => 
          commentContainer.appendChild(buildUserPageComment(comment))
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
function buildUserPageComment(comment) {
  const commentElement = buildCommentElement(comment);
  const commentBody = commentElement.querySelector('.card-body');

  getJsonObject('/business/' + comment.businessId).then(business => {
    const businessPageLink = 
        buildLinkElement('/business.html?id=' + comment.businessId, 'On ' + business.name /*As in on ...'s page*/); 
        
    // change text color to red
    businessPageLink.className = 'text-danger' 
    commentBody.prepend(businessPageLink);
  });

  return commentElement;
}

/** 
* Given a comment object build a comment element on the web page. 
* This function is meant for comments appearing on the page they where posted.
*/
function buildCommentElement(comment) {
  const commentElement = document.createElement('div');

  // Set to bootstrap card element and set margin-top to 2 via bootstrap
  commentElement.className = 'card mt-2'; 
  commentElement.id = comment.id;

  // Build the body of the commentElement
  const commentBody = document.createElement('div');
  commentBody.className = 'card-body'

  const headerElement = buildElement('h5', comment.name + '  ');
  headerElement.appendChild(buildElement('small', comment.timestampStr)); 

  commentBody.appendChild(headerElement);
  commentBody.appendChild(buildElement('p', comment.content));
  
  commentElement.appendChild(commentBody);

  return commentElement;
}


function buildTopLevelCommentElement(comment, userIsLoggedIn) {
  const commentElement = buildCommentElement(comment);
  const commentBody = commentElement.querySelector('.card-body');

  // Area that is initially empty to which reply form can be later added
  const replyFormDiv = document.createElement('div');

  if (userIsLoggedIn) {
    // Add a button that opens a reply form bellow the comment
    commentBody.appendChild(
        buildButton(
          'btn btn-danger btn-space', 
          () => 
              replyFormDiv.appendChild(
                  buildCommentForm(true, comment.businessId, comment.id)), 
          'Reply',
        ));
  }

  if (comment.hasReplies) {
    // Add a button that shows replies bellow the comment
    commentBody.appendChild(
        buildButton(
          'show-replies-button btn btn-danger btn-space', 
          () => showReplies(comment.id), 
          'Show replies',
        ));
  }
  commentElement.appendChild(commentBody);

  commentElement.appendChild(replyFormDiv);

  return commentElement;
}

/** Show replies to a specific comment and display it below the comment */
async function showReplies(commentId) {
  const parentComment = document.getElementById(commentId);

  const replyDiv = document.createElement('div');

  getJsonObject('/comments', {'parentId' : commentId})
      .then(replies => replies.forEach(reply =>
          replyDiv.appendChild(buildCommentElement(reply))
      ));

  parentComment.appendChild(replyDiv);
}
