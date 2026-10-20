import { getCardSpriteStyle, SUIT_LABELS } from './cardData'

export default function Card({ suit, rank, className = '' }) {
  return (
    <div
      aria-label={`${rank} of ${SUIT_LABELS[suit] || suit}`}
      className={`playing-card ${className}`.trim()}
      role="img"
      style={getCardSpriteStyle(suit, rank)}
    />
  )
}
