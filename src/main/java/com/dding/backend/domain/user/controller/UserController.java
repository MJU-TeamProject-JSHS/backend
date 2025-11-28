//package com.dding.backend.user;
//
//import org.springframework.web.bind.annotation.*;
//import java.util.List;
//
//public class UserController {
//    private final UserRepository repo;
//    public UserController(UserRepository repo) {
//        this.repo = repo;
//    }
//
//    @GetMapping
//    public List<User> findAll() {
//        return repo.findAll();
//    }
//
//    @PostMapping
//    public User create(@RequestBody User user) {
//        return repo.save(user);
//    }
//}

package com.dding.backend.domain.user.controller;

import com.dding.backend.domain.auth.service.CustomOAuth2User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
//    @GetMapping("/my-info")
//    public ResponseEntity<Map<String, String>> getMyInfo(@AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
//        if(customOAuth2User == null) {
//            return ResponseEntity.status(401).body(Map.of("message", "Not Authenticated"));
//        }
//
//        Map<String, String> userInfo = new HashMap<>();
//        userInfo.put("name", customOAuth2User.getUserDTO().getUsername());
//        userInfo.put("kakaoId", customOAuth2User.getUserDTO().getKakaoId());
//        userInfo.put("role", customOAuth2User.getUserDTO().getRole());
//
//        return ResponseEntity.ok(userInfo);
//    }
}
