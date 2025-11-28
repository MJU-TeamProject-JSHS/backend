package com.dding.backend.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String kakaoId;
    private String username;
    private String role;
    private String accessToken;
    private String refreshToken;
    private String pictureUrl;
}
