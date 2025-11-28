package com.dding.backend.domain.auth.config;

import com.dding.backend.domain.auth.filter.JWTFilter;
import com.dding.backend.domain.auth.handler.OAuth2LoginSuccessHandler;
import com.dding.backend.domain.auth.service.CustomOAuth2UserService;
import com.dding.backend.domain.auth.util.JWTUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final JWTUtil jwtUtil;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // h2-console 사용 및 X-Frame-Options 헤더 비활성화
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));

        // CSRF, 세션 관리 비활성화
        http.csrf(csrf -> csrf.disable());
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 요청별 권한 설정
        http.authorizeHttpRequests(config -> config
                .requestMatchers("/api/auth/refresh", "/h2-console/**", "/oauth2/**").permitAll()
                .anyRequest().authenticated());

        // OAuth2 로그인 설정
        http.oauth2Login(oauth2Configurer -> oauth2Configurer
                .userInfoEndpoint(userInfo -> userInfo
                        .userService(customOAuth2UserService))
                .successHandler(oAuth2LoginSuccessHandler)
        );

        // JWT 필터 추가
        http.addFilterBefore(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        // 인증 예외 처리
        http.exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"인증이 필요합니다.\"}");
                })
        );

        return http.build();
    }
}