import { createContext, useContext, useMemo, useState } from 'react'
import { fetchWithCsrf } from '../lib/csrf'

const API_BASE = 'http://localhost:8080'
const AuthContext = createContext(null)

async function readResponse(response, fallbackMessage) {
  const data = await response.json().catch(() => ({}))

  if (!response.ok) {
    throw new Error(data.message || fallbackMessage)
  }

  return data
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(localStorage.getItem('ohellenToken') || '')
  const [user, setUser] = useState(null)

  const login = async (email, password) => {
    const response = await fetchWithCsrf(API_BASE, `${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    })
    const data = await readResponse(response, 'Login failed')

    if (!data.token) {
      throw new Error('No token returned from backend')
    }

    localStorage.setItem('ohellenToken', data.token)
    setToken(data.token)

    const profileResponse = await fetchWithCsrf(API_BASE, `${API_BASE}/auth/me`, {
      headers: { Authorization: `Bearer ${data.token}` },
    })

    if (profileResponse.ok) {
      setUser(await profileResponse.json())
    }
  }

  const register = async (name, email, password) => {
    const response = await fetchWithCsrf(API_BASE, `${API_BASE}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, email, password }),
    })
    return readResponse(response, 'Registration failed')
  }

  const loadUser = async () => {
    if (!token) return null

    const response = await fetchWithCsrf(API_BASE, `${API_BASE}/auth/me`, {
      headers: { Authorization: `Bearer ${token}` },
    })

    if (!response.ok) {
      logout()
      throw new Error('Not authenticated')
    }

    const data = await response.json()
    setUser(data)
    return data
  }

  const logout = async () => {
    if (token) {
      await fetchWithCsrf(API_BASE, `${API_BASE}/auth/logout`, {
        method: 'POST',
      }).catch(() => {})
    }

    localStorage.removeItem('ohellenToken')
    setToken('')
    setUser(null)
  }

  const value = useMemo(
    () => ({ token, user, isAuthenticated: Boolean(token), login, register, loadUser, logout }),
    [token, user]
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)

  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider')
  }

  return context
}
