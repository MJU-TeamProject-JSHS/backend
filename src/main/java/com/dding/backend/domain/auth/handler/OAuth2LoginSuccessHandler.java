package com.dding.backend.domain.auth.handler;

import com.dding.backend.domain.auth.service.CustomOAuth2User;
import com.dding.backend.domain.auth.util.JWTUtil;
import com.dding.backend.domain.user.entity.User;
import com.dding.backend.domain.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private static final Long ACCESS_TOKEN_EXPIRED_MS = 24 * 60 * 60 * 1000L; // 24 hours
    private static final long REFRESH_TOKEN_EXPIRED_DAYS = 7; // 7 days

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 Login successful! Preparing token redirection for mobile app.");

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String kakaoId = oAuth2User.getKakaoId();
        String role = oAuth2User.getRole();

        // Generate our service's tokens
        String accessToken = jwtUtil.createJwt(kakaoId, role, ACCESS_TOKEN_EXPIRED_MS);
        String refreshToken = UUID.randomUUID().toString();

        // Update user's refresh token in the database
        User user = userRepository.findByKakaId(kakaoId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRED_DAYS));
        userRepository.save(user);

        // Create redirect URI with tokens for the mobile app
        String redirectUrl = UriComponentsBuilder.fromUriString("dding-app://login/success")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        // Redirect to the custom scheme URL
        response.sendRedirect(redirectUrl);

        log.info("Redirecting to app with tokens for user: {}", kakaoId);
    }
}
