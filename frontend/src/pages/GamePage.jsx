import { useState } from 'react'
import { useParams } from 'react-router-dom'
import Card from '../features/game/Card'
import { randomCard } from '../features/game/cardData'

export default function GamePage() {
  const { gameId } = useParams()
  const [topCard] = useState(randomCard)

  return (
    <section className="content-card">
      <p className="eyebrow">In progress</p>
      <h1>Game {gameId}</h1>
      <div className="game-preview">
        <div>
          <h2>Game table</h2>
          <p>Cards, scores, round state, player actions, and WebSocket updates will be added here.</p>
        </div>
        <div className="top-card-preview">
          <p className="eyebrow">Random card</p>
          <Card rank={topCard.rank} suit={topCard.suit} />
          <p className="card-caption">{topCard.rank} of {topCard.suit}</p>
        </div>
      </div>
    </section>
  )
}
