package com.dding.backend.domain.post.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostListResponseDto {
    private List<PostResponseDto> postList;
}
