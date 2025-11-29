package com.dding.backend.genai;

public enum QuestionType {
    MCQ("mcq.txt"), // 객관식
    NOTE("note.txt"), // 암기노트
    SHORT("short.txt"), // 주관식
    OX("ox.txt"); // OX

    private final String fileName;

    QuestionType(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
