window.apiFetch = async function apiFetch(url, options = {}) {
  const opts = { method: 'GET', headers: {}, ...options };
  // Default accept JSON; do not force Content-Type unless sending JSON body
  const isFormData = (opts.body && typeof FormData !== 'undefined' && opts.body instanceof FormData);
  const isJsonBody = (opts.body && typeof opts.body === 'string');
  opts.headers = { 'Accept': 'application/json', ...(isFormData ? {} : (isJsonBody ? { 'Content-Type': 'application/json' } : {})), ...(options.headers || {}) };

  // Attach JWT if present
  try {
    const token = localStorage.getItem('jwt');
    if (token) {
      opts.headers['Authorization'] = 'Bearer ' + token;
    }
  } catch (_) { /* ignore */ }

  return fetch(url, opts);
};
