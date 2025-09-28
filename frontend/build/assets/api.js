window.apiFetch = async function apiFetch(url, options = {}) {
  const opts = { method: 'GET', headers: {}, ...options };
  opts.headers = { 'Accept': 'application/json', 'Content-Type': 'application/json', ...(options.headers || {}) };

  // Attach JWT if present
  try {
    const token = localStorage.getItem('jwt');
    if (token) {
      opts.headers['Authorization'] = 'Bearer ' + token;
    }
  } catch (_) { /* ignore */ }

  return fetch(url, opts);
};
