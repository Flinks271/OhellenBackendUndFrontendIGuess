import { useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../app/AuthContext'

export default function AuthPage() {
  const { login, register, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const [mode, setMode] = useState('login')
  const [form, setForm] = useState({ name: '', email: '', password: '' })
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  if (isAuthenticated) {
    return <Navigate to="/lobbies" replace />
  }

  const handleChange = (event) => {
    const { name, value } = event.target
    setForm((previous) => ({ ...previous, [name]: value }))
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError('')
    setMessage('')

    try {
      if (mode === 'register') {
        const data = await register(form.name, form.email, form.password)
        setMessage(`Registered user ${data.email || form.email}. You can now log in.`)
        setMode('login')
        setForm((previous) => ({ ...previous, name: '', password: '' }))
        return
      }

      await login(form.email, form.password)
      navigate('/lobbies', { replace: true })
    } catch (err) {
      setError(err.message || 'Authentication failed')
    }
  }

  return (
    <div className="auth-shell">
      <div className="auth-card">
        <div className="switcher">
          <button className={mode === 'login' ? 'active' : ''} onClick={() => setMode('login')} type="button">
            Login
          </button>
          <button className={mode === 'register' ? 'active' : ''} onClick={() => setMode('register')} type="button">
            Register
          </button>
        </div>

        <form onSubmit={handleSubmit} className="auth-form">
          <h1>{mode === 'register' ? 'Create account' : 'Welcome back'}</h1>
          {mode === 'register' && (
            <label>
              Name
              <input name="name" value={form.name} onChange={handleChange} placeholder="Your name" required />
            </label>
          )}
          <label>
            Email
            <input type="email" name="email" value={form.email} onChange={handleChange} placeholder="you@example.com" required />
          </label>
          <label>
            Password
            <input type="password" name="password" value={form.password} onChange={handleChange} placeholder="••••••••" required />
          </label>
          <button type="submit">{mode === 'register' ? 'Register' : 'Login'}</button>
        </form>

        {(message || error) && (
          <div className={error ? 'status error' : 'status success'}>
            {error || message}
          </div>
        )}
      </div>
    </div>
  )
}
