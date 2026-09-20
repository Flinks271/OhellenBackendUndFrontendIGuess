# Security and Authentication Documentation

This document explains the current security model used by the web application, including authentication, token handling, CSRF protection, authorization rules, and the expected frontend behavior.

## 1. Overall security model

The application uses a hybrid authentication model:

- Short-lived JWT access tokens for API authorization
- Server-side refresh tokens stored in the database
- Browser cookies for the refresh token
- CSRF protection for browser-based state-changing requests
- BCrypt password hashing for user credentials

This is a good fit for a browser-based app where the frontend is a separate client and the backend exposes REST endpoints.

## 2. Authentication flow

### Login

The login process is handled in `AuthorizationController`.

Flow:

1. The frontend sends email + password to `POST /auth/login`
2. Spring Security authenticates the user with `AuthenticationManager`
3. The backend generates a short-lived JWT access token
4. The backend creates a refresh session in the database
5. The backend sends the refresh token as an `HttpOnly` cookie
6. The backend returns the access token in the JSON response

Example response shape:

```json
{
  "accessToken": "eyJ...",
  "refreshToken": "server-generated-refresh-token"
}
```

Important:

- The access token is used for normal API requests
- The refresh token is stored in the database and sent via cookie
- The refresh token is not exposed to JavaScript because it is `HttpOnly`

### Access token

The access token is created with `JwtUtil`.

Current behavior:

- generated with JWT
- short lifetime: 15 minutes
- contains the user subject and role claims
- signed with the configured secret key
- used in the `Authorization: Bearer <token>` header

### Refresh token

The refresh token is not just a JWT string floating around in the frontend. Instead:

- the backend creates a refresh session for the user
- the refresh token is persisted in a database table
- the token is rotated on refresh
- revoked on logout

This is more secure than a purely stateless JWT-only refresh model because the server can invalidate tokens immediately and track active sessions.

## 3. Authorization and roles

Roles are loaded from the database and mapped to Spring authorities.

Examples in the app:

- `ROLE_ADMIN`
- user roles and permissions are used in `@PreAuthorize`

Example:

```java
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@PostMapping("/assign-role")
public ResponseEntity<User> assignRole(...) {
    ...
}
```

Protected endpoints require the caller to be authenticated and, when needed, authorized for a certain role.

## 4. CSRF protection

The backend enables CSRF protection with Spring Security.

This is important because the app uses cookies for authentication-related state. Browsers automatically send cookies, so a malicious site could trigger unwanted state-changing requests unless CSRF protection is active.

Current configuration:

- `CookieCsrfTokenRepository.withHttpOnlyFalse()`
- cookie name: `XSRF-TOKEN`
- header name: `X-XSRF-TOKEN`

This means the browser gets a CSRF token as a cookie and the frontend must send that token in the header for state-changing requests.

### State-changing requests

The following should always include the CSRF header:

- `POST`
- `PUT`
- `PATCH`
- `DELETE`

### CSRF bootstrap endpoint

The app exposes:

- `GET /auth/csrf`

This is used so the browser can obtain the CSRF cookie before making a login or other protected request.

## 5. Cookie-based refresh flow

The refresh token is stored in a cookie with the following properties:

- cookie name: `refreshToken`
- `httpOnly: true`
- path: `/`
- max age: 7 days during local usage
- `SameSite: Lax`
- `secure: false` in local development

This prevents JavaScript access to the refresh token while still allowing the browser to send it back automatically with credentialed requests.

### Refresh flow

1. Browser calls `POST /auth/refresh` with cookies enabled
2. Spring reads `refreshToken` from the cookie
3. The server validates the refresh session in the database
4. If valid, the server creates a new access token
5. The server rotates the refresh token and replaces the cookie
6. The new token pair is returned to the client

### Logout flow

1. Browser sends logout request
2. The backend reads the refresh token from the cookie
3. The server invalidates the refresh session
4. The backend clears the cookie by setting an empty value and age `0`

## 6. Security configuration overview

Relevant security settings in the app:

- `PasswordEncoder` is `BCryptPasswordEncoder`
- `AuthenticationManager` is enabled through Spring configuration
- CSRF is enabled, not disabled
- JWT filter is added before the standard username/password authentication filter
- public auth endpoints are allowed without login:
  - `/auth/csrf`
  - `/auth/login`
  - `/auth/register`
  - `/auth/refresh`
  - `/auth/logout`
- all other endpoints require authentication

## 7. Frontend usage rules

For browser-based frontend calls, always do the following:

### Required for all authenticated, credentialed requests

- include cookies: `credentials: "include"` or `withCredentials: true`
- send `X-XSRF-TOKEN` for state-changing requests

### Example with fetch

```js
await fetch("http://localhost:8080/auth/csrf", {
  method: "GET",
  credentials: "include",
});

const csrfToken = document.cookie
  .split("; ")
  .find((row) => row.startsWith("XSRF-TOKEN="))
  ?.split("=")[1];

const response = await fetch("http://localhost:8080/auth/login", {
  method: "POST",
  credentials: "include",
  headers: {
    "Content-Type": "application/json",
    "X-XSRF-TOKEN": csrfToken,
  },
  body: JSON.stringify({
    email: "user@example.com",
    password: "Password123!",
  }),
});
```

### Example with Axios

```js
axios.defaults.withCredentials = true;

const csrfToken = document.cookie
  .split("; ")
  .find((row) => row.startsWith("XSRF-TOKEN="))
  ?.split("=")[1];

await axios.post(
  "http://localhost:8080/auth/login",
  {
    email: "user@example.com",
    password: "Password123!",
  },
  {
    headers: {
      "X-XSRF-TOKEN": csrfToken,
    },
  }
);
```

### Authorization header for protected API routes

After login, use the returned access token for authenticated calls:

```js
const authResponse = await fetch("http://localhost:8080/auth/me", {
  method: "GET",
  credentials: "include",
  headers: {
    Authorization: `Bearer ${accessToken}`,
  },
});
```

## 8. XSS and browser safety

CSRF protects against cross-site request forgery, but it does not protect against XSS.

The app should also follow these rules:

- escape user-controlled output in the frontend
- never render untrusted HTML directly
- validate all user input on the server
- do not store raw HTML from users without sanitization
- prefer templates/components that escape content by default
- use a Content Security Policy in production

These steps are separate from CSRF but are equally important for overall application security.

## 9. Production hardening recommendations

The current local setup uses `secure(false)` for development convenience. In production, these should be hardened:

- set cookies to `secure: true`
- enforce HTTPS everywhere
- use a strong secret key for JWT signing
- keep refresh tokens server-side and rotate them
- keep the session lifetime short
- log and monitor failed authentication attempts
- restrict CORS to trusted frontend origins

## 10. Security summary

The current app uses a browser-safe hybrid pattern:

- BCrypt for passwords
- JWT for short-lived access tokens
- database-backed refresh sessions for longer-lived auth state
- HttpOnly cookies for refresh token transport
- CSRF enabled for browser requests
- role-based authorization for protected endpoints

This is a solid and modern approach for a web app that needs secure browser authentication without making the whole system purely stateless.
