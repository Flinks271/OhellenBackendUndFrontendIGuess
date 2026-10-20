const CSRF_COOKIE = 'XSRF-TOKEN'
const CSRF_HEADER = 'X-XSRF-TOKEN'
const UNSAFE_METHODS = new Set(['POST', 'PUT', 'PATCH', 'DELETE'])

function getCookie(name) {
  const prefix = `${encodeURIComponent(name)}=`
  const cookie = document.cookie
    .split('; ')
    .find((entry) => entry.startsWith(prefix))

  return cookie ? decodeURIComponent(cookie.slice(prefix.length)) : ''
}

async function initializeCsrfCookie(apiBase) {
  await fetch(`${apiBase}/auth/csrf`, {
    credentials: 'include',
  })

  return getCookie(CSRF_COOKIE)
}

export async function fetchWithCsrf(apiBase, url, options = {}) {
  const method = (options.method || 'GET').toUpperCase()
  const isUnsafe = UNSAFE_METHODS.has(method)
  const headers = new Headers(options.headers)
  let csrfToken = isUnsafe ? await initializeCsrfCookie(apiBase) : ''

  if (csrfToken) {
    headers.set(CSRF_HEADER, csrfToken)
  }

  let response = await fetch(url, {
    ...options,
    credentials: 'include',
    headers,
  })

  // The current backend creates the CSRF cookie when the first unsafe request
  // is rejected. Retry that request once with the newly issued cookie.
  if (isUnsafe && response.status === 403 && !csrfToken) {
    csrfToken = getCookie(CSRF_COOKIE)

    if (csrfToken) {
      headers.set(CSRF_HEADER, csrfToken)
      response = await fetch(url, {
        ...options,
        credentials: 'include',
        headers,
      })
    }
  }

  return response
}
