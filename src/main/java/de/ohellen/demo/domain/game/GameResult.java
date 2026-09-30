package de.ohellen.demo.domain.game;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "games")
public class GameResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_id")
    private Long id;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "game_date", nullable = false)
    private LocalDate gameDate = LocalDate.now();

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Transient
    private Long lobbyId;

    public GameResult() {
    }

    @PostLoad
    private void hydrateLobbyId() {
        if (sessionId != null && sessionId.startsWith("lobby-")) {
            this.lobbyId = Long.parseLong(sessionId.substring("lobby-".length()));
        }
    }

    @PrePersist
    @PreUpdate
    private void ensureSessionAlias() {
        if (lobbyId != null && (sessionId == null || sessionId.isBlank())) {
            sessionId = "lobby-" + lobbyId;
        }
        if (lobbyId == null && sessionId != null && sessionId.startsWith("lobby-")) {
            this.lobbyId = Long.parseLong(sessionId.substring("lobby-".length()));
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getLobbyId() { return lobbyId; }
    public void setLobbyId(Long lobbyId) { this.lobbyId = lobbyId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public LocalDate getGameDate() { return gameDate; }
    public void setGameDate(LocalDate gameDate) { this.gameDate = gameDate; }

    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }

    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
}
