import { loadCommentSection } from '/js/comments.js'

window.onload = () => loadCommentSection(document.getElementById('comment-section'));

// Toggle between view and edit profile options.
function toggleProfile() {
  var viewProfile = document.getElementById("view-profile-section");
  var editProfile = document.getElementById("edit-profile-section");

  if (viewProfile.style.display == "block") {
    viewProfile.style.display = "none";
    editProfile.style.display = "block";
  } else {
    editProfile.style.display = "none";
    viewProfile.style.display = "block";
  }
}
