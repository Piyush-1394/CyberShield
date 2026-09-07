package com.cybershieldai.api.auth;

import com.cybershieldai.api.auth.dto.AuthDtos.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    public TokenResponse register(@Valid @RequestBody RegisterRequest req, HttpServletResponse res) {
        TokenResponse tokens = auth.register(req);
        setRefreshCookie(res, tokens.refreshToken());
        return tokens;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req, HttpServletResponse res) {
        TokenResponse tokens = auth.login(req);
        setRefreshCookie(res, tokens.refreshToken());
        return tokens;
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@RequestBody(required = false) RefreshRequest body,
                                 HttpServletRequest req, HttpServletResponse res) {
        String token = body != null && body.refreshToken() != null && !body.refreshToken().isBlank() ? body.refreshToken() : cookie(req);
        TokenResponse tokens = auth.refresh(token);
        setRefreshCookie(res, tokens.refreshToken());
        return tokens;
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody(required = false) LogoutRequest body,
                                       HttpServletRequest req, HttpServletResponse res) {
        String token = body != null && body.refreshToken() != null && !body.refreshToken().isBlank() ? body.refreshToken() : cookie(req);
        auth.logout(token);
        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        res.addCookie(cookie);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public UserResponse me() {
        return auth.me();
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        auth.changePassword(req);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgot(@Valid @RequestBody ForgotPasswordRequest req) {
        auth.forgotPassword(req);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> reset(@Valid @RequestBody ResetPasswordRequest req) {
        auth.resetPassword(req);
        return ResponseEntity.noContent().build();
    }

    private void setRefreshCookie(HttpServletResponse res, String token) {
        Cookie cookie = new Cookie("refreshToken", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) Duration.ofDays(14).toSeconds());
        res.addCookie(cookie);
    }

    private String cookie(HttpServletRequest req) {
        if (req.getCookies() == null) {
            return null;
        }
        for (Cookie c : req.getCookies()) {
            if ("refreshToken".equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }
}
