# Ohellen API Contract

Base URL:
- `http://localhost:8080`

Authentication:
- Authenticated endpoints use a JWT bearer token in the `Authorization` header.
- Example: `Authorization: Bearer <jwt>`
- Refresh token is stored in a cookie named `refreshToken`.
- The server also supports guest login, where the guest account is reused by name.

Common response format:
- Successful responses usually return JSON.
- Error responses are typically returned as HTTP error status codes with an error message body.

Example error shape:
```json
{
  "message": "Lobby not found",
  "status": 400
}
```

---

# Endpoint Overview

## Authentication
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/guest-login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `GET /api/auth/me`

## Lobbies
- `GET /api/lobbies`
- `POST /api/lobbies`
- `POST /api/lobbies/join`
- `POST /api/lobbies/leave`
- `POST /api/lobbies/start`

## Game and live updates
- `POST /api/game/start`
- `POST /api/game/playcard`
- `POST /api/game/acceptordercard`
- `POST /api/game/choosetrickammount`
- `POST /api/game/reconnect`
- `POST /api/game/drewlastcard`
- WebSocket endpoint: `/ws`
- Topics: `/topic/lobby.<code>`, `/topic/game.<lobbyId>`
- Current WS actions supported by the backend: `/app/game.start`, `/app/game.play`
- Service-level round/bid flow currently implemented: `submitBid`, `finishRound`, `determineFirstTrickLeader`

## User / history
- `GET /api/users`
- `GET /api/users/games`
- `GET /api/games`
- `GET /api/fame`
- `GET /api/shame`

---

# 1. Authentication and Accounts

## `POST /api/auth/register`
Create a normal account.

Request body:
```json
{
  "name": "Alice",
  "email": "alice@example.com",
  "password": "Password123!"
}
```

Success response: `200 OK`
```json
{
  "userId": 1,
  "name": "Alice",
  "email": "alice@example.com",
  "roles": ["ROLE_USER"]
}
```

Possible errors:
- `400 Bad Request` if required fields are missing
- `409 Conflict` if the email is already taken

---

## `POST /api/auth/login`
Log in with an email/password account.

Request body:
```json
{
  "email": "alice@example.com",
  "password": "Password123!"
}
```

Success response: `200 OK`
```json
{
  "token": "<jwt-access-token>",
  "refreshToken": "<refresh-token>"
}
```

Headers:
- `Set-Cookie: refreshToken=<token>; HttpOnly; Path=/; SameSite=Lax`

Notes:
- The returned `token` is used for `Authorization: Bearer <token>` on protected requests.
- The `refreshToken` is returned for the client to also store if needed.

---

## `POST /api/auth/guest-login`
Create or reuse a guest account by name.

Request body:
```json
{
  "name": "Guest Player"
}
```

Success response: `200 OK`
```json
{
  "token": "<jwt-access-token>",
  "refreshToken": "<refresh-token>"
}
```

Behavior:
- If a guest with the same name already exists and has no email/password, it is reused.
- If not, a new guest user is created.

---

## `POST /api/auth/refresh`
Use the refresh cookie to obtain a new JWT.

Required input:
- Cookie: `refreshToken=<token>`

Success response: `200 OK`
```json
{
  "token": "<new-access-token>",
  "refreshToken": "<rotated-refresh-token>"
}
```

Error response:
- `401 Unauthorized` if the refresh token is missing or invalid

---

## `POST /api/auth/logout`
Invalidate the current refresh token and clear the cookie.

Required input:
- Cookie: `refreshToken=<token>`

Success response: `200 OK`

---

## `GET /api/auth/me`
Get the currently authenticated user.

Required headers:
- `Authorization: Bearer <jwt>`

Success response: `200 OK`
```json
{
  "userId": 1,
  "name": "Alice",
  "email": "alice@example.com",
  "roles": ["ROLE_USER"]
}
```

---

# 2. Lobby API

## Lobby flow overview

1. Create a lobby or join an existing one.
2. Invite players by lobby code.
3. Start the lobby when enough players are present.
4. Connect to the websocket topic for the active game.
5. Play cards and receive updates live.

---

## `GET /api/lobbies`
List all active lobbies.

Required headers:
- `Authorization: Bearer <jwt>` if protected in your frontend flow

Success response: `200 OK`
```json
[
  {
    "lobbyId": 1,
    "code": "ABCD12",
    "ownerId": 5,
    "ownerName": "Alice",
    "playerNames": ["Alice", "Bob", "Charlie"],
    "started": false,
    "active": true
  }
]
```

Notes:
- This returns the current active lobby list.
- Lobby codes are generated on creation and used by players to join.

---

## `POST /api/lobbies`
Create a new lobby.

Request body:
```json
{
  "ownerId": 5
}
```

Success response: `200 OK`
```json
{
  "lobbyId": 1,
  "code": "ABCD12",
  "ownerId": 5,
  "ownerName": "Alice",
  "playerNames": ["Alice"],
  "started": false,
  "active": true
}
```

Possible errors:
- `400 Bad Request` if owner is missing
- `404 Not Found` if owner does not exist

---

## `POST /api/lobbies/join`
Join an existing lobby by code.

Request body:
```json
{
  "userId": 7,
  "code": "ABCD12"
}
```

Success response: `200 OK`
```json
{
  "lobbyId": 1,
  "code": "ABCD12",
  "ownerId": 5,
  "ownerName": "Alice",
  "playerNames": ["Alice", "Bob", "Charlie"],
  "started": false,
  "active": true
}
```

Possible errors:
- `400 Bad Request` if lobby code is invalid
- `400 Bad Request` if lobby already started

---

## `POST /api/lobbies/leave`
Leave a lobby.

Request body:
```json
{
  "userId": 7,
  "code": "ABCD12"
}
```

Success response: `200 OK`
```json
{
  "lobbyId": 1,
  "code": "ABCD12",
  "ownerId": 5,
  "ownerName": "Alice",
  "playerNames": ["Alice", "Bob"],
  "started": false,
  "active": true
}
```

Notes:
- If the lobby becomes empty, it is removed from the active list.

---

## `POST /api/lobbies/start`
Start the game for a lobby. Only the lobby owner can do this.

Request body:
```json
{
  "userId": 5,
  "code": "ABCD12"
}
```

Success response: `200 OK`
```json
{
  "lobbyId": 1,
  "code": "ABCD12",
  "ownerId": 5,
  "ownerName": "Alice",
  "playerNames": ["Alice", "Bob", "Charlie"],
  "started": true,
  "active": true
}
```

Possible errors:
- `400 Bad Request` if the caller is not the owner
- `400 Bad Request` if lobby not found

---

# 3. Game API

## Game flow overview

1. Call `/api/game/start` after the lobby has started.
2. Receive the initial hand and current turn.
3. Send card actions via the game API or websocket.
4. Listen on the game topic for updates to current trick, player turns, and final round state.
5. Reconnect if needed using `/api/game/reconnect`.

---

The actual live gameplay is primarily pushed through WebSockets.

## WebSocket bootstrap flow
The client should first call the REST API to create or join a lobby, then use the returned lobby metadata to decide which websocket topic to subscribe to.

Recommended flow:
1. `POST /api/lobbies` or `POST /api/lobbies/join`
2. Receive lobby payload with the game topic information
3. Connect to the websocket endpoint `ws://localhost:8080/ws`
4. Subscribe to `/topic/game.<lobbyId>`
5. Send game actions to `/app/game.start` and `/app/game.play`

Current websocket config:
- Endpoint: `/ws`
- Broker: `/topic`, `/queue`
- Application prefix: `/app`

Example STOMP subscriptions:
```javascript
stompClient.subscribe('/topic/game.1', (message) => {
  const payload = JSON.parse(message.body);
  console.log(payload);
});
```

Example outbound message payloads:
```json
{
  "type": "game.started",
  "game": {
    "lobbyId": 1,
    "playerOrder": [5, 7, 9, 11],
    "playerDraws": {
      "5": "HEARTS-K",
      "7": "SPADES-9",
      "9": "CLUBS-4",
      "11": "DIAMONDS-A"
    },
    "currentPlayerId": 5,
    "leadPlayerId": 5,
    "trump": "HEARTS",
    "trumpCard": "HEARTS-K",
    "round": 1,
    "cardsPerRound": 13,
    "hands": {
      "5": ["HEARTS-7", "HEARTS-8", "HEARTS-9"],
      "7": ["CLUBS-10", "SPADES-9", "DIAMONDS-7"]
    },
    "currentTrick": [],
    "bids": {},
    "lastTrickWinnerId": null
  }
}
```

```json
{
  "type": "game.updated",
  "game": {
    "lobbyId": 1,
    "playerOrder": [5, 7, 9, 11],
    "currentPlayerId": 7,
    "leadPlayerId": 5,
    "trump": "HEARTS",
    "trumpCard": "HEARTS-K",
    "round": 1,
    "cardsPerRound": 13,
    "hands": {
      "5": ["HEARTS-7"],
      "7": ["CLUBS-10"]
    },
    "currentTrick": ["5:HEARTS-7", "7:CLUBS-10"],
    "bids": {
      "5": 3,
      "7": 2,
      "9": 5,
      "11": 1
    },
    "lastTrickWinnerId": 5
  }
}
```

Notes:
- The controller currently emits only `game.started` and `game.updated` for lobby game broadcasts.
- `playerDraws` contains the pre-game draw values used to determine seat order and trump.
- `round` and `cardsPerRound` represent the 13-round timeline.
- `leadPlayerId` is used to determine who starts the first trick of a round.
- `lastTrickWinnerId` is set after a completed trick so the next one begins with the winner.
- Bids are stored on the server in `game.bids` and are used to resolve the leader for the first trick of the round.

### Meaning of the most important fields

The server serializes the game state as plain JSON. A few fields use a compact representation that is important to understand on the client side.

#### `currentTrick`
Example:
```json
"currentTrick": ["5:HEARTS-7", "7:CLUBS-10"]
```

This is interpreted as:
- `5` = the player ID who played the card
- `HEARTS-7` = the card that was played
- the combined string `"5:HEARTS-7"` is encoded as `playerId:card`

So the item:
```json
"5:HEARTS-7"
```
means:
- player `5` played the `HEARTS-7`
- the client should parse the part before the first `:` as the player ID
- the part after the first `:` is the card value

This pattern is used throughout the live trick queue: each entry represents one played card in the current trick.

#### `currentPlayerId`
This is the player whose turn it is now.

Example:
```json
"currentPlayerId": 7
```
means:
- player `7` must play the next card
- the client should allow that player to interact with the table and block other players

#### `leadPlayerId`
This is the player who started the current trick or the first trick of the round.

Example:
```json
"leadPlayerId": 5
```
means:
- player `5` started the trick
- any follow-card validation uses this to determine the lead suit

#### `lastTrickWinnerId`
This is the player who won the previous trick.

Example:
```json
"lastTrickWinnerId": 5
```
means:
- the next trick should begin with player `5`
- this matches the implemented rule: the winner of a trick begins the next trick

#### `hands`
This is keyed by player ID and contains the cards still in each player's hand.

Example:
```json
"hands": {
  "5": ["HEARTS-7", "HEARTS-8"],
  "7": ["CLUBS-10", "SPADES-9"]
}
```
means:
- player `5` has `HEARTS-7` and `HEARTS-8`
- player `7` has `CLUBS-10` and `SPADES-9`

The frontend should only show the current user's own hand and keep other players' hands hidden.

#### `playerOrder`
This defines the table sequence for turns.

Example:
```json
"playerOrder": [5, 7, 9, 11]
```
means:
- the game order is `5 -> 7 -> 9 -> 11 -> 5 -> ...`

This is used for all turn progression and trick-following logic.

### WebSocket message contracts currently exposed by the server

#### `/app/game.start`
Request body:
```json
{
  "lobbyId": 1
}
```

Server response topic: `/topic/game.1`
```json
{
  "type": "game.started",
  "game": {
    "lobbyId": 1,
    "playerOrder": [5, 7, 9, 11],
    "trump": "HEARTS",
    "trumpCard": "HEARTS-K",
    "round": 1,
    "cardsPerRound": 13,
    "currentPlayerId": 5,
    "leadPlayerId": 5,
    "currentTrick": [],
    "hands": {
      "5": ["HEARTS-7", "HEARTS-8", "HEARTS-9"],
      "7": ["CLUBS-10", "SPADES-9", "DIAMONDS-7"]
    }
  }
}
```

#### `/app/game.play`
Request body:
```json
{
  "lobbyId": 1,
  "userId": 5,
  "card": "HEARTS-7"
}
```

Server response topic: `/topic/game.1`
```json
{
  "type": "game.updated",
  "game": {
    "lobbyId": 1,
    "currentPlayerId": 7,
    "currentTrick": ["5:HEARTS-7"],
    "hands": {
      "5": []
    },
    "round": 1,
    "cardsPerRound": 13,
    "trump": "HEARTS"
  }
}
```

Notes:
- A dedicated WebSocket action for bidding is not yet exposed in `GameWebSocketController`; the bid logic exists in the service layer and is ready to be surfaced to the client when the UI is wired up.
- The HTTP REST endpoints are still the easiest way to trigger bid and round advancement logic while the UI layer catches up.

---

## `POST /api/game/start`
Start a game for an existing lobby.

Request body:
```json
{
  "lobbyId": 1
}
```

Success response: `200 OK`
```json
{
  "lobbyId": 1,
  "playerOrder": [5, 7, 9, 11],
  "hands": {
    "5": ["HEARTS-7", "HEARTS-8", "HEARTS-9"],
    "7": ["CLUBS-10", "SPADES-9", "DIAMONDS-7"],
    "9": ["HEARTS-10", "SPADES-7", "CLUBS-8"],
    "11": ["DIAMONDS-9", "CLUBS-7", "SPADES-10"]
  },
  "currentPlayerId": 5,
  "trump": "HEARTS",
  "round": 1,
  "currentTrick": []
}
```

---

## `POST /api/game/playcard`
Play a card in the current trick.

Request body:
```json
{
  "lobbyId": 1,
  "userId": 5,
  "card": "HEARTS-7"
}
```

Success response: `200 OK`
```json
{
  "lobbyId": 1,
  "playerOrder": [5, 7, 9, 11],
  "currentPlayerId": 7,
  "currentTrick": ["5:HEARTS-7"],
  "hands": {
    "5": []
  },
  "round": 1,
  "trump": "HEARTS"
}
```

Possible errors:
- `400 Bad Request` if the player does not own the card
- `400 Bad Request` if the lobby/game is not found

---

## `POST /api/game/acceptordercard`
Reserved endpoint for accepting an ordered card decision.

Request body:
```json
{
  "lobbyId": 1,
  "userId": 5,
  "card": "HEARTS-7"
}
```

Success response: `200 OK`
```json
{
  "status": "accepted"
}
```

---

## `POST /api/game/choosetrickammount`
Reserved endpoint for trick amount / bid selection.

Request body:
```json
{
  "lobbyId": 1,
  "userId": 5,
  "amount": 3
}
```

Success response: `200 OK`
```json
{
  "status": "accepted"
}
```

---

## `POST /api/game/reconnect`
Reconnect a player to a game by user ID.

Request body:
```json
{
  "userId": 5,
  "lobbyId": 1
}
```

Success response: `200 OK`
```json
{
  "connected": true,
  "lobbyId": 1,
  "userId": 5
}
```

---

## `POST /api/game/drewlastcard`
Reserved endpoint for the last-round card selection state.

Request body:
```json
{
  "lobbyId": 1,
  "userId": 5,
  "position": 2
}
```

Success response: `200 OK`
```json
{
  "status": "accepted"
}
```

---

# 4. Book of Shame and Fame

## Fame and shame overview

These endpoints return aggregated records for the player history and ranking-style summaries.

---

## `GET /api/fame`
Return fame entries.

Success response: `200 OK`
```json
[
  {
    "name": "Alice",
    "score": 1240
  }
]
```

## `GET /api/shame`
Return shame entries.

Success response: `200 OK`
```json
[
  {
    "name": "Bob",
    "losses": 8
  }
]
```

---

# 5. User and Game History

## History overview

Use these endpoints to inspect users, player match history, and completed games.

---

## `GET /api/users`
List all users.

Success response: `200 OK`
```json
[
  {
    "userId": 1,
    "name": "Alice",
    "email": "alice@example.com"
  }
]
```

## `GET /api/users/games?userId=1`
List games associated with a user.

Success response: `200 OK`
```json
[
  {
    "userId": 1,
    "games": []
  }
]
```

## `GET /api/games`
List or search completed games.

Success response: `200 OK`
```json
[
  {
    "games": []
  }
]
```

---

# 6. WebSocket event table

## Live update overview

The socket is used only for live state updates after the game has started.

---

| Event type | Sent when | Topic |
| --- | --- | --- |
| `lobby.updated` | lobby state changes | `/topic/lobby.<code>` |
| `game.started` | new game begins | `/topic/game.<lobbyId>` |
| `game.updated` | a card is played or turn changes | `/topic/game.<lobbyId>` |

Client should be ready for these event payloads and update the UI based on the `type` and payload body.

---

# 7. Implementation notes for the client

## Client integration overview

Keep REST for commands and bootstrapping, and use WebSockets only for live updates.

---

- Use the REST API to create or join lobbies.
- Use the REST API to start the game.
- Use WebSockets only for receiving live updates from the active match.
- Keep the JWT in memory and send it as `Authorization: Bearer <token>` for protected endpoints.
- For guest users, send a name and get a reusable guest account by name.
- For game updates, subscribe only to the lobby-specific topic after the lobby is started.

This gives a clean separation:
- REST = commands + bootstrap + state queries
- WebSocket = live game updates

---

# 8. Admin APIs

## Admin section

This section is reserved for moderation, admin-only role management, and internal monitoring endpoints.

---
Current admin endpoints are not yet fully defined in the project and should be added as needed for moderation and role management.
