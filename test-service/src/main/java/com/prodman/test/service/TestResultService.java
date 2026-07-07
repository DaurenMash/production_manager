package com.prodman.test.service;

import com.prodman.test.dto.request.SubmitTestRequest;
import com.prodman.test.dto.response.TestResultResponse;
import com.prodman.test.model.Answer;
import com.prodman.test.model.Question;
import com.prodman.test.model.Test;
import com.prodman.test.model.TestResult;
import com.prodman.test.repository.TestResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestResultService {

    private final TestResultRepository testResultRepository;
    private final TestService testService;

    private static final double PASS_THRESHOLD = 70.0; // 70% для сдачи

    @Transactional
    public TestResultResponse submitTest(SubmitTestRequest request) {
        Test test = testService.getTestEntity(request.getTestId());

        // Получаем правильные ответы
        Map<String, List<Integer>> correctAnswers = new HashMap<>();
        for (Question question : test.getQuestions()) {
            correctAnswers.put(question.getId(), question.getCorrectOptions());
        }

        // Проверяем ответы
        int totalQuestions = test.getQuestions().size();
        int correctCount = 0;

        List<Answer> answers = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> entry : request.getAnswers().entrySet()) {
            String questionId = entry.getKey();
            List<Integer> selected = entry.getValue() != null ? entry.getValue() : new ArrayList<>();
            
            List<Integer> correct = correctAnswers.getOrDefault(questionId, new ArrayList<>());
            
            // Проверка: совпадают ли выбранные ответы с правильными
            boolean isCorrect = selected.size() == correct.size() && 
                selected.containsAll(correct) && correct.containsAll(selected);
            
            if (isCorrect) {
                correctCount++;
            }

            Answer answer = Answer.builder()
                .questionId(questionId)
                .selectedOptions(selected)
                .build();
            answers.add(answer);
        }

        // Вычисляем процент
        double score = totalQuestions > 0 ? (double) correctCount / totalQuestions * 100 : 0.0;
        boolean passed = score >= PASS_THRESHOLD;

        // Сохраняем результат
        TestResult result = TestResult.builder()
            .testId(request.getTestId())
            .employeeId(request.getEmployeeId())
            .employeeName(request.getEmployeeName())
            .score(score)
            .passed(passed)
            .answers(answers)
            .build();

        TestResult saved = testResultRepository.save(result);

        return toResponse(saved, test);
    }

    public List<TestResultResponse> getResultsByTest(String testId) {
        return testResultRepository.findByTestId(testId).stream()
            .map(r -> toResponse(r, testService.getTestEntity(r.getTestId())))
            .collect(Collectors.toList());
    }

    public List<TestResultResponse> getResultsByEmployee(String employeeId) {
        return testResultRepository.findByEmployeeId(employeeId).stream()
            .map(r -> toResponse(r, testService.getTestEntity(r.getTestId())))
            .collect(Collectors.toList());
    }

    private TestResultResponse toResponse(TestResult result, Test test) {
        // Преобразуем ответы в Map
        Map<String, List<Integer>> answersMap = new HashMap<>();
        for (Answer answer : result.getAnswers()) {
            answersMap.put(answer.getQuestionId(), answer.getSelectedOptions());
        }

        return TestResultResponse.builder()
            .id(result.getId())
            .testId(result.getTestId())
            .testTitle(test != null ? test.getTitle() : "Unknown")
            .employeeId(result.getEmployeeId())
            .employeeName(result.getEmployeeName())
            .score(result.getScore())
            .passed(result.getPassed())
            .completedAt(result.getCompletedAt())
            .answers(answersMap)
            .build();
    }
}