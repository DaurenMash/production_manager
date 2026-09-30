package com.prodman.employeeservice.service;

import com.prodman.employeeservice.dto.request.CreateDepartmentRequest;
import com.prodman.employeeservice.dto.request.UpdateDepartmentRequest;
import com.prodman.employeeservice.dto.response.DepartmentResponse;
import com.prodman.employeeservice.model.Department;
import com.prodman.employeeservice.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Transactional
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        if (departmentRepository.existsByName(request.getName())) {
            throw new RuntimeException("Department with name '" + request.getName() + "' already exists");
        }

        Department department = Department.builder()
            .name(request.getName())
            .color(request.getColor())
            .build();

        return toResponse(departmentRepository.save(department));
    }

    @Transactional
    public DepartmentResponse updateDepartment(String id, UpdateDepartmentRequest request) {
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Department not found: " + id));

        if (request.getName() != null && !request.getName().equals(department.getName())) {
            if (departmentRepository.existsByName(request.getName())) {
                throw new RuntimeException("Department with name '" + request.getName() + "' already exists");
            }
            department.setName(request.getName());
        }

        if (request.getColor() != null) {
            department.setColor(request.getColor());
        }

        return toResponse(departmentRepository.save(department));
    }

    @Transactional
    public void deleteDepartment(String id) {
        if (!departmentRepository.existsById(id)) {
            throw new RuntimeException("Department not found: " + id);
        }
        departmentRepository.deleteById(id);
    }

    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public DepartmentResponse getDepartmentById(String id) {
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Department not found: " + id));
        return toResponse(department);
    }

    private DepartmentResponse toResponse(Department department) {
        return DepartmentResponse.builder()
            .id(department.getId())
            .name(department.getName())
            .color(department.getColor())
            .createdAt(department.getCreatedAt())
            .updatedAt(department.getUpdatedAt())
            .build();
    }
}