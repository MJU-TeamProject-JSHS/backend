package com.dding.backend.domain.auth.dto;

import lombok.Getter;

public class KakaoDTO {

    @Getter
    public static class OAuthToken{
        private String access_token;
        private String token_type;
        private String reresh_token;
        private int expires_in;
        private String scope;
        private int refresh_token_expires_in;

    }
}
