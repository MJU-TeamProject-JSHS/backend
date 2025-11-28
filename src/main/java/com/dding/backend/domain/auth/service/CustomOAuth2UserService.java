package com.dding.backend.domain.auth.service;

import com.dding.backend.domain.auth.dto.KakaoResponseDto;
import com.dding.backend.domain.auth.dto.OAuth2Response;
import com.dding.backend.domain.user.dto.UserDTO;
import com.dding.backend.domain.user.entity.User;
import com.dding.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // 1. Spring Security의 기본 서비스를 통해 OAuth2 사용자 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 2. 카카오 응답에 맞춰 사용자 정보 파싱
        OAuth2Response oAuth2Response = new KakaoResponseDto(attributes);

        // 3. 사용자 정보를 바탕으로 우리 DB에서 사용자 조회 또는 신규 생성
        User user = processUserLogin(oAuth2Response);

        // 4. Spring Security의 SecurityContext에 저장할 UserDTO 생성
        UserDTO userDTO = UserDTO.builder()
                .kakaoId(user.getKakaId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();

        // 5. 인증 정보를 담은 CustomOAuth2User 객체 반환
        return new CustomOAuth2User(userDTO, attributes);
    }

    private User processUserLogin(OAuth2Response oAuth2Response) {
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