import { Link } from 'react-router-dom'

export default function NotFoundPage() {
  return (
    <section className="content-card">
      <p className="eyebrow">404</p>
      <h1>Screen not found</h1>
      <p>That page does not exist.</p>
      <Link className="nav-link" to="/lobbies">Return to lobbies</Link>
    </section>
  )
}
