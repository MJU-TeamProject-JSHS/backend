package com.dding.backend.domain.auth.controller;

import com.dding.backend.domain.auth.dto.AccessTokenResponseDto;
import com.dding.backend.domain.auth.dto.KakaoTokenRequestDto;
import com.dding.backend.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login/kakao")
    public ResponseEntity<AccessTokenResponseDto> kakaoLogin(@RequestBody KakaoTokenRequestDto request) {
        System.out.println("=============kakao controller========");
        System.out.println(request);
        System.out.println("=====================================");
        AccessTokenResponseDto responseDto = authService.kakaoLogin(request.getAccessToken());
        return ResponseEntity.ok(responseDto);
    }
}