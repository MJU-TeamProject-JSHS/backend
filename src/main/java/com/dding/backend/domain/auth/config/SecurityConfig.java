package com.dding.backend.domain.auth.config;

import com.dding.backend.domain.auth.filter.JWTFilter;
import com.dding.backend.domain.auth.util.JWTUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JWTUtil jwtUtil;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // CORS 설정
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // h2-console 사용 및 X-Frame-Options 헤더 비활성화
        http.headers(headersConfig -> headersConfig.frameOptions(frameOptionsConfig -> frameOptionsConfig.disable()));

        // CSRF, 세션 관리 비활성화
        http.csrf(csrf -> csrf.disable());
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 요청별 권한 설정
        http.authorizeHttpRequests(config -> config
                .requestMatchers(HttpMethod.POST, "/auth/login/kakao").permitAll()
                .requestMatchers("/api/auth/refresh", "/h2-console/**").permitAll()
                .anyRequest().authenticated());

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

    // CORS 설정 Bean
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 프론트엔드 서버의 주소를 여기에 추가
        // Expo Go와 같은 모바일 환경에서는 특정 Origin이 없을 수 있으므로 와일드카드(*)를 사용하거나,
        // 또는 Expo가 사용하는 auth.expo.io 등을 명시적으로 추가해야 할 수 있습니다.
        configuration.setAllowedOrigins(Collections.singletonList("*")); // 우선 모든 Origin 허용
        configuration.setAllowedMethods(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setMaxAge(3600L);

        configuration.addExposedHeader("Authorization");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}