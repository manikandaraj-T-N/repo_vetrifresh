// // shop.js

// // Toggle sidebar sections
// function toggleFilter(id) {
//   const el = document.getElementById(id);
//   el.classList.toggle('collapsed');
// }

// // Build URL with current params + new one
// function buildUrl(newParams) {
//   const url = new URL(window.location.href);
//   Object.entries(newParams).forEach(([k, v]) => {
//     if (v) url.searchParams.set(k, v);
//     else url.searchParams.delete(k);
//   });
//   return url.toString();
// }

// function applySort(value) {
//   window.location.href = buildUrl({ sort: value });
// }

// function updatePrice(value) {
//   document.getElementById('priceDisplay').textContent = '₹' + value;
// }

// function applyPriceFilter() {
//   const val = document.getElementById('maxPrice').value;
//   window.location.href = buildUrl({ maxPrice: val });
// }

// function applyRatingFilter(value) {
//   window.location.href = buildUrl({ rating: value });
// }

// function filterTag(tag) {
//   // toggle active class visually
//   document.querySelectorAll('.tag').forEach(el => el.classList.remove('active'));
//   event.target.classList.add('active');

//   window.location.href = buildUrl({ tag: tag });
// }

// function addToCart(productId) {
//   fetch('/cart/add', {
//     method: 'POST',
//     headers: { 'Content-Type': 'application/json' },
//     body: JSON.stringify({ productId, quantity: 1 })
//   }).then(() => {
//     // update cart count badge
//     const badge = document.querySelector('.cart-count');
//     if (badge) badge.textContent = parseInt(badge.textContent || 0) + 1;
//   });
// }

// function toggleWishlist(productId) {
//   fetch('/wishlist/toggle/' + productId, { method: 'POST' });
// }

// function quickView(productId) {
//   window.location.href = '/product/' + productId;
// }


// shop.js

// shop.js

// ── Toggle sidebar filter sections ──────────────────────────
function toggleFilter(id) {
  const body = document.getElementById(id);
  if (!body) return;

  const title = body.previousElementSibling;
  const icon  = title ? title.querySelector('i[class*="fa-chevron"]') : null;
  const isOpen = body.classList.contains('open');

  if (isOpen) {
    body.classList.remove('open');
    if (icon) {
      icon.classList.remove('fa-chevron-up');
      icon.classList.add('fa-chevron-down');
    }
  } else {
    body.classList.add('open');
    if (icon) {
      icon.classList.remove('fa-chevron-down');
      icon.classList.add('fa-chevron-up');
    }
  }
}

// ── Build URL preserving existing params ─────────────────────
function buildUrl(newParams) {
  const url = new URL(window.location.href);
  Object.entries(newParams).forEach(([k, v]) => {
    if (v) url.searchParams.set(k, v);
    else url.searchParams.delete(k);
  });
  return url.toString();
}

// ── Sort ──────────────────────────────────────────────────────
function applySort(value) {
  window.location.href = buildUrl({ sort: value });
}

// ── Price ─────────────────────────────────────────────────────
function updatePrice(value) {
  document.getElementById('priceDisplay').textContent = '₹' + value;
}

function applyPriceFilter() {
  const val = document.getElementById('maxPrice').value;
  window.location.href = buildUrl({ maxPrice: val });
}

// ── Rating ────────────────────────────────────────────────────
function applyRatingFilter(value) {
  window.location.href = buildUrl({ rating: value });
}

// ── Tags ──────────────────────────────────────────────────────
function filterTag(tag) {
  document.querySelectorAll('.tag').forEach(el => el.classList.remove('active'));
  event.target.classList.add('active');
  window.location.href = buildUrl({ tag: tag });
}

// ── Cart ──────────────────────────────────────────────────────
function addToCart(productId) {
  fetch('/cart/add', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ productId, quantity: 1 })
  }).then(res => {
    if (res.ok) {
      const badge = document.querySelector('.cart-count');
      if (badge) badge.textContent = parseInt(badge.textContent || 0) + 1;
    }
  });
}

// ── Wishlist ──────────────────────────────────────────────────
function toggleWishlist(productId) {
  fetch('/wishlist/toggle/' + productId, { method: 'POST' });
}

// ── Quick View ────────────────────────────────────────────────
function quickView(productId) {
  window.location.href = '/product/' + productId;
}