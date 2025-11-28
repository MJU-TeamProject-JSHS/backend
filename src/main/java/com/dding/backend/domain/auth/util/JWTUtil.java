package com.dding.backend.domain.auth.util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JWTUtil {
    private SecretKey secretKey;

    public JWTUtil(@Value("${spring.jwt.secret.key}") String secret) {
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public String getUsername(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("username", String.class);
    }

    public String getRole(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    public Boolean isExpired(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }

    public String createJwt(String kakaoId, String role, Long expiredMs) {

        return Jwts.builder()
                .claim("kakaoId", kakaoId)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            // 1. 토큰 파싱 시도 (서명 검증, 구조 검증, 만료일 검증을 한 번에 수행)
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);

            // 예외가 발생하지 않았다면, 토큰은 유효함
            return true;

        } catch (ExpiredJwtException e) {
            // 토큰이 만료되었을 때
            System.err.println("JWT 토큰 만료: " + e.getMessage());
            return false;
        } catch (JwtException e) {
            // 서명이 유효하지 않거나, 구조적 문제 등 다른 JWT 관련 문제가 발생했을 때
            System.err.println("유효하지 않은 JWT 토큰: " + e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            // 토큰 문자열이 null이거나 비어있을 때
            System.err.println("JWT 토큰이 비어있거나 형식 오류: " + e.getMessage());
            return false;
        }
    }
}
