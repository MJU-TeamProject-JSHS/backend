package com.dding.backend.domain.auth.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KakaoResponseDto implements OAuth2Response{

    private Map<String, Object> attribute;

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        if(attribute == null) {
            return null;
        }
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
