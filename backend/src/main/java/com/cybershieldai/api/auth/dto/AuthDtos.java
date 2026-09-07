package com.cybershieldai.api.auth.dto;

import com.cybershieldai.api.common.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AuthDtos {
    public record RegisterRequest(
            String organizationName,
            @NotBlank String fullName,
            @Email @NotBlank String email,
            @NotBlank @Size(min = 10) @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
                    message = "Password must include upper, lower, digit, and special character")
            String password,
            String inviteToken
    ) {}

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}

    public record RefreshRequest(String refreshToken) {}

    public record LogoutRequest(String refreshToken) {}

    public record ChangePasswordRequest(@NotBlank String currentPassword,
                                        @NotBlank @Size(min = 10) @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
                                                message = "Password must include upper, lower, digit, and special character")
                                        String newPassword) {}

    public record ForgotPasswordRequest(@Email @NotBlank String email) {}

    public record ResetPasswordRequest(@NotBlank String token,
                                       @NotBlank @Size(min = 10) @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
                                               message = "Password must include upper, lower, digit, and special character")
                                       String newPassword) {}

    public record UserResponse(Long id, String email, String fullName, Role role, String avatarUrl,
                               Long organizationId, String organizationName) {}

    public record TokenResponse(String accessToken, String refreshToken, UserResponse user) {}
}
