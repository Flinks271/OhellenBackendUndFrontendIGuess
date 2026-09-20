package de.ohellen.demo;

import de.ohellen.demo.application.auth.RefreshSessionService;
import de.ohellen.demo.domain.user.User;
import de.ohellen.demo.infrastructure.persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AuthenticationFlowTests {

    @Autowired
    private RefreshSessionService refreshSessionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void refreshSessionIsCreatedAndValid() {
        User user = createUser("test-user@example.com", "Password123!");
        user = userRepository.save(user);

        String token = refreshSessionService.createForUser(user.getUserId());

        assertThat(token).isNotBlank();
        assertThat(refreshSessionService.isValid(token)).isTrue();
        assertThat(refreshSessionService.findUserByRefreshToken(token)).isPresent();
    }

    private User createUser(String email, String password) {
        User user = new User();
        user.setName("Test User");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        return user;
    }
}
