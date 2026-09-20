# Authentication + Accounts:
| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/auth/register` | Create an account |
| `POST` | `/api/auth/login` | Log in |
| `POST` | `/api/auth/logout` | Log out/invalidate session |
| `GET` | `/api/auth/me` | Get the current authenticated user |


# Lobbies:
| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/lobbies` | List joinable public lobbies |
| `POST` | `/api/lobbies` | Create a lobby |
| `POST` | `/api/lobbies/join` | Join a lobby |
| `POST` | `/api/lobbies/leave` | Leave a lobby |
| `POST` | `/api/lobbies/start` | Start the pregame |


# Gameplay:
Most of it is handled with websockets
- Giving the player cards
- Update once other player played a card
- Update round begins
- Updates on who won the round
- Updates on who won the game
- Pause, if one player disconnects (for max 1 min)
- Updates in last round that a player has taken card

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/game/acceptordercard` | Plays card contained in request / or deny is returned |
| `POST` | `/api/game/choosetrickammount` | Plays card contained in request / or deny is returned |
| `POST` | `/api/game/playcard` | Plays card contained in request / or deny is returned |
| `POST` | `/api/game/reconnect` | Reconnects player back into game using userID |
| `POST` | `/api/game/drewlastcard` | in last round, reports which card was chosen (position) -> next player can choose from rest |

# Book of Shame and Fame:
| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/shame` | List of shame records |
| `GET` | `/api/fame` | List of fame records |


# Other:
| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/users/games` | List games involving a user (userID in payload) |
| `GET` | `/api/games` | Search or list completed games |
| `GET` | `/api/users` | List all users |


# Admin APIs:
