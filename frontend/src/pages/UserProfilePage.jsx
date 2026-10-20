import { useParams } from 'react-router-dom'

export default function UserProfilePage() {
  const { userId } = useParams()

  return (
    <section className="content-card">
      <p className="eyebrow">Player profile</p>
      <h1>User {userId}</h1>
      <div className="placeholder">
        <h2>User inspection</h2>
        <p>Player statistics and match history will be loaded here.</p>
      </div>
    </section>
  )
}
