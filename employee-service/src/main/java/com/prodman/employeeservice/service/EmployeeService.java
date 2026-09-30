package com.prodman.employeeservice.service;

import com.prodman.employeeservice.dto.request.CreateEmployeeRequest;
import com.prodman.employeeservice.dto.request.UpdateEmployeeRequest;
import com.prodman.employeeservice.dto.response.DepartmentResponse;
import com.prodman.employeeservice.dto.response.EmployeeResponse;
import com.prodman.employeeservice.dto.response.PositionResponse;
import com.prodman.employeeservice.model.Department;
import com.prodman.employeeservice.model.Employee;
import com.prodman.employeeservice.model.EmployeeStatus;
import com.prodman.employeeservice.model.Position;
import com.prodman.employeeservice.repository.DepartmentRepository;
import com.prodman.employeeservice.repository.EmployeeRepository;
import com.prodman.employeeservice.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PositionRepository positionRepository;
    private final DepartmentRepository departmentRepository;

    @Transactional
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Employee with email " + request.getEmail() + " already exists");
        }

        Position position = resolvePosition(request.getPositionId());
        Department department = resolveDepartment(request.getDepartmentId());

        Employee employee = Employee.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .department(department)
                .position(position)
                .status(EmployeeStatus.AVAILABLE)
                .maxConsecutiveHours(request.getMaxConsecutiveHours() != null ? request.getMaxConsecutiveHours() : 12)
                .userId(request.getUserId())
                .build();

        Employee saved = employeeRepository.save(employee);
        return toResponse(saved);
    }

    @Transactional
    public EmployeeResponse updateEmployee(String id, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + id));

        if (request.getFirstName() != null) employee.setFirstName(request.getFirstName());
        if (request.getLastName() != null) employee.setLastName(request.getLastName());
        if (request.getPhoneNumber() != null) employee.setPhoneNumber(request.getPhoneNumber());
        if (request.getStatus() != null) employee.setStatus(request.getStatus());
        if (request.getMaxConsecutiveHours() != null) employee.setMaxConsecutiveHours(request.getMaxConsecutiveHours());

        if (request.getPositionId() != null) {
            employee.setPosition(resolvePosition(request.getPositionId()));
        }

        if (request.getDepartmentId() != null) {
            employee.setDepartment(resolveDepartment(request.getDepartmentId()));
        }

        if (request.getUserId() != null) {
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
                .orElseThrow(() -> new RuntimeException("Employee not found: " + id));
        return toResponse(employee);
    }

    public EmployeeResponse getEmployeeByEmail(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + email));
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

    private Position resolvePosition(String positionId) {
        if (positionId == null || positionId.isBlank()) return null;
        return positionRepository.findById(positionId)
                .orElseThrow(() -> new RuntimeException("Position not found: " + positionId));
    }

    private Department resolveDepartment(String departmentId) {
        if (departmentId == null || departmentId.isBlank()) return null;
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found: " + departmentId));
    }

    private EmployeeResponse toResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .phoneNumber(employee.getPhoneNumber())
                .position(toPositionResponse(employee.getPosition()))
                .department(toDepartmentResponse(employee.getDepartment()))
                .status(employee.getStatus())
                .maxConsecutiveHours(employee.getMaxConsecutiveHours())
                .userId(employee.getUserId())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
    }

    private PositionResponse toPositionResponse(Position position) {
        if (position == null) return null;
        return PositionResponse.builder()
                .id(position.getId())
                .name(position.getName())
                .color(position.getColor())
                .createdAt(position.getCreatedAt())
                .updatedAt(position.getUpdatedAt())
                .build();
    }

    private DepartmentResponse toDepartmentResponse(Department department) {
        if (department == null) return null;
        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .color(department.getColor())
                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt())
                .build();
    }
}