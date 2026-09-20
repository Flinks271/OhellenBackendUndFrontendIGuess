package de.ohellen.demo.api.auth;

import de.ohellen.demo.api.auth.dto.LoginRequest;
import de.ohellen.demo.api.auth.dto.LoginResponse;
import de.ohellen.demo.api.auth.dto.RefreshRequest;
import de.ohellen.demo.api.auth.dto.RegisterRequest;
import de.ohellen.demo.api.auth.dto.RoleAssignRequest;
import de.ohellen.demo.application.user.UserService;
import de.ohellen.demo.domain.user.Role;
import de.ohellen.demo.domain.user.User;
import de.ohellen.demo.infrastructure.persistence.RoleRepository;
import de.ohellen.demo.infrastructure.security.jwt.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://127.0.0.1:5173"})
@RequestMapping("/auth")
public class AuthorizationController {

    private final UserService userService;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthorizationController(UserService userService, RoleRepository roleRepository, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest req) {
        User u = userService.register(req.getName(), req.getEmail(), req.getPassword());
        return ResponseEntity.ok(u);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        // build claims - include roles
        Map<String, Object> claims = new HashMap<>();
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            List<String> roles = ud.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .collect(Collectors.toList());
            claims.put("roles", roles);
        }
        String token = jwtUtil.generateToken(req.getEmail(), claims);
        // create refresh token (longer expiration)
        long refreshExpMs = 7L * 24 * 60 * 60 * 1000; // 7 days
        Map<String, Object> refreshClaims = new HashMap<>(claims);
        refreshClaims.put("type", "refresh");
        String refreshToken = jwtUtil.generateToken(req.getEmail(), refreshClaims, refreshExpMs);

        return ResponseEntity.ok(new LoginResponse(token, refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@RequestBody RefreshRequest req) {
        String refreshToken = req.getRefreshToken();
        if (!jwtUtil.validateToken(refreshToken)) {
            return ResponseEntity.badRequest().build();
        }
        Claims claims = jwtUtil.parseClaims(refreshToken);
        Object type = claims.get("type");
        if (type == null || !"refresh".equals(type.toString())) {
            return ResponseEntity.badRequest().build();
        }
        String subject = claims.getSubject();
        // extract roles from refresh token to build new access token
        Object rolesObj = claims.get("roles");
        Map<String, Object> newClaims = new HashMap<>();
        if (rolesObj != null) newClaims.put("roles", rolesObj);

        String newAccessToken = jwtUtil.generateToken(subject, newClaims);
        // keep the same refresh token
        return ResponseEntity.ok(new LoginResponse(newAccessToken, refreshToken));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/assign-role")
    public ResponseEntity<User> assignRole(@Valid @RequestBody RoleAssignRequest req) {
        User u = userService.assignRole(req.getUserId(), req.getRoleName());
        return ResponseEntity.ok(u);
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Role>> listRoles() {
        return ResponseEntity.ok(roleRepository.findAll());
    }

    @GetMapping("/me")
    public ResponseEntity<User> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(user);
    }
}
