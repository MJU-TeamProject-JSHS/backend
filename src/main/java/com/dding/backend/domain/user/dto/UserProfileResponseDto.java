package com.dding.backend.domain.user.dto;

import com.dding.backend.domain.post.dto.PostListResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponseDto {
    private Long id;
    private String name;
    private String pictureUrl;
    private List<PostListResponseDto> posts;
}
