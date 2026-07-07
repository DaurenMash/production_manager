package com.prodman.test.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTestRequest {
    @NotBlank(message = "Название обязательно")
    private String title;

    private String description;

    @NotBlank(message = "Создатель обязателен")
    private String createdBy;

    @NotEmpty(message = "Вопросы обязательны")
    private List<QuestionDto> questions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionDto {
        @NotBlank(message = "Текст вопроса обязателен")
        private String text;

        @NotEmpty(message = "Варианты ответов обязательны")
        private List<String> options;

        @NotEmpty(message = "Верные ответы обязательны")
        private List<Integer> correctOptions;

        private Boolean multipleChoice = false;
    }
}