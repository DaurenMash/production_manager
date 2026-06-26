package com.prodman.workcalendar.service;

import com.prodman.workcalendar.dto.request.CreateEmployeeRequest;
import com.prodman.workcalendar.dto.response.EmployeeResponse;
import com.prodman.workcalendar.model.Employee;
import com.prodman.workcalendar.model.EmployeeStatus;
import com.prodman.workcalendar.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        Employee employee = Employee.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .positions(request.getPositions())
                .status(EmployeeStatus.valueOf(request.getStatus()))
                .phoneNumber(request.getPhoneNumber())
                .department(request.getDepartment())
                .maxHoursPerWeek(request.getMaxHoursPerWeek())
                .preferredShift(request.getPreferredShift())
                .vacationStart(request.getVacationStart())
                .vacationEnd(request.getVacationEnd())
                .sickLeaveStart(request.getSickLeaveStart())
                .sickLeaveEnd(request.getSickLeaveEnd())
                .build();

        return toResponse(employeeRepository.save(employee));
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

    public EmployeeResponse updateEmployee(String id, CreateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPositions(request.getPositions());
        employee.setStatus(EmployeeStatus.valueOf(request.getStatus()));
        employee.setPhoneNumber(request.getPhoneNumber());
        employee.setDepartment(request.getDepartment());
        employee.setMaxHoursPerWeek(request.getMaxHoursPerWeek());
        employee.setPreferredShift(request.getPreferredShift());
        employee.setVacationStart(request.getVacationStart());
        employee.setVacationEnd(request.getVacationEnd());
        employee.setSickLeaveStart(request.getSickLeaveStart());
        employee.setSickLeaveEnd(request.getSickLeaveEnd());

        return toResponse(employeeRepository.save(employee));
    }

    public void deleteEmployee(String id) {
        employeeRepository.deleteById(id);
    }

    public List<EmployeeResponse> getAvailableEmployees() {
        return employeeRepository.findByStatus(EmployeeStatus.AVAILABLE).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<EmployeeResponse> getEmployeesByPosition(String position) {
        return employeeRepository.findByPositionsContaining(position).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<EmployeeResponse> findConflicts() {
        LocalDate now = LocalDate.now();
        List<Employee> vacation = employeeRepository.findByVacationStartLessThanEqualAndVacationEndGreaterThanEqual(now, now);
        List<Employee> sick = employeeRepository.findBySickLeaveStartLessThanEqualAndSickLeaveEndGreaterThanEqual(now, now);
        
        List<Employee> allConflicts = employeeRepository.findAll().stream()
                .filter(e -> e.getStatus() != EmployeeStatus.AVAILABLE)
                .collect(Collectors.toList());
        
        return allConflicts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private EmployeeResponse toResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .fullName(employee.getFirstName() + " " + employee.getLastName())
                .email(employee.getEmail())
                .positions(employee.getPositions())
                .status(employee.getStatus().name())
                .statusDisplay(employee.getStatus().getDisplayName())
                .phoneNumber(employee.getPhoneNumber())
                .department(employee.getDepartment())
                .maxHoursPerWeek(employee.getMaxHoursPerWeek())
                .preferredShift(employee.getPreferredShift())
                .vacationStart(employee.getVacationStart())
                .vacationEnd(employee.getVacationEnd())
                .sickLeaveStart(employee.getSickLeaveStart())
                .sickLeaveEnd(employee.getSickLeaveEnd())
                .isAvailable(employee.getStatus() == EmployeeStatus.AVAILABLE)
                .build();
    }
}