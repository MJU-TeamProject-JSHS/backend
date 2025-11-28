package com.dding.backend.domain.post.entity;

import com.dding.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    private String title;

    private String content;

    private String author;

    private String materials;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
