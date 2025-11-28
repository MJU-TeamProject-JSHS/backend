package com.dding.backend.domain.auth.service;

import com.dding.backend.domain.user.dto.UserDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Getter
@Setter
public class CustomOAuth2User implements OAuth2User {

    private final UserDTO userDTO;

    private final Map<String, Object> attributes;
    private String token;

    public CustomOAuth2User(UserDTO userDTO, Map<String, Object> attributes) {
        this.userDTO = userDTO;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {

//                return userDTO.getRole();
                return "ROLE_USER";
            }

        });

        return collection;
    }

    @Override
    public String getName() {

        return userDTO.getKakaoId();
    }
}
