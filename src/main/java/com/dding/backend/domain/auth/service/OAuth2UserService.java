package com.dding.backend.domain.auth.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OAuth2UserService extends DefaultOAuth2UserService {
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest); // 카카오 API 서버와 실제로 통신하여 사용자 정보를 가져옴

        // 임시로 모든 사용자를 관리자로 지정함
        List<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER");

        String userNameAttributeName = userRequest.getClientRegistration() // userRequest에서 kakao에 대한 application.yml 내용을 가져옴
                .getProviderDetails() // 설정 중에서 provider부분을 가져옴
                .getUserInfoEndpoint() // provider정보 중에서 사용자 정보를 user-info-uri, user-name-attribute 설정을 가져옴
                .getUserNameAttributeName(); // 설정에서 user-name-attribute에 지정된 id값을 꺼낸다.

        // 권한, 사용자 정보, 고유 식별 키를 리턴
        return new DefaultOAuth2User(authorities, oAuth2User.getAttributes(), userNameAttributeName);
    }
}
