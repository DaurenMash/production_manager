package com.prodman.employeeservice.service;

import com.prodman.employeeservice.dto.request.CreateEmployeeRequest;
import com.prodman.employeeservice.dto.request.UpdateEmployeeRequest;
import com.prodman.employeeservice.dto.response.EmployeeResponse;
import com.prodman.employeeservice.model.Employee;
import com.prodman.employeeservice.model.EmployeeStatus;
import com.prodman.employeeservice.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Transactional
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Employee with email " + request.getEmail() + " already exists");
        }

        Employee employee = Employee.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .phoneNumber(request.getPhoneNumber())
            .department(request.getDepartment())
            .position(request.getPosition())
            .status(EmployeeStatus.AVAILABLE)
            .userId(request.getUserId())
            .build();

        Employee saved = employeeRepository.save(employee);
        return toResponse(saved);
    }

    @Transactional
    public EmployeeResponse updateEmployee(String id, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (request.getFirstName() != null) employee.setFirstName(request.getFirstName());
        if (request.getLastName() != null) employee.setLastName(request.getLastName());
        if (request.getPhoneNumber() != null) employee.setPhoneNumber(request.getPhoneNumber());
        if (request.getDepartment() != null) employee.setDepartment(request.getDepartment());
        if (request.getPosition() != null) employee.setPosition(request.getPosition());
        if (request.getStatus() != null) employee.setStatus(request.getStatus());
        if (request.getUserId() != null) {
            // Проверяем, что userId не занят другим сотрудником
            employeeRepository.findByUserId(request.getUserId())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new RuntimeException("User ID already assigned to another employee");
                    }
                });
            employee.setUserId(request.getUserId());
        }

        Employee saved = employeeRepository.save(employee);
        return toResponse(saved);
    }

    @Transactional
    public void deleteEmployee(String id) {
        employeeRepository.deleteById(id);
    }

    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public EmployeeResponse getEmployeeById(String id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee not found"));
        return toResponse(employee);
    }

    public EmployeeResponse getEmployeeByEmail(String email) {
        Employee employee = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Employee not found"));
        return toResponse(employee);
    }

    public EmployeeResponse getEmployeeByUserId(String userId) {
        Employee employee = employeeRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Employee not found for user: " + userId));
        return toResponse(employee);
    }

    public List<EmployeeResponse> getEmployeesByStatus(EmployeeStatus status) {
        return employeeRepository.findByStatus(status).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    private EmployeeResponse toResponse(Employee employee) {
        return EmployeeResponse.builder()
            .id(employee.getId())
            .firstName(employee.getFirstName())
            .lastName(employee.getLastName())
            .email(employee.getEmail())
            .phoneNumber(employee.getPhoneNumber())
            .department(employee.getDepartment())
            .position(employee.getPosition())
            .status(employee.getStatus())
            .userId(employee.getUserId())
            .createdAt(employee.getCreatedAt())
            .updatedAt(employee.getUpdatedAt())
            .build();
    }
}