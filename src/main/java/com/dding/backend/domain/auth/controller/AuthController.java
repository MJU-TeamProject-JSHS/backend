package com.dding.backend.domain.auth.controller;

import com.dding.backend.domain.auth.dto.AccessTokenResponseDto;
import com.dding.backend.domain.auth.dto.KakaoCodeRequestDto;
import com.dding.backend.domain.auth.dto.RefreshTokenRequestDto;
import com.dding.backend.domain.auth.service.CustomOAuth2UserService;
import com.dding.backend.domain.auth.util.JWTUtil;
import com.dding.backend.domain.user.dto.UserDTO;
import com.dding.backend.domain.user.entity.User;
import com.dding.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Long EXPIRED_MS = 24 * 60 * 60 * 1000L;
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequestDto refreshDto) {
        String refreshToken = refreshDto.getRefreshToken();

        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Refresh Token not found"));

        if (user.getRefreshTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Refresh Token has expired");
        }

        String newAccessToken = jwtUtil.createJwt(user.getKakaId(), user.getRole(), EXPIRED_MS);

        return ResponseEntity.ok(new AccessTokenResponseDto(newAccessToken));
    }
}
