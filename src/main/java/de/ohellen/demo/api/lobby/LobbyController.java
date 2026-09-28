package de.ohellen.demo.api.lobby;

import de.ohellen.demo.api.lobby.dto.CreateLobbyRequest;
import de.ohellen.demo.api.lobby.dto.JoinLobbyRequest;
import de.ohellen.demo.api.lobby.dto.LobbyDto;
import de.ohellen.demo.application.lobby.LobbyService;
import de.ohellen.demo.application.user.UserService;
import de.ohellen.demo.domain.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
@RequestMapping({"/api/lobbies", "/lobbies"})
public class LobbyController {

    private final LobbyService lobbyService;
    private final UserService userService;

    public LobbyController(LobbyService lobbyService, UserService userService) {
        this.lobbyService = lobbyService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<LobbyDto>> listActiveLobbies() {
        return ResponseEntity.ok(lobbyService.listActiveLobbies());
    }

    @PostMapping
    public ResponseEntity<LobbyDto> createLobby(@RequestBody CreateLobbyRequest request) {
        User owner = userService.findById(request.getOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(lobbyService.createLobby(owner));
    }

    @PostMapping("/join")
    public ResponseEntity<LobbyDto> joinLobby(@RequestBody JoinLobbyRequest request) {
        User user = userService.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(lobbyService.joinLobby(user, request.getCode()));
    }

    @PostMapping("/leave")
    public ResponseEntity<LobbyDto> leaveLobby(@RequestBody JoinLobbyRequest request) {
        User user = userService.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(lobbyService.leaveLobby(user, request.getCode()));
    }

    @PostMapping("/start")
    public ResponseEntity<LobbyDto> startLobby(@RequestBody JoinLobbyRequest request) {
        User owner = userService.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(lobbyService.startLobby(owner, request.getCode()));
    }
}
