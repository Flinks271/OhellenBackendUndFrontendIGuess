package de.ohellen.demo.api.auth;

import de.ohellen.demo.api.auth.dto.LoginRequest;
import de.ohellen.demo.api.auth.dto.LoginResponse;
import de.ohellen.demo.api.auth.dto.RegisterRequest;
import de.ohellen.demo.api.auth.dto.RoleAssignRequest;
import de.ohellen.demo.application.auth.RefreshSessionService;
import de.ohellen.demo.application.user.UserService;
import de.ohellen.demo.domain.user.Role;
import de.ohellen.demo.domain.user.User;
import de.ohellen.demo.infrastructure.persistence.RoleRepository;
import de.ohellen.demo.infrastructure.security.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
@RequestMapping("/auth")
public class AuthorizationController {

    private static final long ACCESS_TOKEN_TTL_MS = 15L * 60 * 1000;

    private final UserService userService;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshSessionService refreshSessionService;

    public AuthorizationController(UserService userService,
                                  RoleRepository roleRepository,
                                  AuthenticationManager authenticationManager,
                                  JwtUtil jwtUtil,
                                  RefreshSessionService refreshSessionService) {
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshSessionService = refreshSessionService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest req) {
        User u = userService.register(req.getName(), req.getEmail(), req.getPassword());
        return ResponseEntity.ok(u);
    }

    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req, HttpServletResponse response) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        User authenticatedUser = userService.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + req.getEmail()));

        Map<String, Object> claims = new HashMap<>();
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            List<String> roles = ud.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .collect(Collectors.toList());
            claims.put("roles", roles);
        }

        String accessToken = jwtUtil.generateToken(req.getEmail(), claims, ACCESS_TOKEN_TTL_MS);
        String refreshToken = refreshSessionService.createForUser(authenticatedUser.getUserId());

        response.addHeader(HttpHeaders.SET_COOKIE, createRefreshCookie(refreshToken, 7 * 24 * 60 * 60).toString());
        return ResponseEntity.ok(new LoginResponse(accessToken, refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken,
                                                HttpServletResponse response) {
        if (refreshToken == null || !refreshSessionService.isValid(refreshToken)) {
            return ResponseEntity.status(401).build();
        }

        return refreshSessionService.findUserByRefreshToken(refreshToken)
                .map(user -> {
                    Map<String, Object> claims = new HashMap<>();
                    claims.put("roles", user.getAuthorities().stream()
                            .map(grantedAuthority -> grantedAuthority.getAuthority())
                            .collect(Collectors.toList()));
                    String newAccessToken = jwtUtil.generateToken(user.getEmail(), claims, ACCESS_TOKEN_TTL_MS);
                    String rotatedRefreshToken = refreshSessionService.rotate(refreshToken);
                    response.addHeader(HttpHeaders.SET_COOKIE, createRefreshCookie(rotatedRefreshToken, 7 * 24 * 60 * 60).toString());
                    return ResponseEntity.ok(new LoginResponse(newAccessToken, rotatedRefreshToken));
                })
                .orElse(ResponseEntity.status(401).build());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = "refreshToken", required = false) String refreshToken,
                                      HttpServletResponse response) {
        if (refreshToken != null) {
            refreshSessionService.revoke(refreshToken);
        }
        response.addHeader(HttpHeaders.SET_COOKIE, createRefreshCookie("", 0).toString());
        return ResponseEntity.ok().build();
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

    private ResponseCookie createRefreshCookie(String value, int maxAgeSeconds) {
        return ResponseCookie.from("refreshToken", value)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("Lax")
                .build();
    }

}
