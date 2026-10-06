package com.prodman.workstation.controller;

import com.prodman.workstation.dto.request.CreateWorkstationRequest;
import com.prodman.workstation.dto.request.UpdateWorkstationRequest;
import com.prodman.workstation.dto.response.WorkstationResponse;
import com.prodman.workstation.service.WorkstationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/workstations")
@RequiredArgsConstructor
public class WorkstationController {

    private final WorkstationService service;

    @GetMapping
    public ResponseEntity<Page<WorkstationResponse>> list(
            @RequestParam(required = false) UUID departmentId,
            Pageable pageable) {
        if (departmentId != null) {
            return ResponseEntity.ok(service.listByDepartment(departmentId, pageable));
        }
        return ResponseEntity.ok(service.list(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkstationResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping
    public ResponseEntity<WorkstationResponse> create(@Valid @RequestBody CreateWorkstationRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkstationResponse> update(@PathVariable UUID id,
                                                      @Valid @RequestBody UpdateWorkstationRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}