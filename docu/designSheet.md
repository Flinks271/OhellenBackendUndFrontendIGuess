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
4. In-Game
    1. (if needed, starting player is decided)
    2. Trump gets chosen
    3. Get cards
    4. Call your points
    5. Play round
        1. Play trick
            1. Player plays card
            2. All players get update
            3. if <3 have played, go back to 1
        2. if $trick < round$, go back to 1
    6. if $round != 13$, round++ and go back to 3
    7. Get cards with method of step 1
    8. Call your points
    9. Play trick
    10. Show game stats
    11. go back to lobby







