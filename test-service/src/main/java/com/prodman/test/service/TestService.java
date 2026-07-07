package com.prodman.test.service;

import com.prodman.test.dto.request.CreateTestRequest;
import com.prodman.test.dto.response.TestResponse;
import com.prodman.test.dto.response.QuestionResponse;
import com.prodman.test.model.Question;
import com.prodman.test.model.Test;
import com.prodman.test.repository.TestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestService {

    private final TestRepository testRepository;

    @Transactional
    public TestResponse createTest(CreateTestRequest request) {
        Test test = Test.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .createdBy(request.getCreatedBy())
            .build();

        // Создаем вопросы
        test.setQuestions(request.getQuestions().stream()
            .map(q -> Question.builder()
                .text(q.getText())
                .options(q.getOptions())
                .correctOptions(q.getCorrectOptions())
                .multipleChoice(q.getMultipleChoice() != null ? q.getMultipleChoice() : false)
                .test(test)
                .build())
            .collect(Collectors.toList()));

        Test saved = testRepository.save(test);
        return toResponse(saved);
    }

    public TestResponse getTest(String id) {
        Test test = testRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Test not found"));
        return toResponse(test);
    }

    public Test getTestEntity(String id) {
        return testRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Test not found"));
    }

    private TestResponse toResponse(Test test) {
        return TestResponse.builder()
            .id(test.getId())
            .title(test.getTitle())
            .description(test.getDescription())
            .createdBy(test.getCreatedBy())
            .createdAt(test.getCreatedAt())
            .questions(test.getQuestions().stream()
                .map(q -> QuestionResponse.builder()
                    .id(q.getId())
                    .text(q.getText())
                    .options(q.getOptions())
                    .multipleChoice(q.getMultipleChoice())
                    .build())
                .collect(Collectors.toList()))
            .build();
    }
}