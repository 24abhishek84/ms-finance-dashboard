package com.finance.dashboard.controller;

import com.finance.dashboard.config.JwtTokenProvider;
import com.finance.dashboard.dto.UserRequestDTO;
import com.finance.dashboard.dto.UserResponseDTO;
import com.finance.dashboard.entity.User;
import com.finance.dashboard.exception.AccountLockedException;
import com.finance.dashboard.exception.RateLimitExceededException;
import com.finance.dashboard.service.UserService;
import com.finance.dashboard.service.SecurityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final SecurityService securityService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody UserRequestDTO request) {
        UserResponseDTO user = userService.registerUser(request);
        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully");
        response.put("user", user);
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
        @RequestBody LoginRequest request,
        HttpServletRequest httpRequest) {

        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        try {
            // Step 1: Validate login attempt (rate limit + account lockout)
            securityService.validateLoginAttempt(request.getEmail(), ipAddress);

            // Step 2: Retrieve user
            User user = userService.getUserByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

            // Step 3: Validate password
            if (!userService.validatePassword(request.getPassword(), user.getPassword())) {
                securityService.recordLoginAttempt(
                    request.getEmail(),
                    ipAddress,
                    false,
                    userAgent,
                    "Invalid password"
                );
                throw new IllegalArgumentException("Invalid email or password");
            }

            // Step 4: Check if account is locked (double-check)
            if (user.isLocked()) {
                securityService.recordLoginAttempt(
                    request.getEmail(),
                    ipAddress,
                    false,
                    userAgent,
                    "Account locked"
                );
                throw new AccountLockedException("Account is locked. Contact support.");
            }

            // Step 5: Generate JWT token
            String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

            // Step 6: Record successful login
            securityService.recordLoginAttempt(
                request.getEmail(),
                ipAddress,
                true,
                userAgent,
                null
            );
            securityService.recordSuccessfulLogin(request.getEmail(), ipAddress);

            // Step 7: Return response
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("userId", user.getId());
            response.put("email", user.getEmail());
            response.put("firstName", user.getFirstName());
            response.put("message", "Login successful");

            return ResponseEntity.ok(response);

        } catch (AccountLockedException | RateLimitExceededException e) {
            // These are security exceptions, already logged and handled
            throw e;
        } catch (IllegalArgumentException e) {
            // Generic login failure
            throw e;
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String token) {
        String jwt = token.replace("Bearer ", "");
        return ResponseEntity.ok(jwtTokenProvider.validateToken(jwt));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}

class LoginRequest {
    public String email;
    public String password;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
