package de.ohellen.demo.domain.game;

import de.ohellen.demo.domain.user.User;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Lobby {
    private Long lobbyId;
    private String code;
    private Long ownerId;
    private String ownerName;
    private final Set<User> players = new LinkedHashSet<>();
    private boolean started;
    private boolean active = true;
    private Instant createdAt = Instant.now();

    public Long getLobbyId() { return lobbyId; }
    public void setLobbyId(Long lobbyId) { this.lobbyId = lobbyId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public Set<User> getPlayers() { return players; }
    public void addPlayer(User user) { if (user != null) players.add(user); }
    public void removePlayer(User user) { players.remove(user); }

    public boolean isStarted() { return started; }
    public void setStarted(boolean started) { this.started = started; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public List<String> playerNames() {
        List<String> names = new ArrayList<>();
        for (User user : players) {
            names.add(user.getName());
        }
        return names;
    }
}
