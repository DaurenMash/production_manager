package com.prodman.employeeservice.controller;

import com.prodman.employeeservice.dto.request.AssignQualificationRequest;
import com.prodman.employeeservice.dto.response.EmployeeQualificationResponse;
import com.prodman.employeeservice.model.EmployeeQualification;
import com.prodman.employeeservice.service.EmployeeQualificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees/{employeeId}/qualifications")
@RequiredArgsConstructor
public class EmployeeQualificationController {

    private final EmployeeQualificationService service;

    @GetMapping
    public ResponseEntity<List<EmployeeQualificationResponse>> list(@PathVariable UUID employeeId) {
        List<EmployeeQualificationResponse> out = service.listForEmployee(employeeId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(out);
    }

    @PostMapping
    public ResponseEntity<EmployeeQualificationResponse> assign(@PathVariable UUID employeeId,
                                                                @Valid @RequestBody AssignQualificationRequest req) {
        EmployeeQualification eq = service.assign(
                employeeId,
                req.getQualificationId(),
                req.getLevel(),
                req.getAssignedBy(),
                req.getNotes()
        );
        return ResponseEntity.ok(toResponse(eq));
    }

    @DeleteMapping("/{qualificationId}")
    public ResponseEntity<Void> revoke(@PathVariable UUID employeeId,
                                       @PathVariable UUID qualificationId) {
        service.revoke(employeeId, qualificationId);
        return ResponseEntity.noContent().build();
    }

    private EmployeeQualificationResponse toResponse(EmployeeQualification eq) {
        return EmployeeQualificationResponse.builder()
                .id(eq.getId())
                .employeeId(eq.getEmployee().getId())
                .qualificationId(eq.getQualification().getId())
                .qualificationCode(eq.getQualification().getCode())
                .qualificationName(eq.getQualification().getName())
                .level(eq.getLevel())
                .assignedAt(eq.getAssignedAt())
                .assignedBy(eq.getAssignedBy())
                .notes(eq.getNotes())
                .createdAt(eq.getCreatedAt())
                .updatedAt(eq.getUpdatedAt())
                .build();
    }
}