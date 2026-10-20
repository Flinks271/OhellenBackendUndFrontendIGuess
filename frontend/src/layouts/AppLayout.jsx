import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../app/AuthContext'

const links = [
  { to: '/lobbies', label: 'Lobbies' },
  { to: '/fame-shame', label: 'Fame / Shame' },
]

export default function AppLayout({ children }) {
  const { logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <NavLink className="brand" to="/lobbies">Ohellen</NavLink>
        <nav aria-label="Main navigation">
          {links.map((link) => (
            <NavLink
              className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}
              key={link.to}
              to={link.to}
            >
              {link.label}
            </NavLink>
          ))}
          <button className="ghost" onClick={handleLogout} type="button">Logout</button>
        </nav>
      </header>
      <main className="main-content">{children}</main>
    </div>
  )
}
