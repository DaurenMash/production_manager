package com.prodman.workcalendar.service;

import com.prodman.workcalendar.dto.request.SetWorkstationDayStatusRequest;
import com.prodman.workcalendar.dto.response.WorkstationDayStatusResponse;
import com.prodman.workcalendar.model.WorkstationDayStatus;
import com.prodman.workcalendar.repository.WorkstationDayStatusRepository;
import com.prodman.workcalendar.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkstationDayStatusService {

    private final WorkstationDayStatusRepository repository;

    @Transactional(readOnly = true)
    public List<WorkstationDayStatusResponse> listByDate(LocalDate date) {
        UUID tenantId = TenantContext.require();
        return repository.findAllByTenantIdAndDate(tenantId, date).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public WorkstationDayStatusResponse setStatus(SetWorkstationDayStatusRequest req) {
        UUID tenantId = TenantContext.require();
        WorkstationDayStatus s = repository
                .findByTenantIdAndWorkstationIdAndDate(tenantId, req.getWorkstationId(), req.getDate())
                .orElseGet(() -> WorkstationDayStatus.builder()
                        .tenantId(tenantId)
                        .workstationId(req.getWorkstationId())
                        .date(req.getDate())
                        .build());
        s.setIsWorking(req.getIsWorking());
        s.setNote(req.getNote());
        return toResponse(repository.save(s));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = TenantContext.require();
        WorkstationDayStatus s = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("WorkstationDayStatus not found: " + id));
        repository.delete(s);
    }

    private WorkstationDayStatusResponse toResponse(WorkstationDayStatus s) {
        return WorkstationDayStatusResponse.builder()
                .id(s.getId())
                .workstationId(s.getWorkstationId())
                .date(s.getDate())
                .isWorking(s.getIsWorking())
                .note(s.getNote())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}