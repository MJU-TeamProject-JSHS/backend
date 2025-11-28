package com.dding.backend.domain.auth.handler;

import com.dding.backend.domain.auth.service.CustomOAuth2User;
import com.dding.backend.domain.auth.util.JWTUtil;
import com.dding.backend.domain.user.entity.User;
import com.dding.backend.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private static final Long ACCESS_TOKEN_EXPIRED_MS = 24 * 60 * 60 * 1000L; // 24 hours
    private static final long REFRESH_TOKEN_EXPIRED_DAYS = 7; // 7 days

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 Login successful!");

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

        // Prepare JSON response with tokens
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        // Set response properties
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // Write tokens to the response body
        response.getWriter().write(objectMapper.writeValueAsString(tokens));

        log.info("JWT Tokens issued for user: {}", kakaoId);
    }
}
