package de.ohellen.demo.domain.game;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GameState {
    private Long lobbyId;
    private Instant startedAt = Instant.now();
    private List<Long> playerOrder = new ArrayList<>();
    private Map<String, List<String>> hands = new LinkedHashMap<>();
    private Map<Long, String> playerDraws = new LinkedHashMap<>();
    private List<String> currentTrick = new ArrayList<>();
    private Long currentPlayerId;
    private Long leadPlayerId;
    private Long lastTrickWinnerId;
    private String trump = "HEARTS";
    private String trumpCard;
    private int round = 1;
    private int cardsPerRound = 13;
    private int tricksPlayedThisRound;
    private boolean finished;
    private final Map<Long, Integer> bids = new LinkedHashMap<>();
    private final Map<Long, Integer> roundTricksWon = new LinkedHashMap<>();
    private final Map<Long, Integer> totalTricksWon = new LinkedHashMap<>();
    private final List<Map<String, Object>> roundResults = new ArrayList<>();

    public Long getLobbyId() { return lobbyId; }
    public void setLobbyId(Long lobbyId) { this.lobbyId = lobbyId; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public List<Long> getPlayerOrder() { return playerOrder; }
    public void setPlayerOrder(List<Long> playerOrder) { this.playerOrder = playerOrder; }

    public Map<String, List<String>> getHands() { return hands; }
    public void setHands(Map<String, List<String>> hands) { this.hands = hands; }

    public Map<Long, String> getPlayerDraws() { return playerDraws; }
    public void setPlayerDraws(Map<Long, String> playerDraws) { this.playerDraws = playerDraws; }

    public List<String> getCurrentTrick() { return currentTrick; }
    public void setCurrentTrick(List<String> currentTrick) { this.currentTrick = currentTrick; }

    public Long getCurrentPlayerId() { return currentPlayerId; }
    public void setCurrentPlayerId(Long currentPlayerId) { this.currentPlayerId = currentPlayerId; }

    public Long getLeadPlayerId() { return leadPlayerId; }
    public void setLeadPlayerId(Long leadPlayerId) { this.leadPlayerId = leadPlayerId; }

    public Long getLastTrickWinnerId() { return lastTrickWinnerId; }
    public void setLastTrickWinnerId(Long lastTrickWinnerId) { this.lastTrickWinnerId = lastTrickWinnerId; }

    public String getTrump() { return trump; }
    public void setTrump(String trump) { this.trump = trump; }

    public String getTrumpCard() { return trumpCard; }
    public void setTrumpCard(String trumpCard) { this.trumpCard = trumpCard; }

    public int getRound() { return round; }
    public void setRound(int round) { this.round = round; }

    public int getCardsPerRound() { return cardsPerRound; }
    public void setCardsPerRound(int cardsPerRound) { this.cardsPerRound = cardsPerRound; }

    public int getTricksPlayedThisRound() { return tricksPlayedThisRound; }
    public void setTricksPlayedThisRound(int tricksPlayedThisRound) { this.tricksPlayedThisRound = tricksPlayedThisRound; }

    public boolean isFinished() { return finished; }
    public void setFinished(boolean finished) { this.finished = finished; }

    public Map<Long, Integer> getBids() { return bids; }
    public void setBid(Long userId, Integer amount) { bids.put(userId, amount); }
    public void clearBids() { bids.clear(); }

    public Map<Long, Integer> getRoundTricksWon() { return roundTricksWon; }
    public void resetRoundTricksWon() {
        roundTricksWon.clear();
        for (Long playerId : playerOrder) {
            roundTricksWon.put(playerId, 0);
        }
    }

    public void recordTrickWin(Long userId) {
        if (userId == null) {
            return;
        }
        roundTricksWon.put(userId, roundTricksWon.getOrDefault(userId, 0) + 1);
        totalTricksWon.put(userId, totalTricksWon.getOrDefault(userId, 0) + 1);
    }

    public Map<Long, Integer> getTotalTricksWon() { return totalTricksWon; }

    public List<Map<String, Object>> getRoundResults() { return roundResults; }
    public void recordRoundResult() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("round", round);
        result.put("cardsPerRound", cardsPerRound);
        result.put("leadPlayerId", leadPlayerId);
        result.put("bids", new LinkedHashMap<>(bids));
        result.put("tricksWon", new LinkedHashMap<>(roundTricksWon));
        result.put("winnerPlayerId", determineWinningPlayer(roundTricksWon));
        roundResults.add(result);
        resetRoundTricksWon();
    }

    private Long determineWinningPlayer(Map<Long, Integer> trickMap) {
        Long winner = null;
        int highest = -1;
        for (Map.Entry<Long, Integer> entry : trickMap.entrySet()) {
            if (entry.getValue() > highest) {
                highest = entry.getValue();
                winner = entry.getKey();
            }
        }
        return winner;
    }

    public void recordPlayedCard(Long userId, String card) {
        currentTrick.add(userId + ":" + card);
    }
}
