package com.dding.backend.genai;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class PromptService {

    private final ResourceLoader resourceLoader;

    public String getPrompt(QuestionType type) {
        try {
            Resource resource = resourceLoader.getResource("classpath:prompts/" + type.getFileName());

            try (InputStream is = resource.getInputStream()){
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            throw new RuntimeException("프롬프트 로딩 실패", e);
        }
    }
}
