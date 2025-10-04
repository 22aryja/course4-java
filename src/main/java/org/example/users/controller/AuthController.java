package org.example.users.controller;

import org.example.users.model.AppUser;
import org.example.users.repository.AppUserRepository;
import org.example.users.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (appUserRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Username already exists!"));
        }

        AppUser appUser = new AppUser();
        appUser.setUsername(request.getUsername());
        appUser.setPassword(passwordEncoder.encode(request.getPassword()));

        AppUser savedUser = appUserRepository.save(appUser);

        String token = jwtService.generateToken(savedUser.getUsername());

        return ResponseEntity.ok(new AuthResponse(savedUser.getUsername(), token));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            String token = jwtService.generateToken(request.getUsername());

            return ResponseEntity.ok(new AuthResponse(request.getUsername(), token));

        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid credentials"));
        }
    }

    public static class RegisterRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class AuthResponse {
        private String username;
        private String token;

        public AuthResponse(String username, String token) {
            this.username = username;
            this.token = token;
        }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }

    public static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}