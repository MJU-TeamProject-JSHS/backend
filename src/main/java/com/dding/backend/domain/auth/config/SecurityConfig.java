package com.dding.backend.domain.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

public class SecurityConfig {
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService;

    public SecurityConfig(OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService) {
        this.oAuth2UserService = oAuth2UserService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()); // CSRF공격 방어 기능 비활성화
        http.authorizeHttpRequests(config -> config.anyRequest().permitAll()); // 모든 HTTP 요청에 대해 접근을 허용
        http.oauth2Login(oauth2Configurer -> oauth2Configurer // OAuth2로그인 기능 활성화하고 세부 설정
                .loginPage("/login") // 사용자 정의 로그인 페이지 사용
                .successHandler(successHandler()) // 로그인 성공했을 때 successHandler라는 Bean을 실행하도록 지정
                .userInfoEndpoint(userInfo -> userInfo
                        .userService(oAuth2UserService)
                )
        );
        return http.build();
    }

    private AuthenticationSuccessHandler successHandler() {
        return ((request, response, authentication) -> {
            DefaultOAuth2User defaultOAuth2User = (DefaultOAuth2User) authentication.getPrincipal(); // 인증이 완료된 사용자 정보 객체를 가져옴

            String id = defaultOAuth2User.getAttribute("id"); // 사용자 정보 맵에서 카카오가 발급한 고유 값을 가져옴
            String body = """
                    {"id":"%s"}
                    """.formatted(id); // HTTP 응답의 타입을 HTML이 아닌 JSON이라고 클라이언트에게 알림

            // HTTP응답의 헤더 설정
            response.setContentType(MediaType.APPLICATION_JSON_VALUE); // JSON 영식 선언
            response.setCharacterEncoding(StandardCharsets.UTF_8.name()); // UTF-8 인코딩 선언

            PrintWriter writer = response.getWriter(); // 객체에 텍스트를 쓸 수 있는 객체
            writer.print(body); // body를 작성
            writer.flush();
        });
    }
}
