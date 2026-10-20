import { Link } from 'react-router-dom'

const screens = [
  { to: '/lobbies/example-lobby', title: 'Lobby screen', description: 'Open a lobby and see its players.' },
  { to: '/games/example-game', title: 'Game screen', description: 'Placeholder for the live card table.' },
  { to: '/users/example-user', title: 'User inspection', description: 'View player stats and match history.' },
]

export default function LobbyListPage() {
  return (
    <section className="content-card">
      <div className="page-intro">
        <p className="eyebrow">Play online</p>
        <h1>Lobby selection</h1>
        <p>The lobby list will be loaded from the backend here. These links demonstrate the screen structure while that API is being built.</p>
      </div>

      <button type="button">Create lobby</button>

      <div className="screen-grid">
        {screens.map((screen) => (
          <Link className="screen-link" key={screen.to} to={screen.to}>
            <strong>{screen.title}</strong>
            <span>{screen.description}</span>
          </Link>
        ))}
      </div>
    </section>
  )
}
