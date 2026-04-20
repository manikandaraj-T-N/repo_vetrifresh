// shop.js

// Toggle sidebar sections
function toggleFilter(id) {
  const el = document.getElementById(id);
  el.classList.toggle('collapsed');
}

// Build URL with current params + new one
function buildUrl(newParams) {
  const url = new URL(window.location.href);
  Object.entries(newParams).forEach(([k, v]) => {
    if (v) url.searchParams.set(k, v);
    else url.searchParams.delete(k);
  });
  return url.toString();
}

function applySort(value) {
  window.location.href = buildUrl({ sort: value });
}

function updatePrice(value) {
  document.getElementById('priceDisplay').textContent = '₹' + value;
}

function applyPriceFilter() {
  const val = document.getElementById('maxPrice').value;
  window.location.href = buildUrl({ maxPrice: val });
}

function applyRatingFilter(value) {
  window.location.href = buildUrl({ rating: value });
}

function filterTag(tag) {
  // toggle active class visually
  document.querySelectorAll('.tag').forEach(el => el.classList.remove('active'));
  event.target.classList.add('active');

  window.location.href = buildUrl({ tag: tag });
}

function addToCart(productId) {
  fetch('/cart/add', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ productId, quantity: 1 })
  }).then(() => {
    // update cart count badge
    const badge = document.querySelector('.cart-count');
    if (badge) badge.textContent = parseInt(badge.textContent || 0) + 1;
  });
}

function toggleWishlist(productId) {
  fetch('/wishlist/toggle/' + productId, { method: 'POST' });
}

function quickView(productId) {
  window.location.href = '/product/' + productId;
}