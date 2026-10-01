package com.prodman.employeeservice.controller;

import com.prodman.employeeservice.dto.request.CreateQualificationRequest;
import com.prodman.employeeservice.dto.request.UpdateQualificationRequest;
import com.prodman.employeeservice.dto.response.QualificationResponse;
import com.prodman.employeeservice.service.QualificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/qualifications")
@RequiredArgsConstructor
public class QualificationController {

    private final QualificationService qualificationService;

    @GetMapping
    public ResponseEntity<Page<QualificationResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(qualificationService.list(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QualificationResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(qualificationService.get(id));
    }

    @PostMapping
    public ResponseEntity<QualificationResponse> create(@Valid @RequestBody CreateQualificationRequest req) {
        return ResponseEntity.ok(qualificationService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QualificationResponse> update(@PathVariable UUID id,
                                                        @Valid @RequestBody UpdateQualificationRequest req) {
        return ResponseEntity.ok(qualificationService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        qualificationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}