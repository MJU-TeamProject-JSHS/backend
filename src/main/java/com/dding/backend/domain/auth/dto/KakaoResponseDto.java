package com.dding.backend.domain.auth.dto;

import java.util.Map;

public class KakaoResponseDto implements OAuth2Response{

    private final Map<String, Object> attribute;

    public KakaoResponseDto(Map<String, Object> attribute) {
        this.attribute = attribute;
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return attribute.get("id").toString();
    }

    @Override
    public String getName() {
        Map<String, Object> properties = (Map<String, Object>) attribute.get("properties");
        System.out.println("getName: " + properties.get("nickname"));
        if(properties == null) {
            System.out.println("nickname : null");
            return null;
        }
        return properties.get("nickname").toString();
    }

    @Override
    public String getPictureUrl() {
        Map<String, Object> properties = (Map<String, Object>) attribute.get("properties");
        System.out.println("getPicture: " + properties.get("profile_image"));
        if(properties == null) {
            System.out.println("pictureUrl : null");
            return null;
        }
        return properties.get("profile_image").toString();
    }
}
