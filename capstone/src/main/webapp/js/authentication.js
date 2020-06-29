/** Update log-in status of users in nav bar. */
export function getLoginStatus() {
  fetch('/auth').then(response => response.json()).then((user) => {
    const loginElement = document.getElementById('auth-button');
    loginElement.appendChild(createUrlElement(user.url, user.isLoggedin));
  });
}

/** Helper function for creating a link element. */
export function createUrlElement(url, isLoggedin) {    
  const aElement = document.createElement('a');
  aElement.setAttribute('href', url);
  if (isLoggedin) {
    aElement.innerText = "Logout";
  } else {
    aElement.innerText = "Login";
  }
  return aElement;
}
