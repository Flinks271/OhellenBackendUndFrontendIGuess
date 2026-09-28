package de.ohellen.demo.api.lobby.dto;

public class JoinLobbyRequest {
    private Long userId;
    private String code;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
