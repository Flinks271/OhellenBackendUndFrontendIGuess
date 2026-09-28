package de.ohellen.demo.api.user;

import de.ohellen.demo.application.user.UserService;
import de.ohellen.demo.domain.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
@RequestMapping({"/api", "/api/users"})
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> listUsers() {
        return ResponseEntity.ok(userService.findAllUsers());
    }

    @GetMapping("/users/games")
    public ResponseEntity<List<Map<String, Object>>> listGamesForUser(@RequestParam Long userId) {
        return ResponseEntity.ok(userService.getUserGames(userId));
    }

    @GetMapping("/games")
    public ResponseEntity<List<Map<String, Object>>> listGames() {
        return ResponseEntity.ok(List.of(Map.of("games", List.of())));
    }

    @GetMapping("/fame")
    public ResponseEntity<List<Map<String, Object>>> listFame() {
        return ResponseEntity.ok(List.of(Map.of("name", "No records yet")));
    }

    @GetMapping("/shame")
    public ResponseEntity<List<Map<String, Object>>> listShame() {
        return ResponseEntity.ok(List.of(Map.of("name", "No records yet")));
    }
}
