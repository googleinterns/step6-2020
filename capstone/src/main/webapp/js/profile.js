// Toggle between view and edit profile options.
function showProfile() {
  var viewProfile = document.getElementById("view-profile-section");
  var editProfile = document.getElementById("edit-profile-section");

  if (editProfile.style.display = "block") {
    editProfile.style.display = "none";
    viewProfile.style.display = "block";
  } else {
    viewProfile.style.display = "none";
    editProfile.style.display = "block";
  }
}
