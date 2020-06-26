function showMore() {
  let showElement = document.getElementsByClassName('show-more');
  let businessCard = document.getElementsByClassName('business-card');

  // For now, it is just doing this with the first element.
  // This will be handled better with unique IDs assigned via the servlets.
  showElement[0].style.display = 'none';
  businessCard[0].style.maxHeight = 'none';
}
