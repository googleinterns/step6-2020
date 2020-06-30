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

import { wrapInPromise, buildElement, buildButton } from '/js/util.js';

var COMMENT_FIELD = undefined;
var COMMENT_CONTAINER = undefined;

window.onload = () =>{
  COMMENT_FIELD = document.getElementById('comment-field');
  COMMENT_CONTAINER = document.getElementById('comments');
  
  showComments();
}

function getComments() {
  // TODO (bergmoney@): make get request to comments servlet
  let translatedLoremIpsum = "But I must explain to you how all this mistaken idea of denouncing pleasure and praising pain was born and I will give you a complete account of the system, and expound the actual teachings of the great explorer of the truth, the master-builder of human happiness. No one rejects, dislikes, or avoids pleasure itself, because it is pleasure, but because those who do not know how to pursue pleasure rationally encounter consequences that are extremely painful. Nor again is there anyone who loves or pursues or desires to obtain pain of itself, because it is pain, but because occasionally circumstances occur in which toil and pain can procure him some great pleasure. To take a trivial example, which of us ever undertakes laborious physical exercise, except to obtain some advantage from it? But who has any right to find fault with a man who chooses to enjoy a pleasure that has no annoying consequences, or one who avoids a pain that produces no resultant pleasure?"

  let originalLoremIpsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."


  let comments = [
    {id: 1, userId: 1, content: 'Hii', 
        timestamp: Date.parse('01 Jan 1970 00:00:00 GMT'), timeElapsedStr: '20yr'},
    {id: 2, userId: 2, content: 'I am sorry about what happened to your business', 
        timestamp: Date.parse('30 Jun 2019 00:00:00 GMT'), timeElapsedStr: '5mo'},
    {id: 3, userId: 2, content: translatedLoremIpsum,
        timestamp: Date.parse('01 Jan 2020 00:00:00 GMT'), timeElapsedStr: '10wk'},
    {id: 4, userId: 3, content: originalLoremIpsum,
        timestamp: Date.parse('01 Jun 2020 00:00:00 GMT'), timeElapsedStr: '1wk'},
    {id: 5, userId: 0, 
        content: 'I love pizza. This should never happen to a pizza joint. Sending my love',
        timestamp: Date.parse('24 Jun 2020 00:00:00 GMT'), timeElapsedStr: '1d'},
    {id: 6, userId: 0, 
        content: 'I love pizza. This should never happen to a pizza joint. Sending my love',
        timestamp: Date.parse('26 Jun 2020 10:00:00 GMT'), timeElapsedStr: '10h'},
    {id: 7, userId: 0, 
        content: 'I love pizza. This should never happen to a pizza joint. Sending my love',
        timestamp: Date.parse('26 Jun 2020 16:00:00 GMT'), timeElapsedStr: '4h'}
  ];

  return wrapInPromise(comments);
}

function postComment(content, userId, parentId = null) {
  //TODO (bergmoney@): Post comment to servlet
  console.log('Post comment \'' + content + '\' by user ' + userId + ' with parent ' + parentId);
}

function getUserId() {
  // TODO [begmoney@]: Figure out a way to get user's id or prompt sign in if user isn't logged in
  return 1;
}

function getUserName(userId) {
  // TODO (bergmoney@): Request username from API
  let users = ['lukas', 'winnie', 'eashan', 'ben', 'alyssa'];

  return wrapInPromise(users[userId]);
}

function getReplies(commentId) {
  // TODO (bergmoney@): Request replies from API
  let hash = commentId + 10;
  let replies = [
    {id: hash, userId: 0, content: 'Hii', timestamp: 1},
    {id: hash, userId: 1, content: 'I am sorry about what happened to your business', timestamp: 2}
  ]
  return wrapInPromise(replies);
}

function addUserComment(textArea, parentId = null) {
  postComment(textArea.value, getUserId(), parentId);

  textArea.value = '';
  COMMENT_CONTAINER.innerHTML = '';
  showComments();
}

async function buildCommentElement(comment) {
  let commentElement = document.createElement('div');
  
  commentElement.className = 'comment'
  commentElement.id = comment.id;
  commentElement.innerHTML = '';
  commentElement.appendChild(buildElement('small', comment.timeElapsedStr));
  commentElement.appendChild(buildElement('p', comment.content));
  
  let userName = await getUserName(comment.userId);
  commentElement.appendChild(buildElement('small', userName));
  
  return commentElement;
}

function buildShowRepliesElement(commentId) {
  let button = document.createElement('button');

  button.className = 'show-replies-button';
  button.addEventListener('click', () => showReplies(commentId));
  button.innerText = 'Show replies';

  return button;
}

function buildRepliesDiv(commentId) {
  let div = document.createElement('div');

  div.className = 'replies';
  div.innerHTML = '';
  div.appendChild(buildButton('show-replies-button', () => showReplies(commentId), 'Show replies'));

  return div;
}

function showReplyToCommentField(parentId) {
  let replyToCommentDiv = document.getElementById(parentId).querySelector('.reply-to-comment-div');

  replyToCommentDiv.innerHTML = '';

  let textArea = document.createElement('textArea');
  textArea.cols = 70;
  textArea.placeholder = 'Write a comment';
  textArea.rows = 2;

  replyToCommentDiv.appendChild(textArea);
  
  replyToCommentDiv.appendChild( 
    buildButton(
      'submit-reply-button', 
      () => addUserComment(textArea, parentId),
      'Post'
    )
  );
}

function buildReplyToCommentDiv(parentId) {
  let div = document.createElement('div');

  div.className = 'reply-to-comment-div';
  div.innerHTML = '';
  div.appendChild(
      buildButton('reply-to-comment-button', () => showReplyToCommentField(parentId), 'Reply'));

  return div
}

async function buildTopLevelCommentElement(comment) {
  let commentElement = await buildCommentElement(comment);
  
  commentElement.appendChild(document.createElement('br'));
  commentElement.appendChild(buildReplyToCommentDiv(comment.id));
  commentElement.appendChild(buildRepliesDiv(comment.id));
  

  return commentElement;
}

function showComments() {
  getComments().then(
    comments => comments.forEach(
      comment => 
        buildTopLevelCommentElement(comment).then(commentElement =>
          COMMENT_CONTAINER.appendChild(commentElement)
        )
    )
  );
}

async function showReplies(commentId) {
  let replyDiv = document.getElementById(commentId).querySelector('.replies');

  replyDiv.innerHTML = '';

  let replies = await getReplies(commentId);
  replies.forEach(reply =>
    buildCommentElement(reply).then(commentElement =>
      replyDiv.appendChild(commentElement)
    )
  );
}
