package com.prodman.workstation.controller;

import com.prodman.workstation.dto.request.CreateWorkstationRequest;
import com.prodman.workstation.dto.response.WorkstationResponse;
import com.prodman.workstation.service.WorkstationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workstations")
@RequiredArgsConstructor
@Tag(name = "Workstation", description = "Управление рабочими местами")
public class WorkstationController {

    private final WorkstationService workstationService;

    @PostMapping
    @Operation(summary = "Создать рабочее место")
    public ResponseEntity<WorkstationResponse> create(@Valid @RequestBody CreateWorkstationRequest request) {
        return ResponseEntity.ok(workstationService.createWorkstation(request));
    }

    @GetMapping
    @Operation(summary = "Получить все рабочие места")
    public ResponseEntity<List<WorkstationResponse>> getAll() {
        return ResponseEntity.ok(workstationService.getAllWorkstations());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить рабочее место по ID")
    public ResponseEntity<WorkstationResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(workstationService.getWorkstationById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить рабочее место")
    public ResponseEntity<WorkstationResponse> update(
            @PathVariable String id,
            @Valid @RequestBody CreateWorkstationRequest request) {
        return ResponseEntity.ok(workstationService.updateWorkstation(id, request));
    }

    @PostMapping("/{id}/toggle")
    @Operation(summary = "Переключить статус (активно/неактивно)")
    public ResponseEntity<WorkstationResponse> toggleActive(@PathVariable String id) {
        return ResponseEntity.ok(workstationService.toggleActive(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить рабочее место")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        workstationService.deleteWorkstation(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/employees")
    @Operation(summary = "Добавить сотрудников на рабочее место")
    public ResponseEntity<WorkstationResponse> addEmployees(
            @PathVariable String id,
            @RequestBody List<String> employeeIds) {
        return ResponseEntity.ok(workstationService.addEmployees(id, employeeIds));
    }

    @DeleteMapping("/{id}/employees/{employeeId}")
    @Operation(summary = "Удалить сотрудника с рабочего места")
    public ResponseEntity<WorkstationResponse> removeEmployee(
            @PathVariable String id,
            @PathVariable String employeeId) {
        return ResponseEntity.ok(workstationService.removeEmployee(id, employeeId));
    }
}