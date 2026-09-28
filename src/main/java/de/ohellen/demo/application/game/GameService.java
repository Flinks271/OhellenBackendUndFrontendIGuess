package de.ohellen.demo.application.game;

import de.ohellen.demo.application.lobby.LobbyService;
import de.ohellen.demo.domain.game.GameState;
import de.ohellen.demo.domain.game.Lobby;
import de.ohellen.demo.domain.user.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {
    private final LobbyService lobbyService;
    private final Map<Long, GameState> gamesByLobbyId = new ConcurrentHashMap<>();

    public GameService(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    public GameState startGame(Long lobbyId) {
        Lobby lobby = lobbyService.getLobby(lobbyId);
        if (lobby == null) {
            throw new IllegalArgumentException("Lobby not found");
        }
        if (!lobby.isStarted()) {
            throw new IllegalArgumentException("Lobby has not started");
        }

        List<Long> playerOrder = new ArrayList<>();
        for (User user : lobby.getPlayers()) {
            playerOrder.add(user.getUserId());
        }

        Map<String, List<String>> hands = new LinkedHashMap<>();
        String[] suits = {"HEARTS", "DIAMONDS", "CLUBS", "SPADES"};
        for (Long userId : playerOrder) {
            List<String> cards = new ArrayList<>();
            for (String suit : suits) {
                cards.add(suit + "-7");
                cards.add(suit + "-8");
                cards.add(suit + "-9");
                cards.add(suit + "-10");
            }
            hands.put(String.valueOf(userId), cards);
        }

        GameState state = new GameState();
        state.setLobbyId(lobbyId);
        state.setPlayerOrder(playerOrder);
        state.setHands(hands);
        state.setCurrentPlayerId(playerOrder.get(0));
        state.setTrump("HEARTS");
        gamesByLobbyId.put(lobbyId, state);
        return state;
    }

    public GameState getGameForLobby(Long lobbyId) {
        GameState state = gamesByLobbyId.get(lobbyId);
        if (state == null) {
            Lobby lobby = lobbyService.getLobby(lobbyId);
            if (lobby != null && lobby.isStarted()) {
                return startGame(lobbyId);
            }
        }
        return state;
    }

    public GameState playCard(Long lobbyId, Long userId, String card) {
        GameState game = getGameForLobby(lobbyId);
        if (game == null) {
            throw new IllegalArgumentException("Game not found");
        }

        List<String> hand = game.getHands().get(String.valueOf(userId));
        if (hand == null || !hand.contains(card)) {
            throw new IllegalArgumentException("Card not in hand");
        }

        hand.remove(card);
        game.recordPlayedCard(userId, card);

        int nextIndex = game.getPlayerOrder().indexOf(userId) + 1;
        if (nextIndex >= game.getPlayerOrder().size()) {
            nextIndex = 0;
        }
        game.setCurrentPlayerId(game.getPlayerOrder().get(nextIndex));

        return game;
    }

    public void removeGame(Long lobbyId) {
        gamesByLobbyId.remove(lobbyId);
    }
}
