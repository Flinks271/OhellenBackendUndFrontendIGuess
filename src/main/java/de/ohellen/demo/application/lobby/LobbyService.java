package de.ohellen.demo.application.lobby;

import de.ohellen.demo.api.lobby.dto.LobbyDto;
import de.ohellen.demo.domain.game.Lobby;
import de.ohellen.demo.domain.user.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class LobbyService {
    private final Map<Long, Lobby> lobbiesById = new ConcurrentHashMap<>();
    private final Map<String, Long> lobbyIdByCode = new ConcurrentHashMap<>();
    private final AtomicLong lobbySequence = new AtomicLong(1L);

    public LobbyDto createLobby(User owner) {
        if (owner == null) {
            throw new IllegalArgumentException("Owner required");
        }

        Lobby lobby = new Lobby();
        lobby.setLobbyId(lobbySequence.getAndIncrement());
        lobby.setCode(generateCode());
        lobby.setOwnerId(owner.getUserId());
        lobby.setOwnerName(owner.getName());
        lobby.addPlayer(owner);
        lobbiesById.put(lobby.getLobbyId(), lobby);
        lobbyIdByCode.put(lobby.getCode(), lobby.getLobbyId());
        return toDto(lobby);
    }

    public LobbyDto joinLobby(User user, String code) {
        Long lobbyId = lobbyIdByCode.get(code);
        if (lobbyId == null) {
            throw new IllegalArgumentException("Lobby not found");
        }
        Lobby lobby = lobbiesById.get(lobbyId);
        if (lobby == null || !lobby.isActive()) {
            throw new IllegalArgumentException("Lobby is not active");
        }
        if (lobby.isStarted()) {
            throw new IllegalArgumentException("Lobby already started");
        }
        lobby.addPlayer(user);
        return toDto(lobby);
    }

    public LobbyDto leaveLobby(User user, String code) {
        Long lobbyId = lobbyIdByCode.get(code);
        if (lobbyId == null) {
            throw new IllegalArgumentException("Lobby not found");
        }
        Lobby lobby = lobbiesById.get(lobbyId);
        if (lobby == null) {
            throw new IllegalArgumentException("Lobby not found");
        }
        lobby.removePlayer(user);
        if (lobby.getPlayers().isEmpty()) {
            removeLobby(lobbyId);
        }
        return toDto(lobby);
    }

    public List<LobbyDto> listActiveLobbies() {
        List<LobbyDto> result = new ArrayList<>();
        for (Lobby lobby : lobbiesById.values()) {
            if (lobby.isActive()) {
                result.add(toDto(lobby));
            }
        }
        return result;
    }

    public LobbyDto startLobby(User owner, String code) {
        Long lobbyId = lobbyIdByCode.get(code);
        if (lobbyId == null) {
            throw new IllegalArgumentException("Lobby not found");
        }
        Lobby lobby = lobbiesById.get(lobbyId);
        if (lobby == null) {
            throw new IllegalArgumentException("Lobby not found");
        }
        if (!owner.getUserId().equals(lobby.getOwnerId())) {
            throw new IllegalArgumentException("Only the owner can start the lobby");
        }
        lobby.setStarted(true);
        return toDto(lobby);
    }

    public Lobby getLobby(Long lobbyId) {
        return lobbiesById.get(lobbyId);
    }

    public Lobby getLobbyByCode(String code) {
        Long lobbyId = lobbyIdByCode.get(code);
        return lobbyId == null ? null : lobbiesById.get(lobbyId);
    }

    public void removeLobby(Long lobbyId) {
        Lobby lobby = lobbiesById.remove(lobbyId);
        if (lobby != null) {
            lobbyIdByCode.remove(lobby.getCode());
        }
    }

    private LobbyDto toDto(Lobby lobby) {
        LobbyDto dto = new LobbyDto();
        dto.setLobbyId(lobby.getLobbyId());
        dto.setCode(lobby.getCode());
        dto.setOwnerId(lobby.getOwnerId());
        dto.setOwnerName(lobby.getOwnerName());
        dto.setPlayerNames(lobby.playerNames());
        dto.setStarted(lobby.isStarted());
        dto.setActive(lobby.isActive());
        return dto;
    }

    private String generateCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int index = (int) (Math.random() * chars.length());
            builder.append(chars.charAt(index));
        }
        return builder.toString();
    }
}
