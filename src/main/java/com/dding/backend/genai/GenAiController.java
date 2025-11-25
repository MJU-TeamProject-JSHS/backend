package com.dding.backend.genai;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api/genai")
@RequiredArgsConstructor // Lombok annotation
public class GenAiController {

    private final GeminiService geminiService;

    @PostMapping("/questions")
    public ResponseEntity<?> generateQuestions(
            @RequestPart("file1") MultipartFile file1,
            @RequestPart("file2") MultipartFile file2,
            @RequestParam("type") QuestionType type
    ) throws IOException {

        // PDF 파일 유효성 검증
        if (file1.isEmpty() || file2.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "두 개의 PDF 파일을 모두 업로드해야 합니다."));
        }

        if (!file1.getContentType().equals("application/pdf") ||
                !file2.getContentType().equals("application/pdf")) {
            return ResponseEntity.badRequest().body(Map.of("error", "PDF 파일만 업로드할 수 있습니다."));
        }

        // 임시 파일 저장
        Path tmp1 = Files.createTempFile("upload_", "_" + file1.getOriginalFilename());
        Path tmp2 = Files.createTempFile("upload_", "_" + file2.getOriginalFilename());
        file1.transferTo(tmp1.toFile());
        file2.transferTo(tmp2.toFile());

        // Gemini 호출
        Map<String, Object> result = geminiService.generateExamQuestions(tmp1.toFile(), tmp2.toFile(), type);

        // 응답 반환
        return ResponseEntity.ok(result);
    }
}
