import cardSprite from '../../../resources/PlayingCards.svg'

export const CARD_WIDTH = 64
export const CARD_HEIGHT = 89

export const RANKS = ['A', '2', '3', '4', '5', '6', '7', '8', '9', '10', 'J', 'Q', 'K']

// The sprite sheet stores one suit per row: clubs, hearts, spades, diamonds.
export const SUIT_ROWS = {
  clubs: 0,
  hearts: 1,
  spades: 2,
  diamonds: 3,
}

export const SUIT_LABELS = {
  clubs: 'clubs',
  hearts: 'hearts',
  spades: 'spades',
  diamonds: 'diamonds',
}

export function randomCard() {
  const suitNames = Object.keys(SUIT_ROWS)
  const suit = suitNames[Math.floor(Math.random() * suitNames.length)]
  const rank = RANKS[Math.floor(Math.random() * RANKS.length)]

  return { suit, rank }
}

export function getCardSpriteStyle(suit, rank) {
  const column = RANKS.indexOf(rank)
  const row = SUIT_ROWS[suit]

  if (column === -1 || row === undefined) {
    throw new Error(`Invalid card: ${rank} of ${suit}`)
  }

  return {
    backgroundImage: `url(${cardSprite})`,
    // Percentage positions keep the sprite aligned when the card scales.
    backgroundPosition: `${column * (100 / (RANKS.length - 1))}% ${row * (100 / 3)}%`,
  }
}
