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
}
