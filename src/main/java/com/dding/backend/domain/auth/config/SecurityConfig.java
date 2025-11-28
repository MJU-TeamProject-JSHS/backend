package com.dding.backend.domain.auth.config;

import com.dding.backend.domain.auth.filter.JWTFilter;
import com.dding.backend.domain.auth.service.CustomOAuth2User;
import com.dding.backend.domain.auth.util.JWTUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.NullRequestCache;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService;
    private final JWTUtil jwtUtil;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;

    public SecurityConfig(
            OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService,
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService, JWTUtil jwtUtil) { // 새로 주입
        this.oAuth2UserService = oAuth2UserService;
        this.clientRegistrationRepository = clientRegistrationRepository; // 새로 주입
        this.authorizedClientService = authorizedClientService; // 새로 주입
        this.jwtUtil = jwtUtil;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable())); // h2화면을 보기 위해서 X-Frame-Options 헤더를 제거함
        http.csrf(csrf -> csrf.disable()); // CSRF공격 방어 기능 비활성화
        //세션을 사용하지 않도록 STATELESS로 설정
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // "saved request" 캐시 비활성화.
        http.requestCache(cache -> cache.requestCache(new NullRequestCache()));

        http.authorizeHttpRequests(config -> config
                .requestMatchers("/api/auth/**", "/h2-console/**", "/oauth2/**").permitAll()
                .anyRequest().authenticated());

        http.oauth2Login(oauth2Configurer -> oauth2Configurer // OAuth2로그인 기능 활성화하고 세부 설정
                .successHandler(successHandler()) // 로그인 성공했을 때 successHandler라는 Bean을 실행하도록 지정
                .userInfoEndpoint(userInfo -> userInfo
                        .userService(oAuth2UserService)
                )
        );

        http.addFilterAfter(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        //인증되지 않은 사용자가 보호된 리소스에 접근하려고 할 때 로그인 페이지로 리디렉션하는 대신 401 Unauthorized 상태 코드와 함께 JSON 응답을 반환
        http.exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"인증이 필요합니다.\"}");
                })
        );

        return http.build();
    }

    // 사용자가 카카오 로그인을 성공적으로 마쳤을 때 스프링 시큐리티에 의해 호출된다.
    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return ((request, response, authentication) -> {
            CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();

            String jwtToken = customOAuth2User.getUserDTO().getAccessToken();

            String id = customOAuth2User.getName();
            String refreshToken = customOAuth2User.getUserDTO().getRefreshToken();

            // 4. 응답 본문에 토큰 값을 추가합니다.
            String body = """
                    {"id":"%s", \n"accessToken": "%s", \n"refreshToken": "%s"}
                    """.formatted(id, jwtToken, refreshToken);

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

            PrintWriter writer = response.getWriter();
            writer.print(body);
            writer.flush();

        });
    }
}