package de.ohellen.demo;

import de.ohellen.demo.api.lobby.dto.LobbyDto;
import de.ohellen.demo.application.game.GameService;
import de.ohellen.demo.application.lobby.LobbyService;
import de.ohellen.demo.application.user.UserService;
import de.ohellen.demo.domain.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class GameAndLobbyFlowTests {

    @Autowired
    private UserService userService;

    @Autowired
    private LobbyService lobbyService;

    @Autowired
    private GameService gameService;

    @Test
    void guestUsersAreReusedByName() {
        User guestOne = userService.findOrCreateGuest("Guest Player");
        User guestTwo = userService.findOrCreateGuest("Guest Player");

        assertThat(guestOne.getUserId()).isEqualTo(guestTwo.getUserId());
        assertThat(guestOne.getEmail()).isNull();
        assertThat(guestOne.getPassword()).isNull();
    }

    @Test
    void activeLobbiesAreListedAndGameCanStart() {
        User owner = userService.register("Owner", "owner@test.com", "Password123!");
        User playerTwo = userService.register("Second", "second@test.com", "Password123!");

        LobbyDto lobby = lobbyService.createLobby(owner);
        lobbyService.joinLobby(playerTwo, lobby.getCode());

        List<LobbyDto> activeLobbies = lobbyService.listActiveLobbies();
        assertThat(activeLobbies).extracting(LobbyDto::getCode).contains(lobby.getCode());

        lobbyService.startLobby(owner, lobby.getCode());
        assertThat(gameService.getGameForLobby(lobby.getLobbyId())).isNotNull();
    }

    @Test
    void aGameCanAcceptAPlayedCard() {
        User owner = userService.register("Owner", "owner2@test.com", "Password123!");
        User playerTwo = userService.register("Second", "second2@test.com", "Password123!");
        User playerThree = userService.register("Third", "third2@test.com", "Password123!");
        User playerFour = userService.register("Fourth", "fourth2@test.com", "Password123!");

        LobbyDto lobby = lobbyService.createLobby(owner);
        lobbyService.joinLobby(playerTwo, lobby.getCode());
        lobbyService.joinLobby(playerThree, lobby.getCode());
        lobbyService.joinLobby(playerFour, lobby.getCode());
        lobbyService.startLobby(owner, lobby.getCode());

        var game = gameService.getGameForLobby(lobby.getLobbyId());
        assertThat(game).isNotNull();

        Long currentPlayerId = game.getCurrentPlayerId();
        String cardToPlay = game.getHands().get(currentPlayerId.toString()).getFirst();

        var updatedGame = gameService.playCard(lobby.getLobbyId(), currentPlayerId, cardToPlay);

        assertThat(updatedGame.getCurrentTrick()).isNotEmpty();
        assertThat(updatedGame.getHands().get(currentPlayerId.toString())).doesNotContain(cardToPlay);
    }

    @Test
    void aCompletedTrickResetsTheTurnOrder() {
        User owner = userService.register("Owner", "owner3@test.com", "Password123!");
        User playerTwo = userService.register("Second", "second3@test.com", "Password123!");
        User playerThree = userService.register("Third", "third3@test.com", "Password123!");
        User playerFour = userService.register("Fourth", "fourth3@test.com", "Password123!");

        LobbyDto lobby = lobbyService.createLobby(owner);
        lobbyService.joinLobby(playerTwo, lobby.getCode());
        lobbyService.joinLobby(playerThree, lobby.getCode());
        lobbyService.joinLobby(playerFour, lobby.getCode());
        lobbyService.startLobby(owner, lobby.getCode());

        var game = gameService.getGameForLobby(lobby.getLobbyId());

        for (Long playerId : game.getPlayerOrder()) {
            String card = game.getHands().get(playerId.toString()).getFirst();
            game = gameService.playCard(lobby.getLobbyId(), playerId, card);
        }

        assertThat(game.getCurrentTrick()).isEmpty();
        assertThat(game.getPlayerOrder()).contains(game.getCurrentPlayerId());
    }

    @Test
    void gameStartDrawsATrumpCardAndSeatOrder() {
        User owner = userService.register("Owner", "owner4@test.com", "Password123!");
        User playerTwo = userService.register("Second", "second4@test.com", "Password123!");
        User playerThree = userService.register("Third", "third4@test.com", "Password123!");
        User playerFour = userService.register("Fourth", "fourth4@test.com", "Password123!");

        LobbyDto lobby = lobbyService.createLobby(owner);
        lobbyService.joinLobby(playerTwo, lobby.getCode());
        lobbyService.joinLobby(playerThree, lobby.getCode());
        lobbyService.joinLobby(playerFour, lobby.getCode());
        lobbyService.startLobby(owner, lobby.getCode());

        var game = gameService.getGameForLobby(lobby.getLobbyId());

        assertThat(game.getTrumpCard()).isNotBlank();
        assertThat(game.getTrump()).isIn("CLUBS", "SPADES", "HEARTS", "DIAMONDS");
        assertThat(game.getPlayerDraws()).hasSize(4);
        assertThat(game.getPlayerOrder()).containsExactlyInAnyOrderElementsOf(List.of(owner.getUserId(), playerTwo.getUserId(), playerThree.getUserId(), playerFour.getUserId()));
    }

    @Test
    void thirteenRoundGameLoopTracksRoundProgressAndBidLeader() {
        User owner = userService.register("Owner", "owner5@test.com", "Password123!");
        User playerTwo = userService.register("Second", "second5@test.com", "Password123!");
        User playerThree = userService.register("Third", "third5@test.com", "Password123!");
        User playerFour = userService.register("Fourth", "fourth5@test.com", "Password123!");

        LobbyDto lobby = lobbyService.createLobby(owner);
        lobbyService.joinLobby(playerTwo, lobby.getCode());
        lobbyService.joinLobby(playerThree, lobby.getCode());
        lobbyService.joinLobby(playerFour, lobby.getCode());
        lobbyService.startLobby(owner, lobby.getCode());

        var game = gameService.getGameForLobby(lobby.getLobbyId());
        assertThat(game.getRound()).isEqualTo(1);
        assertThat(game.getCardsPerRound()).isEqualTo(13);
        assertThat(game.getHands().get(owner.getUserId().toString())).hasSize(13);

        List<Long> playerOrder = game.getPlayerOrder();
        for (Long playerId : playerOrder) {
            gameService.submitBid(lobby.getLobbyId(), playerId, 0);
        }

        game = gameService.getGameForLobby(lobby.getLobbyId());
        assertThat(game.getLeadPlayerId()).isNotNull();
        assertThat(game.getCurrentPlayerId()).isEqualTo(game.getLeadPlayerId());

        gameService.finishRound(lobby.getLobbyId());
        game = gameService.getGameForLobby(lobby.getLobbyId());

        assertThat(game.getRound()).isEqualTo(2);
        assertThat(game.getCardsPerRound()).isEqualTo(12);
        assertThat(game.getHands().get(owner.getUserId().toString())).hasSize(12);
    }
}
