package com.dding.backend.domain.user.entity;

import com.dding.backend.domain.post.entity.Post;
import jakarta.persistence.*;
import lombok.*;

import javax.management.relation.Role;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(unique = true, nullable = false)
    private String kakaId;

    @Column(nullable = true)
    private String username;

    @Column(nullable = true)
    private String pictureUrl;  // 프로필 사진 URI

    @Column(length = 512)
    private String refreshToken;

    private LocalDateTime refreshTokenExpiresAt;

//    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private String role;

    @OneToMany(mappedBy = "user")
    private List<Post> posts;
}
