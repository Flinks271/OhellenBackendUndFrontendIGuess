package de.ohellen.demo.api.lobby.dto;

import java.util.ArrayList;
import java.util.List;

public class LobbyDto {
    private Long lobbyId;
    private String code;
    private Long ownerId;
    private String ownerName;
    private List<String> playerNames = new ArrayList<>();
    private boolean started;
    private boolean active;

    public LobbyDto() {}

    public Long getLobbyId() { return lobbyId; }
    public void setLobbyId(Long lobbyId) { this.lobbyId = lobbyId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public List<String> getPlayerNames() { return playerNames; }
    public void setPlayerNames(List<String> playerNames) { this.playerNames = playerNames; }

    public boolean isStarted() { return started; }
    public void setStarted(boolean started) { this.started = started; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
