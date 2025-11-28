package com.dding.backend.domain.post.repository;

import com.dding.backend.domain.post.entity.Post;
import com.dding.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserId(Long userId);

    List<Post> findByUser(User user);

}
