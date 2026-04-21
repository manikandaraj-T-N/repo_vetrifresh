// blog.js

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

// ── Tags ──────────────────────────────────────────────────────
function filterTag(tag) {
  document.querySelectorAll('.tag').forEach(el => el.classList.remove('active'));
  event.target.classList.add('active');
  const url = new URL(window.location.href);
  url.searchParams.set('tag', tag);
  window.location.href = url.toString();
}

// AFTER — keeps all existing params
function applySort(value) {
  const url = new URL(window.location.href);
  url.searchParams.set('sort', value);
  window.location.href = url.toString();
}