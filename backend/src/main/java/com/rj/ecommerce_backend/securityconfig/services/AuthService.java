package com.rj.ecommerce_backend.securityconfig.services;

import com.rj.ecommerce.api.shared.dto.security.AuthResponse;
import com.rj.ecommerce.api.shared.dto.security.LoginRequest;
import com.rj.ecommerce.api.shared.dto.security.TokenRefreshRequest;
import com.rj.ecommerce_backend.user.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    AuthResponse authenticateUser(LoginRequest loginRequest);
    AuthResponse refreshToken(TokenRefreshRequest tokenRefreshRequest);
    AuthResponse handleEmailUpdate(
            User user,
            String currentPassword,
            HttpServletRequest request,
            HttpServletResponse response);
}
