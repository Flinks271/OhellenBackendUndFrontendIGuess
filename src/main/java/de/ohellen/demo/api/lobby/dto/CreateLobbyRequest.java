package de.ohellen.demo.api.lobby.dto;

public class CreateLobbyRequest {
    private Long ownerId;

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
}
