package de.ohellen.demo.api.game;

import de.ohellen.demo.application.game.GameService;
import de.ohellen.demo.application.lobby.LobbyService;
import de.ohellen.demo.domain.game.GameState;
import de.ohellen.demo.domain.game.Lobby;
import de.ohellen.demo.domain.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
@RequestMapping({"/api/game", "/game"})
public class GameController {

    private final GameService gameService;
    private final LobbyService lobbyService;

    public GameController(GameService gameService, LobbyService lobbyService) {
        this.gameService = gameService;
        this.lobbyService = lobbyService;
    }

    @GetMapping("/lobby/{lobbyId}")
    public ResponseEntity<GameState> getGame(@PathVariable Long lobbyId) {
        return ResponseEntity.ok(gameService.getGameForLobby(lobbyId));
    }

    @PostMapping("/playcard")
    public ResponseEntity<GameState> playCard(@RequestBody Map<String, Object> payload) {
        Long lobbyId = ((Number) payload.get("lobbyId")).longValue();
        Long userId = ((Number) payload.get("userId")).longValue();
        String card = String.valueOf(payload.get("card"));

        GameState game = gameService.playCard(lobbyId, userId, card);
        return ResponseEntity.ok(game);
    }

    @PostMapping("/start")
    public ResponseEntity<GameState> startGame(@RequestBody Map<String, Object> payload) {
        Long lobbyId = ((Number) payload.get("lobbyId")).longValue();
        return ResponseEntity.ok(gameService.startGame(lobbyId));
    }

    @PostMapping("/acceptordercard")
    public ResponseEntity<Map<String, Object>> acceptOrderCard(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(Map.of("status", "accepted"));
    }

    @PostMapping("/choosetrickammount")
    public ResponseEntity<Map<String, Object>> chooseTrickAmount(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(Map.of("status", "accepted"));
    }

    @PostMapping("/reconnect")
    public ResponseEntity<Map<String, Object>> reconnect(@RequestBody Map<String, Object> payload) {
        Long userId = ((Number) payload.get("userId")).longValue();
        Long lobbyId = ((Number) payload.get("lobbyId")).longValue();
        Lobby lobby = lobbyService.getLobby(lobbyId);
        boolean found = lobby != null && lobby.getPlayers().stream().anyMatch(u -> u.getUserId().equals(userId));
        Map<String, Object> result = new HashMap<>();
        result.put("connected", found);
        result.put("lobbyId", lobbyId);
        result.put("userId", userId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/drewlastcard")
    public ResponseEntity<Map<String, Object>> drewLastCard(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(Map.of("status", "accepted"));
    }
}
