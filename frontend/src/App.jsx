import { useMemo, useState } from 'react'

const API_BASE = 'http://localhost:8080'

export default function App() {
  const [mode, setMode] = useState('login')
  const [form, setForm] = useState({ name: '', email: '', password: '' })
  const [token, setToken] = useState(localStorage.getItem('ohellenToken') || '')
  const [me, setMe] = useState(null)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const authHeader = useMemo(() => (token ? { Authorization: `Bearer ${token}` } : {}), [token])

  const handleChange = (e) => {
    const { name, value } = e.target
    setForm((prev) => ({ ...prev, [name]: value }))
  }

  const handleRegister = async (e) => {
    e.preventDefault()
    setError('')
    setMessage('')

    try {
      const response = await fetch(`${API_BASE}/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: form.name,
          email: form.email,
          password: form.password,
        }),
      })

      if (!response.ok) {
        throw new Error('Registration failed')
      }

      const data = await response.json()
      setMessage(`Registered user ${data.email || form.email}`)
      setMode('login')
      setForm((prev) => ({ ...prev, name: '', password: '' }))
    } catch (err) {
      setError(err.message || 'Registration failed')
    }
  }

  const handleLogin = async (e) => {
    e.preventDefault()
    setError('')
    setMessage('')

    try {
      const response = await fetch(`${API_BASE}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: form.email,
          password: form.password,
        }),
      })

      const data = await response.json()

      if (!response.ok) {
        throw new Error(data.message || 'Login failed')
      }

      if (!data.token) {
        throw new Error('No token returned from backend')
      }

      localStorage.setItem('ohellenToken', data.token)
      setToken(data.token)
      setMessage('Login successful')
      setForm((prev) => ({ ...prev, password: '' }))

      const meResponse = await fetch(`${API_BASE}/auth/me`, {
        headers: { ...authHeader, 'Content-Type': 'application/json' },
      })

      if (meResponse.ok) {
        const meData = await meResponse.json()
        setMe(meData)
      }
    } catch (err) {
      setError(err.message || 'Login failed')
    }
  }

  const handleLogout = () => {
    localStorage.removeItem('ohellenToken')
    setToken('')
    setMe(null)
    setMessage('Logged out')
  }

  const loadMe = async () => {
    if (!token) return

    try {
      const response = await fetch(`${API_BASE}/auth/me`, {
        headers: { Authorization: `Bearer ${token}` },
      })

      if (!response.ok) {
        throw new Error('Not authenticated')
      }

      const data = await response.json()
      setMe(data)
    } catch (err) {
      setError(err.message || 'Could not load profile')
    }
  }

  return (
    <div className="page-shell">
      <div className="card">
        <div className="switcher">
          <button className={mode === 'login' ? 'active' : ''} onClick={() => setMode('login')} type="button">
            Login
          </button>
          <button className={mode === 'register' ? 'active' : ''} onClick={() => setMode('register')} type="button">
            Register
          </button>
        </div>

        {mode === 'register' ? (
          <form onSubmit={handleRegister} className="auth-form">
            <h1>Create account</h1>
            <label>
              Name
              <input name="name" value={form.name} onChange={handleChange} placeholder="Your name" required />
            </label>
            <label>
              Email
              <input type="email" name="email" value={form.email} onChange={handleChange} placeholder="you@example.com" required />
            </label>
            <label>
              Password
              <input type="password" name="password" value={form.password} onChange={handleChange} placeholder="••••••••" required />
            </label>
            <button type="submit">Register</button>
          </form>
        ) : (
          <form onSubmit={handleLogin} className="auth-form">
            <h1>Welcome back</h1>
            <label>
              Email
              <input type="email" name="email" value={form.email} onChange={handleChange} placeholder="you@example.com" required />
            </label>
            <label>
              Password
              <input type="password" name="password" value={form.password} onChange={handleChange} placeholder="••••••••" required />
            </label>
            <button type="submit">Login</button>
          </form>
        )}

        {(message || error) && (
          <div className={error ? 'status error' : 'status success'}>
            {error || message}
          </div>
        )}

        {token && (
          <div className="profile-box">
            <h2>Logged in</h2>
            <p>Token saved in browser storage.</p>
            <div className="inline-actions">
              <button onClick={loadMe} type="button">Load profile</button>
              <button className="ghost" onClick={handleLogout} type="button">Logout</button>
            </div>
            {me && (
              <pre>{JSON.stringify(me, null, 2)}</pre>
            )}
          </div>
        )}
      </div>
    </div>
  )
}
