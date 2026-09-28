package de.ohellen.demo.infrastructure.persistence;

import de.ohellen.demo.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByName(String name);
    Optional<User> findByNameAndEmailIsNullAndPasswordIsNull(String name);
    Optional<User> findByEmailIsNullAndPasswordIsNullAndName(String name);
}
