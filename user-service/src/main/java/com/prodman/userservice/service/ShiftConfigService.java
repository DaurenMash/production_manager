package com.prodman.userservice.service;

import com.prodman.userservice.dto.request.CreateShiftConfigRequest;
import com.prodman.userservice.dto.response.ShiftConfigResponse;
import com.prodman.userservice.exception.CustomException;
import com.prodman.userservice.model.Shift;
import com.prodman.userservice.model.ShiftConfig;
import com.prodman.userservice.repository.ShiftConfigRepository;
import com.prodman.userservice.repository.ShiftRepository;
import com.prodman.userservice.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiftConfigService {

    private final ShiftConfigRepository shiftConfigRepository;
    private final ShiftRepository shiftRepository;

    @Transactional
    public ShiftConfigResponse createShiftConfig(CreateShiftConfigRequest request) {
        UUID tenantId = TenantContext.require();

        // Если создаём новую активную конфигурацию — деактивируем прежнюю активную у этого тенанта.
        if (Boolean.TRUE.equals(request.getIsActive())) {
            shiftConfigRepository.findFirstByTenantIdAndIsActive(tenantId, true)
                    .ifPresent(old -> {
                        old.setIsActive(false);
                        shiftConfigRepository.save(old);
                    });
        }

        ShiftConfig config = ShiftConfig.builder()
                .tenantId(tenantId)
                .name(request.getName())
                .shiftCount(request.getShiftCount())
                .isActive(request.getIsActive() == null ? Boolean.TRUE : request.getIsActive())
                .build();

        config = shiftConfigRepository.save(config);

        final UUID configId = config.getId();
        List<Shift> shifts = request.getShifts().stream()
                .map(dto -> Shift.builder()
                        .tenantId(tenantId)
                        .shiftConfigId(configId)
                        .name(dto.getName())
                        .startTime(dto.getStartTime())
                        .endTime(dto.getEndTime())
                        .displayOrder(dto.getDisplayOrder())
                        .color(dto.getColor())
                        .build())
                .collect(Collectors.toList());

        shiftRepository.saveAll(shifts);
        config.setShifts(shifts);

        return toResponse(config);
    }

    @Transactional(readOnly = true)
    public ShiftConfigResponse getActiveConfig() {
        UUID tenantId = TenantContext.require();
        ShiftConfig config = shiftConfigRepository.findFirstByTenantIdAndIsActive(tenantId, true)
                .orElseThrow(() -> new CustomException.NotFound("No active shift configuration found"));
        return toResponse(config);
    }

    @Transactional(readOnly = true)
    public List<ShiftConfigResponse> getAllConfigs() {
        UUID tenantId = TenantContext.require();
        return shiftConfigRepository.findAllByTenantId(tenantId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Shift> getShiftsForConfig(UUID configId) {
        UUID tenantId = TenantContext.require();
        // Убедимся, что конфиг наш
        shiftConfigRepository.findByIdAndTenantId(configId, tenantId)
                .orElseThrow(() -> new CustomException.NotFound("Shift config not found: " + configId));
        return shiftRepository.findAllByTenantIdAndShiftConfigId(tenantId, configId);
    }

    @Transactional(readOnly = true)
    public List<String> getShiftNamesForConfig(UUID configId) {
        return getShiftsForConfig(configId).stream()
                .map(Shift::getName)
                .collect(Collectors.toList());
    }

    @Transactional
    public ShiftConfigResponse setActiveConfig(UUID configId) {
        UUID tenantId = TenantContext.require();

        List<ShiftConfig> all = shiftConfigRepository.findAllByTenantId(tenantId);
        all.forEach(c -> c.setIsActive(false));
        shiftConfigRepository.saveAll(all);

        ShiftConfig config = shiftConfigRepository.findByIdAndTenantId(configId, tenantId)
                .orElseThrow(() -> new CustomException.NotFound("Config not found"));
        config.setIsActive(true);
        shiftConfigRepository.save(config);
        return toResponse(config);
    }

    private ShiftConfigResponse toResponse(ShiftConfig config) {
        List<Shift> shifts = shiftRepository
                .findAllByTenantIdAndShiftConfigId(config.getTenantId(), config.getId());

        return ShiftConfigResponse.builder()
                .id(config.getId())
                .name(config.getName())
                .shiftCount(config.getShiftCount())
                .isActive(config.getIsActive())
                .shifts(shifts.stream()
                        .map(s -> ShiftConfigResponse.ShiftDto.builder()
                                .id(s.getId())
                                .name(s.getName())
                                .startTime(s.getStartTime())
                                .endTime(s.getEndTime())
                                .displayOrder(s.getDisplayOrder())
                                .color(s.getColor())
                                .build())
                        .collect(Collectors.toList()))
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }
}