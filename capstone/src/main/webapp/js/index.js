import { buildElement } from '/js/util.js';

window.onload = function() {
  const businesses = document.getElementById('businesses');
  for (let i = 0; i < 2; i++) {
    // Create new business card for this business.
    let businessCard = document.createElement('div');
    businessCard.classList.add('business-card');

    businessCard.appendChild(createBusinessLink(i, 'business.html'));
    businessCard.appendChild(createBusinessInfo(i));
    businessCard.appendChild(createShowMoreButton(i));

    businesses.appendChild(businessCard);
  }
}

function createBusinessLink(index, href) {
  // Creates a business card header that links to the respective business page.
  let businessLink = buildElement('a', '');
  businessLink.href = href;
  businessLink.appendChild(buildElement('h2', getBusinessNames()[index]));
  return businessLink;
}

function createBusinessInfo(index) {
  // Creates a description to the business.
  let businessInfo = buildElement('p', getBusinessDescriptions()[index]);
  businessInfo.classList.add('business-info');
  return businessInfo;
}

function createShowMoreButton(index) {
  // Creates the show more button at the bottom of the business card.
  let showMore = buildElement('p', '');
  let showMoreSpan = buildElement('span', 'Show More');
  showMore.appendChild(showMoreSpan);
  showMore.classList.add('show-more');
  showMore.id = 'business' + index;
  showMore.onclick = function() { showMoreInfo(showMore.id); };
  return showMore;
}

function showMoreInfo(id) {
  const showElement = document.getElementById(id);
  const businessCard = document.getElementsByClassName('business-card');

  // For now, it is just doing this with the first element.
  // This will be handled better with unique IDs assigned via the servlets.
  showElement.style.display = 'none';

  // id.slice(-1) gets the index of the business as specified by the id
  businessCard[id.slice(-1)].style.maxHeight = 'none';
}

function getBusinessNames() {
  // temporary function that will be replaced by fetches to datastore.
  return ['La Villa Pizzeria', 'La Casa Pizzeria'];
}

function getBusinessDescriptions() {
  // temporary function that will be replaced by fetches to datastore.
  return [`In order to tell the story of how La Villa came to be in 1982 properly, 
          we must go back three decades before. In 1955 my Uncle, Gino Branchinelli 
          opened the family's first Pizzeria on 5th avenue and Bay ridge. He along 
          with my Other Uncle, Antonio opened more locations around Brooklyn and in 
          1960 opened Gino's Italian Ices on 39th street as well.  In the late 60's 
          My Father Benito joined my uncle at his current location in Long Beach, 
          Long Island which opened in 1962.  After 14 years there, my Father decided 
          to leave Long Beach. In the spring of 1982, along with other family members, 
          he opened the La Villa Located in the Lindenwood section of Howard Beach.  
          The Pizzeria was small with seating for only about 25-30 people, 
          but it was always standing room only!`,
          `In order to tell the story of how La Casa came to be in 1982 properly, 
          we must go back three decades before. In 1955 my Uncle, Gino Branchinelli 
          opened the family's first Pizzeria on 5th avenue and Bay ridge. He along 
          with my Father, Antonio opened more locations around Brooklyn and in 
          1960 opened Gino's Italian Ices on 39th street as well.  In the late 60's 
          My Other Uncle Benito joined my uncle at his current location in Long Beach, 
          Long Island which opened in 1962.  After 14 years there, my Uncle decided 
          to leave Long Beach. In the spring of 1982, along with other family members, 
          he opened the La Casa Located in the Lindenwood section of Howard Beach.  
          The Pizzeria was small with seating for only about 25-30 people, 
          but it was always standing room only! La Casa > La Villa`];
}
