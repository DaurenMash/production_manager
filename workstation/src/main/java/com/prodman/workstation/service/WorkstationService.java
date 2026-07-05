package com.prodman.workstation.service;

import com.prodman.workstation.dto.request.CreateWorkstationRequest;
import com.prodman.workstation.dto.response.WorkstationResponse;
import com.prodman.workstation.model.Workstation;
import com.prodman.workstation.repository.WorkstationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkstationService {

    private final WorkstationRepository workstationRepository;
    private final RestTemplate restTemplate;

    @Value("${user.service.url:http://user-service-app:8081}")
    private String userServiceUrl;

    @Value("${employee.service.url:http://employee-service-app:8084}")
    private String employeeServiceUrl;

    @Transactional
    public WorkstationResponse createWorkstation(CreateWorkstationRequest request) {
        Workstation workstation = Workstation.builder()
                .department(request.getDepartment())
                .title(request.getTitle())
                .createdBy(request.getCreatedBy())
                .isActive(true)
                .employeeIds(request.getEmployeeIds() != null ? request.getEmployeeIds() : new ArrayList<>())
                .build();

        Workstation saved = workstationRepository.save(workstation);
        return toResponse(saved);
    }

    @Transactional
    public WorkstationResponse updateWorkstation(String id, CreateWorkstationRequest request) {
        Workstation workstation = workstationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workstation not found"));

        workstation.setDepartment(request.getDepartment());
        workstation.setTitle(request.getTitle());
        workstation.setEmployeeIds(request.getEmployeeIds() != null ? request.getEmployeeIds() : new ArrayList<>());

        Workstation saved = workstationRepository.save(workstation);
        return toResponse(saved);
    }

    @Transactional
    public WorkstationResponse toggleActive(String id) {
        Workstation workstation = workstationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workstation not found"));

        workstation.setIsActive(!workstation.getIsActive());
        Workstation saved = workstationRepository.save(workstation);
        return toResponse(saved);
    }

    @Transactional
    public void deleteWorkstation(String id) {
        workstationRepository.deleteById(id);
    }

    @Transactional
    public WorkstationResponse addEmployees(String workstationId, List<String> employeeIds) {
        Workstation workstation = workstationRepository.findById(workstationId)
                .orElseThrow(() -> new RuntimeException("Workstation not found"));

        for (String employeeId : employeeIds) {
            if (!workstation.getEmployeeIds().contains(employeeId)) {
                workstation.getEmployeeIds().add(employeeId);
            }
        }

        Workstation saved = workstationRepository.save(workstation);
        return toResponse(saved);
    }

    @Transactional
    public WorkstationResponse removeEmployee(String workstationId, String employeeId) {
        Workstation workstation = workstationRepository.findById(workstationId)
                .orElseThrow(() -> new RuntimeException("Workstation not found"));

        workstation.getEmployeeIds().remove(employeeId);
        Workstation saved = workstationRepository.save(workstation);
        return toResponse(saved);
    }

    public List<WorkstationResponse> getAllWorkstations() {
        return workstationRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public WorkstationResponse getWorkstationById(String id) {
        Workstation workstation = workstationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workstation not found"));
        return toResponse(workstation);
    }

    private WorkstationResponse toResponse(Workstation workstation) {
        // Получаем данные сотрудников из work-calendar-service
        List<WorkstationResponse.EmployeeInfo> employeeInfos = new ArrayList<>();
        for (String employeeId : workstation.getEmployeeIds()) {
            try {
                // Используем эндпоинт для получения сотрудника из work-calendar
                String url = employeeServiceUrl + "/api/v1/employees/" + employeeId;
                WorkstationResponse.EmployeeInfo info = restTemplate.getForObject(url, WorkstationResponse.EmployeeInfo.class);
                if (info != null) {
                    employeeInfos.add(info);
                }
            } catch (Exception e) {
                // Сотрудник не найден — пропускаем
                System.err.println("Employee not found: " + employeeId);
            }
        }

        return WorkstationResponse.builder()
                .id(workstation.getId())
                .department(workstation.getDepartment())
                .title(workstation.getTitle())
                .createdBy(workstation.getCreatedBy())
                .createdAt(workstation.getCreatedAt())
                .isActive(workstation.getIsActive())
                .employeeIds(workstation.getEmployeeIds())
                .employees(employeeInfos)
                .build();
    }
}