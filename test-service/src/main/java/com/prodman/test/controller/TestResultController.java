package com.prodman.test.controller;

import com.prodman.test.dto.request.SubmitTestRequest;
import com.prodman.test.dto.response.TestResultResponse;
import com.prodman.test.service.TestResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/test-results")
@RequiredArgsConstructor
public class TestResultController {

    private final TestResultService testResultService;

    // ⚠️ РАБОТНИК: только сдача теста
    @PostMapping("/submit")
    public ResponseEntity<TestResultResponse> submitTest(@RequestBody SubmitTestRequest request) {
        return ResponseEntity.ok(testResultService.submitTest(request));
    }

    // ⚠️ ТОЛЬКО ДЛЯ АДМИНА (заглушка)
    @GetMapping("/test/{testId}")
    public ResponseEntity<List<TestResultResponse>> getResultsByTest(@PathVariable String testId) {
        // TODO: Проверка роли ADMIN
        return ResponseEntity.ok(testResultService.getResultsByTest(testId));
    }

    // ⚠️ РАБОТНИК: только свои результаты
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<TestResultResponse>> getResultsByEmployee(@PathVariable String employeeId) {
        return ResponseEntity.ok(testResultService.getResultsByEmployee(employeeId));
    }
}