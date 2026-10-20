import { Link, useParams } from 'react-router-dom'

export default function LobbyPage() {
  const { lobbyId } = useParams()

  return (
    <section className="content-card">
      <p className="eyebrow">Lobby</p>
      <h1>Lobby {lobbyId}</h1>
      <div className="placeholder">
        <h2>Waiting room</h2>
        <p>Players, lobby settings, ready state, and the start-game action will live here.</p>
      </div>
      <p><Link className="nav-link" to="/lobbies">Back to lobbies</Link></p>
    </section>
  )
}
