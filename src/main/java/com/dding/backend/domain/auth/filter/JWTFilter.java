package com.dding.backend.domain.auth.filter;

import com.dding.backend.domain.auth.service.CustomOAuth2User;
import com.dding.backend.domain.auth.util.JWTUtil;
import com.dding.backend.domain.user.dto.UserDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        System.out.println(request);
        String authorization = request.getHeader("Authorization");

        if(authorization == null || !authorization.startsWith("Bearer ")) {
            System.out.println("hi");
            System.out.println("token null");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring("Bearer ".length());
        if(jwtUtil.isExpired(token)) {
            System.out.println("token expired");
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰에서 username과 role 획득
        // JWT의 username은 우리 서비스의 고유 ID (예: "kakao 12345")입니다.
        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);

        // UserDTO를 생성하여 값 설정
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(username);
        userDTO.setRole(role);

        // CustomOAuth2User에 토큰에서 획득한 회원 정보 담기
        // UserDetails로 Authentication 객체를 만들어야 함. CustomOAuth2User를 UserDetails로 사용
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(userDTO, Map.of());

        // 스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(userDTO, null, customOAuth2User.getAuthorities());

        System.out.println("============ authToken ============");
        System.out.println(authToken); // 첫 번째 코드의 토큰 출력 로직
        System.out.println("===========================================");

        // 세션에 사용자 등록(이제 이 요청 동안에는 이 사용자가 인증된 것으로 간주됨)
        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);
    }
}
