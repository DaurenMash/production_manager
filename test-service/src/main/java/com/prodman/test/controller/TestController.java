package com.prodman.test.controller;

import com.prodman.test.dto.request.CreateTestRequest;
import com.prodman.test.dto.response.TestResponse;
import com.prodman.test.service.TestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tests")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    // ⚠️ ТОЛЬКО ДЛЯ АДМИНА (заглушка)
    @PostMapping
    public ResponseEntity<TestResponse> createTest(@Valid @RequestBody CreateTestRequest request) {
        // TODO: Проверка роли ADMIN
        return ResponseEntity.ok(testService.createTest(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestResponse> getTest(@PathVariable String id) {
        return ResponseEntity.ok(testService.getTest(id));
    }
}