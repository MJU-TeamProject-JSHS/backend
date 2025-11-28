package com.dding.backend.domain.auth.service;

import com.dding.backend.domain.auth.dto.KakaoResponseDto;
import com.dding.backend.domain.auth.dto.KakaoTokenResponseDto;
import com.dding.backend.domain.auth.dto.OAuth2Response;
import com.dding.backend.domain.auth.util.JWTUtil;
import com.dding.backend.domain.user.dto.UserDTO;
import com.dding.backend.domain.user.entity.User;
import com.dding.backend.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
    private final RestTemplate restTemplate;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String kakaoClientSecret;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String kakaoTokenUri;

    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String kakaoUserInfoUri;

    CustomOAuth2UserService(UserRepository userRepository, JWTUtil jwtUtil, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.restTemplate = restTemplate;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();
        OAuth2Response oAuth2Response = new KakaoResponseDto(attributes);

        UserDTO userDTO = processUserLogin(oAuth2Response);

        return new CustomOAuth2User(userDTO, attributes);
    }

    @Transactional
    public UserDTO loginWithKakaoCode(String code) {
        // 1. 인가 코드로 카카오 토큰 받기
        KakaoTokenResponseDto tokenResponse = getKakaoToken(code);

        // 2. 카카오 토큰으로 카카오 사용자 정보 받기
        OAuth2Response oAuth2Response = getKakaoUserInfo(tokenResponse.getAccessToken());

        // 3. 사용자 정보로 로그인 처리 (신규 가입 또는 정보 업데이트) 및 우리 서비스 토큰 발급
        return processUserLogin(oAuth2Response);
    }

    private KakaoTokenResponseDto getKakaoToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("client_secret", kakaoClientSecret);
        params.add("redirect_uri", kakaoRedirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, headers);

        ResponseEntity<KakaoTokenResponseDto> response = restTemplate.exchange(
                kakaoTokenUri,
                HttpMethod.POST,
                kakaoTokenRequest,
                KakaoTokenResponseDto.class
        );

        return response.getBody();
    }

    private OAuth2Response getKakaoUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                kakaoUserInfoUri,
                HttpMethod.POST,
                kakaoUserInfoRequest,
                Map.class
        );

        return new KakaoResponseDto(response.getBody());
    }

    private UserDTO processUserLogin(OAuth2Response oAuth2Response) {
        String kakaoId = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();
        Optional<User> optionalUserEntity = userRepository.findByUsername(kakaoId);

        User userEntity;
        if (optionalUserEntity.isEmpty()) {
            userEntity = new User();
            userEntity.setKakaId(kakaoId);
            userEntity.setUsername(oAuth2Response.getName());
            userEntity.setPictureUrl(oAuth2Response.getPictureUrl());
            userEntity.setRole("ROLE_USER");
        } else {
            userEntity = optionalUserEntity.get();
            userEntity.setUsername(oAuth2Response.getName());
            userEntity.setPictureUrl(oAuth2Response.getPictureUrl());
        }

        userRepository.save(userEntity);

        final Long EXPIRED_MS = 24 * 60 * 60 * 1000L;
        String accessToken = jwtUtil.createJwt(userEntity.getKakaId(), userEntity.getRole(), EXPIRED_MS);
        String refreshToken = UUID.randomUUID().toString();

        final long REFRESH_TOKEN_EXPIRED_DAYS = 7;
        userEntity.setRefreshToken(refreshToken);
        userEntity.setRefreshTokenExpiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRED_DAYS));
        userRepository.save(userEntity);

        UserDTO userDTO = new UserDTO();
        userDTO.setKakaoId(userEntity.getKakaId());
        userDTO.setUsername(userEntity.getUsername());
        userDTO.setAccessToken(accessToken);
        userDTO.setRefreshToken(refreshToken);
        userDTO.setRole(userEntity.getRole());

        return userDTO;
    }
}
