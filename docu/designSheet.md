# Ohellen
## Features:
- Book of fame/shame
- Accounts
    - all normal features (e.g. resetting password)
    - **later:** friend list
- Ohellen
    - **first:** Only for pc + Play by click
    - **later:** mobile devices + drag and drop
    - **later:** settings for sorting cards and how they are ordered
    - **later:** true choosing of card in last round
- Lobbies
    - Session based games
    - Scores for games played sequentially
    - **later:** Chat or Voice-chat
- Ranking
    - **first:** average points per game (in a certain period)
    - **later:** maybe win-rate based points

## Database:
- Tables:
    - Users
        - Name
        - Email (null, if guest)
        - User ID
    - Games
        - Player 1-4
        - Score 1-4
        - Game Id (Key)
        - Session ID
        - Timestamp
    - Rounds
        - Game Id
        - Round Number ([1 ... 13])
        - Call 1-4
        - Points 1-4 (without bonus +10)
    - UserRolls
        - User ID
        - Roll ID
    - RollTypes
        - Roll ID
        - Roll name
- Tech-Stack:
    - PostgreSQL with Flyway


## Gameplay Loop:
1. Login or guest login:
2. Online play options (Create or join lobby)
    - If Join -> Screen with lobby list and lobby-code input field
3. Lobby Screen
    - Game Settings
        - Round Timer
        - **later:** Card Skin + Background Skin (maybe can be set on main lobby screen for each player)
4. Pregame setup
    1. A trump card is drawn from the 52-card deck.
    2. The trump suit is extracted from that card.
    3. Each player draws one card from the shuffled deck.
    4. The cards are ordered by rank relative to the trump suit.
    5. The resulting sorted draw order decides the player seat order at the table.
5. Start of the game
    1. There are exactly 13 rounds.
    2. Round 1 deals 13 cards to each player.
    3. Every following round reduces the hand size by 1 card.
    4. The last round deals exactly 1 card to each player.
6. Bidding / prediction phase
    1. For each round, every player predicts how many tricks they expect to win.
    2. Prediction range is from 0 to the current cards-per-round value.
    3. The player with the highest prediction starts the first trick of the round.
    4. If two or more players tie for the highest prediction, the player who predicted first in the seat order starts.
7. Trick phase
    1. The current leader starts the trick.
    2. Remaining players follow in seat order.
    3. The highest valid card wins the trick according to suit precedence and trump rules.
    4. After a trick is resolved, the winner of that trick starts the next trick.
    5. The round continues until all tricks of the current hand size are played.
8. Round transition
    1. When the round ends, the server advances to the next round.
    2. The cards-per-round value decreases by 1.
    3. The new round starts with a fresh bidding phase.
9. Game end
    1. After round 13 is complete, the game ends.
    2. Final scores can then be calculated and shown to the lobby.
    3. The lobby can return to its lobby state after the result screen.

Implementation status:
- Pregame draw and seat-order generation are implemented.
- Trump selection and deck comparison logic are implemented.
- Thirteen-round progression and decreasing card count are implemented.
- Bid-based opening leader selection and trick-winner-based lead progression are implemented in the service layer.
- A dedicated broadcast action for bidding is not yet exposed in the WebSocket controller, but the backend state and round mechanics are in place.







