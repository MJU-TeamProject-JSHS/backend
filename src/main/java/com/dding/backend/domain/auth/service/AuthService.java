package com.dding.backend.domain.auth.service;

import com.dding.backend.domain.auth.dto.AccessTokenResponseDto;
import com.dding.backend.domain.auth.dto.KakaoResponseDto;
import com.dding.backend.domain.auth.dto.OAuth2Response;
import com.dding.backend.domain.auth.util.JWTUtil;
import com.dding.backend.domain.user.entity.User;
import com.dding.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
    private final RestTemplate restTemplate;

    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String kakaoUserInfoUri;

    private static final Long ACCESS_TOKEN_EXPIRED_MS = 24 * 60 * 60 * 1000L; // 24 hours
    private static final long REFRESH_TOKEN_EXPIRED_DAYS = 7; // 7 days

    @Transactional
    public AccessTokenResponseDto kakaoLogin(String accessToken) {

        System.out.println("=============servicce accessToken========");
        System.out.println(accessToken);
        System.out.println("=========================================");
        // 1. 액세스 토큰으로 카카오 사용자 정보 받기
        OAuth2Response oAuth2Response = getKakaoUserInfo(accessToken);

        // 2. 사용자 정보로 우리 서비스의 유저를 만들거나 찾기
        User user = findOrCreateUser(oAuth2Response);

        // 3. 우리 서비스의 JWT 생성
        String newAccessToken = jwtUtil.createJwt(user.getKakaId(), user.getRole(), ACCESS_TOKEN_EXPIRED_MS);
        String newRefreshToken = UUID.randomUUID().toString();
        System.out.println("=============kakao newAccessToken========");
        System.out.println(newAccessToken);
        System.out.println("=========================================");

        // 4. Refresh Token 정보 DB에 업데이트
        user.setRefreshToken(newRefreshToken);
        user.setRefreshTokenExpiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRED_DAYS));
        userRepository.save(user);

        // 5. 최종 DTO로 변환하여 반환
        return AccessTokenResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    private OAuth2Response getKakaoUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);

        // 응답을 Map으로 받고, 수동으로 DTO를 생성하여 attribute 필드를 초기화합니다.
        ResponseEntity<Map> response = restTemplate.exchange(
                kakaoUserInfoUri,
                HttpMethod.POST, // 카카오 사용자 정보 요청은 POST도 지원합니다. 기존 코드를 유지합니다.
                kakaoUserInfoRequest,
                Map.class
        );

        return new KakaoResponseDto(response.getBody());
    }

    private User findOrCreateUser(OAuth2Response oAuth2Response) {
        String kakaoId = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();
        Optional<User> optionalUser = userRepository.findByKakaId(kakaoId);

        User user;
        if (optionalUser.isEmpty()) {
            user = User.builder()
                    .kakaId(kakaoId)
                    .username(oAuth2Response.getName())
                    .pictureUrl(oAuth2Response.getPictureUrl())
                    .role("ROLE_USER")
                    .build();
        } else {
            user = optionalUser.get();
            // 기존 사용자의 정보가 변경되었을 수 있으니 업데이트
            user.setUsername(oAuth2Response.getName());
            user.setPictureUrl(oAuth2Response.getPictureUrl());
        }

        return userRepository.save(user);
    }
}
