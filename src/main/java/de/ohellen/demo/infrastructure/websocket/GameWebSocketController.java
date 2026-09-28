package de.ohellen.demo.infrastructure.websocket;

import de.ohellen.demo.application.game.GameService;
import de.ohellen.demo.application.lobby.LobbyService;
import de.ohellen.demo.domain.game.GameState;
import de.ohellen.demo.domain.game.Lobby;
import de.ohellen.demo.domain.user.User;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
public class GameWebSocketController {

    private final GameService gameService;
    private final LobbyService lobbyService;
    private final SimpMessagingTemplate messagingTemplate;

    public GameWebSocketController(GameService gameService, LobbyService lobbyService, SimpMessagingTemplate messagingTemplate) {
        this.gameService = gameService;
        this.lobbyService = lobbyService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/lobby.join")
    public void joinLobby(@Payload Map<String, Object> message) {
        Long userId = ((Number) message.get("userId")).longValue();
        String code = String.valueOf(message.get("code"));
        Lobby lobby = lobbyService.getLobbyByCode(code);
        if (lobby == null) {
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "lobby.updated");
        payload.put("lobby", lobbyService.getLobbyByCode(code));
        messagingTemplate.convertAndSend((String) ("/topic/lobby." + code), payload);
    }

    @MessageMapping("/game.start")
    public void startGame(@Payload Map<String, Object> message) {
        Long lobbyId = ((Number) message.get("lobbyId")).longValue();
        GameState state = gameService.startGame(lobbyId);
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "game.started");
        payload.put("game", state);
        messagingTemplate.convertAndSend((String) ("/topic/game." + lobbyId), payload);
    }

    @MessageMapping("/game.play")
    public void playCard(@Payload Map<String, Object> message) {
        Long lobbyId = ((Number) message.get("lobbyId")).longValue();
        Long userId = ((Number) message.get("userId")).longValue();
        String card = String.valueOf(message.get("card"));

        GameState state = gameService.playCard(lobbyId, userId, card);
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "game.updated");
        payload.put("game", state);
        messagingTemplate.convertAndSend((String) ("/topic/game." + lobbyId), payload);
    }
}
