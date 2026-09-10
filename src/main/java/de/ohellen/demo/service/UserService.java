package de.ohellen.demo.service;

import de.ohellen.demo.model.Role;
import de.ohellen.demo.model.User;
import de.ohellen.demo.repository.RoleRepository;
import de.ohellen.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(String name, String email, String rawPassword) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        if (rawPassword != null) {
            u.setPassword(passwordEncoder.encode(rawPassword));
        }
        // assign default ROLE_USER if exists
        roleRepository.findByRoleName("ROLE_USER").ifPresent(r -> u.getRoles().add(r));
        return userRepository.save(u);
    }

    @Transactional
    public User assignRole(Long userId, String roleName) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Role role = roleRepository.findByRoleName(roleName).orElseThrow(() -> new IllegalArgumentException("Role not found"));
        user.getRoles().add(role);
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) { return userRepository.findByEmail(email); }
}
