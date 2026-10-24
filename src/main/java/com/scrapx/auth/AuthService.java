package com.scrapx.auth;

import com.scrapx.auth.dto.AuthResponse;
import com.scrapx.auth.dto.LoginRequest;
import com.scrapx.auth.dto.RegisterRequest;
import com.scrapx.auth.dto.UserResponse;
import com.scrapx.user.User;
import com.scrapx.user.UserRepository;
import com.scrapx.user.UserRole;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        // 1. Validate User Data
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered: " + request.getEmail());
        }

        // Default to Buyer if role is omitted
        UserRole role = (request.getRole() != null) ? request.getRole() : UserRole.Buyer;

        // 2. Hash Password with BCrypt
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(
                request.getName(),
                request.getEmail(),
                hashedPassword,
                request.getPhoneNumber(),
                role
        );

        // 3. Save User in MySQL
        User savedUser = userRepository.save(user);

        // 4. Generate JWT
        String token = jwtService.generateToken(savedUser);

        // 5. Return Response
        return new AuthResponse(token, savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        // 1. Verify Email and Password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + request.getEmail()));

        // 2. Generate JWT
        String token = jwtService.generateToken(user);

        // 3. Return Response
        return new AuthResponse(token, user);
    }

    public UserResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
        return UserResponse.fromUser(user);
    }
}
