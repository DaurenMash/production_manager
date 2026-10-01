package com.prodman.employeeservice.controller;

import com.prodman.employeeservice.dto.request.CreateEmployeeRequest;
import com.prodman.employeeservice.dto.request.UpdateEmployeeRequest;
import com.prodman.employeeservice.dto.response.EmployeeResponse;
import com.prodman.employeeservice.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<Page<EmployeeResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(employeeService.list(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(employeeService.get(id));
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody CreateEmployeeRequest req) {
        return ResponseEntity.ok(employeeService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> update(@PathVariable UUID id,
                                                   @Valid @RequestBody UpdateEmployeeRequest req) {
        return ResponseEntity.ok(employeeService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}