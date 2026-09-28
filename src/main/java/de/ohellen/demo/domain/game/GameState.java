package de.ohellen.demo.domain.game;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GameState {
    private Long lobbyId;
    private List<Long> playerOrder = new ArrayList<>();
    private Map<String, List<String>> hands = new LinkedHashMap<>();
    private List<String> currentTrick = new ArrayList<>();
    private Long currentPlayerId;
    private String trump = "HEARTS";
    private int round = 1;
    private final Map<Long, Integer> bids = new LinkedHashMap<>();

    public Long getLobbyId() { return lobbyId; }
    public void setLobbyId(Long lobbyId) { this.lobbyId = lobbyId; }

    public List<Long> getPlayerOrder() { return playerOrder; }
    public void setPlayerOrder(List<Long> playerOrder) { this.playerOrder = playerOrder; }

    public Map<String, List<String>> getHands() { return hands; }
    public void setHands(Map<String, List<String>> hands) { this.hands = hands; }

    public List<String> getCurrentTrick() { return currentTrick; }
    public void setCurrentTrick(List<String> currentTrick) { this.currentTrick = currentTrick; }

    public Long getCurrentPlayerId() { return currentPlayerId; }
    public void setCurrentPlayerId(Long currentPlayerId) { this.currentPlayerId = currentPlayerId; }

    public String getTrump() { return trump; }
    public void setTrump(String trump) { this.trump = trump; }

    public int getRound() { return round; }
    public void setRound(int round) { this.round = round; }

    public Map<Long, Integer> getBids() { return bids; }
    public void setBid(Long userId, Integer amount) { bids.put(userId, amount); }

    public void recordPlayedCard(Long userId, String card) {
        currentTrick.add(userId + ":" + card);
    }
}
