package de.ohellen.demo.application.game;

import de.ohellen.demo.application.lobby.LobbyService;
import de.ohellen.demo.domain.game.GameResult;
import de.ohellen.demo.domain.game.GameState;
import de.ohellen.demo.domain.game.Lobby;
import de.ohellen.demo.domain.user.User;
import de.ohellen.demo.infrastructure.persistence.GameResultRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {
    private static final List<String> SUITS = List.of("CLUBS", "SPADES", "HEARTS", "DIAMONDS");
    private static final List<String> CARD_VALUES = List.of("2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A");
    private static final Map<String, Integer> RANK_VALUES = Map.ofEntries(
            Map.entry("2", 2),
            Map.entry("3", 3),
            Map.entry("4", 4),
            Map.entry("5", 5),
            Map.entry("6", 6),
            Map.entry("7", 7),
            Map.entry("8", 8),
            Map.entry("9", 9),
            Map.entry("10", 10),
            Map.entry("J", 11),
            Map.entry("Q", 12),
            Map.entry("K", 13),
            Map.entry("A", 14)
    );

    private final LobbyService lobbyService;
    private final GameResultRepository gameResultRepository;
    private final JdbcTemplate jdbcTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final Random random = new Random();
    private final Map<Long, GameState> gamesByLobbyId = new ConcurrentHashMap<>();

    public GameService(LobbyService lobbyService, GameResultRepository gameResultRepository, JdbcTemplate jdbcTemplate, SimpMessagingTemplate messagingTemplate) {
        this.lobbyService = lobbyService;
        this.gameResultRepository = gameResultRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.messagingTemplate = messagingTemplate;
    }

    public GameState startGame(Long lobbyId) {
        Lobby lobby = lobbyService.getLobby(lobbyId);
        if (lobby == null) {
            throw new IllegalArgumentException("Lobby not found");
        }
        if (!lobby.isStarted()) {
            throw new IllegalArgumentException("Lobby has not started");
        }

        String trumpCard = drawTrumpCard();
        String trump = extractSuit(trumpCard);
        Map<Long, String> playerDraws = drawPreGameCards(lobby.getPlayers(), trump);
        List<Long> playerOrder = new ArrayList<>(playerDraws.keySet());

        GameState state = new GameState();
        state.setLobbyId(lobbyId);
        state.setPlayerOrder(playerOrder);
        state.setPlayerDraws(playerDraws);
        state.setTrump(trump);
        state.setTrumpCard(trumpCard);
        state.setRound(1);
        state.setCardsPerRound(13);
        state.setStartedAt(Instant.now());
        state.setCurrentTrick(new ArrayList<>());
        state.setLeadPlayerId(playerOrder.getFirst());
        state.setCurrentPlayerId(playerOrder.getFirst());
        state.setFinished(false);
        state.clearBids();
        state.getRoundResults().clear();
        state.getTotalTricksWon().clear();
        state.resetRoundTricksWon();
        initializeRound(state, 1);
        gamesByLobbyId.put(lobbyId, state);
        return state;
    }

    public GameState submitBid(Long lobbyId, Long userId, Integer amount) {
        GameState game = getGameForLobby(lobbyId);
        if (game == null) {
            throw new IllegalArgumentException("Game not found");
        }
        if (!game.getPlayerOrder().contains(userId)) {
            throw new IllegalArgumentException("Player not in game");
        }
        if (amount < 0 || amount > game.getCardsPerRound()) {
            throw new IllegalArgumentException("Bid must be between 0 and " + game.getCardsPerRound());
        }

        game.setBid(userId, amount);
        if (game.getBids().size() == game.getPlayerOrder().size()) {
            Long leadPlayerId = determineFirstTrickLeader(game);
            game.setLeadPlayerId(leadPlayerId);
            game.setCurrentPlayerId(leadPlayerId);
        }
        return game;
    }

    public GameState finishRound(Long lobbyId) {
        GameState game = getGameForLobby(lobbyId);
        if (game == null) {
            throw new IllegalArgumentException("Game not found");
        }

        if (game.getRound() >= 13) {
            game.recordRoundResult();
            persistCompletedGameResult(game);
            game.setFinished(true);
            game.setCurrentPlayerId(null);
            game.setLeadPlayerId(null);
            gamesByLobbyId.remove(lobbyId);
            return game;
        }

        game.recordRoundResult();
        initializeRound(game, game.getRound() + 1);
        return game;
    }

    private void initializeRound(GameState game, int round) {
        int cardsPerRound = 14 - round;
        game.setRound(round);
        game.setCardsPerRound(cardsPerRound);
        game.setCurrentTrick(new ArrayList<>());
        game.setTricksPlayedThisRound(0);
        game.setLeadPlayerId(game.getPlayerOrder().getFirst());
        game.setCurrentPlayerId(game.getPlayerOrder().getFirst());
        game.clearBids();
        game.setHands(dealHands(game.getPlayerOrder(), cardsPerRound));
        game.resetRoundTricksWon();
    }

    private Long determineFirstTrickLeader(GameState game) {
        Long leader = null;
        Integer highestBid = null;

        for (Long playerId : game.getPlayerOrder()) {
            Integer bid = game.getBids().get(playerId);
            if (bid == null) {
                continue;
            }
            if (highestBid == null || bid > highestBid) {
                highestBid = bid;
                leader = playerId;
            }
        }

        return leader == null ? game.getPlayerOrder().getFirst() : leader;
    }

    private Map<Long, String> drawPreGameCards(java.util.Collection<User> players, String trump) {
        List<String> deck = buildDeck();
        Collections.shuffle(deck, random);

        Map<Long, String> draws = new LinkedHashMap<>();
        for (User user : players) {
            draws.put(user.getUserId(), deck.remove(0));
        }

        List<Map.Entry<Long, String>> sorted = new ArrayList<>(draws.entrySet());
        sorted.sort(Comparator.comparing((Map.Entry<Long, String> entry) -> entry.getValue(), (left, right) -> compareCards(left, right, trump)));

        Map<Long, String> ordered = new LinkedHashMap<>();
        for (Map.Entry<Long, String> entry : sorted) {
            ordered.put(entry.getKey(), entry.getValue());
        }

        return ordered;
    }

    private String drawTrumpCard() {
        List<String> deck = buildDeck();
        Collections.shuffle(deck, random);
        return deck.get(0);
    }

    private Map<String, List<String>> dealHands(List<Long> playerOrder, int cardsPerRound) {
        List<String> deck = buildDeck();
        Collections.shuffle(deck, random);

        Map<String, List<String>> hands = new LinkedHashMap<>();
        for (Long userId : playerOrder) {
            hands.put(String.valueOf(userId), new ArrayList<>());
        }

        for (int i = 0; i < cardsPerRound; i++) {
            for (Long userId : playerOrder) {
                if (deck.isEmpty()) {
                    break;
                }
                hands.get(String.valueOf(userId)).add(deck.remove(0));
            }
        }

        return hands;
    }

    private List<String> buildDeck() {
        List<String> deck = new ArrayList<>();
        for (String suit : SUITS) {
            for (String value : CARD_VALUES) {
                deck.add(suit + "-" + value);
            }
        }
        return deck;
    }

    private int compareCards(String left, String right, String trump) {
        String leftSuit = extractSuit(left);
        String rightSuit = extractSuit(right);
        int leftSuitPower = suitPower(leftSuit, trump);
        int rightSuitPower = suitPower(rightSuit, trump);

        if (leftSuitPower != rightSuitPower) {
            return Integer.compare(leftSuitPower, rightSuitPower);
        }

        int leftValue = cardValue(extractRank(left));
        int rightValue = cardValue(extractRank(right));
        return Integer.compare(rightValue, leftValue);
    }

    private int suitPower(String suit, String trump) {
        List<String> powerOrder = rotateSuitOrder(trump);
        return powerOrder.indexOf(suit);
    }

    private List<String> rotateSuitOrder(String trump) {
        List<String> order = new ArrayList<>(SUITS);
        int index = order.indexOf(trump);
        List<String> rotated = new ArrayList<>();
        for (int i = 0; i < order.size(); i++) {
            rotated.add(order.get((index + i) % order.size()));
        }
        return rotated;
    }

    private String extractSuit(String card) {
        return card.substring(0, card.lastIndexOf('-'));
    }

    private String extractRank(String card) {
        return card.substring(card.lastIndexOf('-') + 1);
    }

    private int cardValue(String rank) {
        return RANK_VALUES.getOrDefault(rank, 0);
    }

    private int compareCardsForTrick(String candidate, String current, String trump, String leadSuit) {
        String candidateSuit = extractSuit(candidate);
        String currentSuit = extractSuit(current);

        boolean candidateTrump = candidateSuit.equalsIgnoreCase(trump);
        boolean currentTrump = currentSuit.equalsIgnoreCase(trump);

        if (candidateTrump && !currentTrump) {
            return 1;
        }
        if (!candidateTrump && currentTrump) {
            return -1;
        }
        if (candidateTrump && currentTrump) {
            return Integer.compare(cardValue(extractRank(candidate)), cardValue(extractRank(current)));
        }

        boolean candidateLead = candidateSuit.equalsIgnoreCase(leadSuit);
        boolean currentLead = currentSuit.equalsIgnoreCase(leadSuit);

        if (candidateLead && !currentLead) {
            return 1;
        }
        if (!candidateLead && currentLead) {
            return -1;
        }
        if (candidateLead && currentLead) {
            return Integer.compare(cardValue(extractRank(candidate)), cardValue(extractRank(current)));
        }

        return 0;
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

        if (!userId.equals(game.getCurrentPlayerId())) {
            throw new IllegalArgumentException("It's not this player's turn");
        }

        List<String> hand = game.getHands().get(String.valueOf(userId));
        if (hand == null || !hand.contains(card)) {
            throw new IllegalArgumentException("Card not in hand");
        }

        hand.remove(card);
        game.recordPlayedCard(userId, card);

        if (game.getCurrentTrick().size() < game.getPlayerOrder().size()) {
            int currentIndex = game.getPlayerOrder().indexOf(userId);
            int nextIndex = (currentIndex + 1) % game.getPlayerOrder().size();
            game.setCurrentPlayerId(game.getPlayerOrder().get(nextIndex));
            return game;
        }

        Long trickWinnerId = determineTrickWinner(game);
        game.recordTrickWin(trickWinnerId);
        game.setLastTrickWinnerId(trickWinnerId);
        game.setLeadPlayerId(trickWinnerId);
        game.setCurrentPlayerId(trickWinnerId);
        game.setTricksPlayedThisRound(game.getTricksPlayedThisRound() + 1);
        game.getCurrentTrick().clear();

        if (game.getTricksPlayedThisRound() >= game.getCardsPerRound()) {
            finishRound(lobbyId);
        }

        return game;
    }

    private Long determineTrickWinner(GameState game) {
        String leadSuit = extractSuit(game.getCurrentTrick().getFirst().split(":", 2)[1]);

        Long winnerId = null;
        String winningCard = null;

        for (String entry : game.getCurrentTrick()) {
            String[] parts = entry.split(":", 2);
            Long playerId = Long.parseLong(parts[0]);
            String card = parts[1];

            if (winnerId == null) {
                winnerId = playerId;
                winningCard = card;
                continue;
            }

            if (compareCardsForTrick(card, winningCard, game.getTrump(), leadSuit) > 0) {
                winnerId = playerId;
                winningCard = card;
            }
        }

        return winnerId;
    }

    private GameResult persistCompletedGameResult(GameState game) {
        Long winnerId = null;
        int highestTotal = -1;
        for (Map.Entry<Long, Integer> entry : game.getTotalTricksWon().entrySet()) {
            if (entry.getValue() > highestTotal) {
                highestTotal = entry.getValue();
                winnerId = entry.getKey();
            }
        }

        Lobby lobby = lobbyService.getLobby(game.getLobbyId());
        List<Map<String, Object>> players = new ArrayList<>();
        for (Long playerId : game.getPlayerOrder()) {
            User user = lobby != null ? lobby.getPlayers().stream().filter(p -> p.getUserId().equals(playerId)).findFirst().orElse(null) : null;
            Map<String, Object> playerEntry = new LinkedHashMap<>();
            playerEntry.put("userId", playerId);
            playerEntry.put("name", user != null ? user.getName() : "Unknown");
            playerEntry.put("totalTricksWon", game.getTotalTricksWon().getOrDefault(playerId, 0));
            players.add(playerEntry);
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("lobbyId", game.getLobbyId());
        summary.put("winnerUserId", winnerId);
        summary.put("players", players);
        summary.put("rounds", game.getRoundResults());

        try {
            GameResult result = new GameResult();
            result.setLobbyId(game.getLobbyId());
            result.setSessionId("lobby-" + game.getLobbyId());
            result.setStartTime(game.getStartedAt());
            result.setEndTime(Instant.now());
            GameResult saved = gameResultRepository.save(result);

            for (Long playerId : game.getPlayerOrder()) {
                int finalScore = game.getTotalTricksWon().getOrDefault(playerId, 0);
                jdbcTemplate.update(
                        "INSERT INTO game_players (game_id, user_id, final_score) VALUES (?, ?, ?)",
                        saved.getId(), playerId.intValue(), finalScore
                );
            }

            for (Map<String, Object> roundResult : game.getRoundResults()) {
                Integer roundNumber = (Integer) roundResult.get("round");
                jdbcTemplate.update(
                        "INSERT INTO rounds (game_id, round_number) VALUES (?, ?)",
                        saved.getId(), roundNumber
                );

                Map<?, ?> bids = (Map<?, ?>) roundResult.get("bids");
                Map<?, ?> tricksWon = (Map<?, ?>) roundResult.get("tricksWon");
                for (Long playerId : game.getPlayerOrder()) {
                    Integer playerCall = bids != null && bids.containsKey(playerId) ? ((Number) bids.get(playerId)).intValue() : 0;
                    Integer pointsEarned = tricksWon != null && tricksWon.containsKey(playerId) ? ((Number) tricksWon.get(playerId)).intValue() : 0;
                    jdbcTemplate.update(
                            "INSERT INTO round_player_stats (game_id, round_number, user_id, player_call, points_earned) VALUES (?, ?, ?, ?, ?)",
                            saved.getId(), roundNumber, playerId.intValue(), playerCall, pointsEarned
                    );
                }
            }

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("type", "game.finished");
            payload.put("result", summary);
            payload.put("gameId", saved.getId());
            messagingTemplate.convertAndSend("/topic/game." + game.getLobbyId(), (Object) payload);
            return saved;
        } catch (Exception exception) {
            throw new IllegalStateException("Could not persist completed game result", exception);
        }
    }

    public void removeGame(Long lobbyId) {
        gamesByLobbyId.remove(lobbyId);
    }
}
