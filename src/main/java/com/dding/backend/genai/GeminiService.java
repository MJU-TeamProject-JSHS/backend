package com.dding.backend.genai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;


@Service
@RequiredArgsConstructor
public class GeminiService {

    private final Client client;
    private final PromptService promptService;
    private final ObjectMapper objectMapper;

    private String extractJson(String text) {
        if (text == null) return null;

        // 앞뒤 공백 제거
        String trimmed = text.trim();

        // ```로 감싸진 경우 제거
        if (trimmed.startsWith("```")) {
            // 첫 번째 { 와 마지막 } 사이만 잘라서 JSON 추출
            int start = trimmed.indexOf('{');
            int end = trimmed.lastIndexOf('}');
            if (start != -1 && end != -1 && end > start) {
                return trimmed.substring(start, end + 1);
            }
        }

        // 그 외의 경우에도 안전하게 { ... }만 추출
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            return trimmed.substring(start, end + 1);
        }

        // 그래도 못 찾으면 그대로 리턴 (파싱 시 에러 나도록)
        return trimmed;
    }

    public Map<String, Object> generateExamQuestions(File pdf1, File pdf2, QuestionType type) {
        final String model = "gemini-2.5-flash";

        // 프롬프트
        String prompt = promptService.getPrompt(type);

        try {
            // PDF 업로드
            UploadFileConfig uploadConfig = UploadFileConfig.builder()
                    .mimeType("application/pdf")
                    .displayName(pdf1.getName())
                    .build();

            UploadFileConfig uploadConfig2 = UploadFileConfig.builder()
                    .mimeType("application/pdf")
                    .displayName(pdf2.getName())
                    .build();

            com.google.genai.types.File up1 = client.files.upload(pdf1, uploadConfig);
            com.google.genai.types.File up2 = client.files.upload(pdf2, uploadConfig2);

            String uri1 = up1.uri().orElseThrow(() -> new RuntimeException("파일1 업로드 실패"));
            String uri2 = up2.uri().orElseThrow(() -> new RuntimeException("파일2 업로드 실패"));

            // 프롬프트 + 파일을 입력으로 전달
            Content input = Content.builder()
                    .parts(List.of(
                            Part.builder().text(prompt).build(),
                            Part.builder()
                                    .fileData(FileData.builder()
                                            .mimeType("application/pdf")
                                            .fileUri(uri1)
                                            .build())
                                    .build(),
                            Part.builder()
                                    .fileData(FileData.builder()
                                            .mimeType("application/pdf")
                                            .fileUri(uri2)
                                            .build())
                                    .build()
                    ))
                    .build();

            // Gemini 호출
            GenerateContentResponse res = client.models.generateContent(model, input, null);

            // 결과 파싱
            String text = res.text();
            String json = extractJson(text);
            Map<String, Object> jsonResult = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});

            return jsonResult;

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", e.getMessage());
        }
    }
}
