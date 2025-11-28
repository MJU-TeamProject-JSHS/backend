package com.dding.backend.domain.post.dto;

import com.dding.backend.domain.post.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostResponseDto {
    private Long postId;
    private String title;
    private String content;
    private String author;
    private String materials;

    public static PostResponseDto fromEntity(Post post) {
        return PostResponseDto.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(post.getAuthor())
                .materials(post.getMaterials())
                .build();
    }
}
