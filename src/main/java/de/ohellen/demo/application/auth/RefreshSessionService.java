package de.ohellen.demo.application.auth;

import de.ohellen.demo.domain.auth.RefreshSession;
import de.ohellen.demo.domain.user.User;
import de.ohellen.demo.infrastructure.persistence.RefreshSessionRepository;
import de.ohellen.demo.infrastructure.persistence.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Service
public class RefreshSessionService {

    private static final long REFRESH_TOKEN_TTL_SECONDS = 7L * 24 * 60 * 60;
    private final RefreshSessionRepository refreshSessionRepository;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshSessionService(RefreshSessionRepository refreshSessionRepository, UserRepository userRepository) {
        this.refreshSessionRepository = refreshSessionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public String createForUser(Long userId) {
        String token = generateToken();
        Instant expiresAt = Instant.now().plusSeconds(REFRESH_TOKEN_TTL_SECONDS);
        RefreshSession refreshSession = new RefreshSession(token, userId, expiresAt);
        refreshSessionRepository.save(refreshSession);
        return token;
    }

    public Optional<User> findUserByRefreshToken(String token) {
        return refreshSessionRepository.findByTokenAndRevokedFalseAndExpiresAtAfter(token, Instant.now())
                .map(session -> userRepository.findById(session.getUserId()))
                .orElse(Optional.empty());
    }

    @Transactional
    public String rotate(String existingToken) {
        return refreshSessionRepository.findByToken(existingToken)
                .map(session -> {
                    session.setRevoked(true);
                    String newToken = generateToken();
                    session.setToken(newToken);
                    session.setExpiresAt(Instant.now().plusSeconds(REFRESH_TOKEN_TTL_SECONDS));
                    session.setRevoked(false);
                    refreshSessionRepository.save(session);
                    return newToken;
                })
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
    }

    @Transactional
    public void revoke(String token) {
        refreshSessionRepository.findByToken(token).ifPresent(session -> {
            session.setRevoked(true);
            refreshSessionRepository.save(session);
        });
    }

    public boolean isValid(String token) {
        return refreshSessionRepository.findByTokenAndRevokedFalseAndExpiresAtAfter(token, Instant.now()).isPresent();
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
