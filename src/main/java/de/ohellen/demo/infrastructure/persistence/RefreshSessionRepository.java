package de.ohellen.demo.infrastructure.persistence;

import de.ohellen.demo.domain.auth.RefreshSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface RefreshSessionRepository extends JpaRepository<RefreshSession, Long> {

    Optional<RefreshSession> findByToken(String token);

    Optional<RefreshSession> findByTokenAndRevokedFalseAndExpiresAtAfter(String token, Instant now);

    void deleteByUserId(Long userId);
}
