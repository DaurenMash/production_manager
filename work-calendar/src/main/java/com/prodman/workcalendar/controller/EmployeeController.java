package com.prodman.workcalendar.controller;

import com.prodman.workcalendar.dto.request.CreateEmployeeRequest;
import com.prodman.workcalendar.dto.response.EmployeeResponse;
import com.prodman.workcalendar.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employees", description = "Управление сотрудниками")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @Operation(summary = "Создать сотрудника")
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        return ResponseEntity.ok(employeeService.createEmployee(request));
    }

    @GetMapping
    @Operation(summary = "Получить всех сотрудников")
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить сотрудника по ID")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable String id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить сотрудника")
    public ResponseEntity<EmployeeResponse> updateEmployee(@PathVariable String id, 
            @Valid @RequestBody CreateEmployeeRequest request) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить сотрудника")
    public ResponseEntity<Void> deleteEmployee(@PathVariable String id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available")
    @Operation(summary = "Получить доступных сотрудников")
    public ResponseEntity<List<EmployeeResponse>> getAvailableEmployees() {
        return ResponseEntity.ok(employeeService.getAvailableEmployees());
    }

    @GetMapping("/by-position/{position}")
    @Operation(summary = "Получить сотрудников по позиции")
    public ResponseEntity<List<EmployeeResponse>> getEmployeesByPosition(@PathVariable String position) {
        return ResponseEntity.ok(employeeService.getEmployeesByPosition(position));
    }

    @GetMapping("/conflicts")
    @Operation(summary = "Найти конфликты (отпуска, больничные)")
    public ResponseEntity<List<EmployeeResponse>> findConflicts() {
        return ResponseEntity.ok(employeeService.findConflicts());
    }
}