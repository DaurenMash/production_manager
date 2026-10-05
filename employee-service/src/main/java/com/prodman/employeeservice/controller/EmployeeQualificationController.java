package com.prodman.employeeservice.controller;

import com.prodman.employeeservice.dto.request.AssignQualificationRequest;
import com.prodman.employeeservice.dto.response.EmployeeQualificationResponse;
import com.prodman.employeeservice.service.EmployeeQualificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employees/{employeeId}/qualifications")
@RequiredArgsConstructor
public class EmployeeQualificationController {

    private final EmployeeQualificationService service;

    @GetMapping
    public ResponseEntity<List<EmployeeQualificationResponse>> list(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(service.listForEmployee(employeeId));
    }

    @PostMapping
    public ResponseEntity<EmployeeQualificationResponse> assign(
            @PathVariable UUID employeeId,
            @Valid @RequestBody AssignQualificationRequest req) {
        return ResponseEntity.ok(service.assign(
                employeeId,
                req.getQualificationId(),
                req.getLevel(),
                req.getAssignedBy(),
                req.getNotes()
        ));
    }

    @DeleteMapping("/{qualificationId}")
    public ResponseEntity<Void> revoke(@PathVariable UUID employeeId,
                                       @PathVariable UUID qualificationId) {
        service.revoke(employeeId, qualificationId);
        return ResponseEntity.noContent().build();
    }
}