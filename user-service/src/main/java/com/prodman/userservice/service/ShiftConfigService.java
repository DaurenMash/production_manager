package com.prodman.userservice.service;

import com.prodman.userservice.dto.request.CreateShiftConfigRequest;
import com.prodman.userservice.dto.response.ShiftConfigResponse;
import com.prodman.userservice.exception.CustomException;
import com.prodman.userservice.model.Shift;
import com.prodman.userservice.model.ShiftConfig;
import com.prodman.userservice.repository.ShiftConfigRepository;
import com.prodman.userservice.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiftConfigService {

    private final ShiftConfigRepository shiftConfigRepository;
    private final ShiftRepository shiftRepository;

    @Transactional
    public ShiftConfigResponse createShiftConfig(CreateShiftConfigRequest request) {
        // Если создается активная конфигурация, деактивируем старую
        if (request.getShiftCount() == 2 || request.getShiftCount() == 3) {
            shiftConfigRepository.findByIsActiveTrue()
                .ifPresent(old -> {
                    old.setIsActive(false);
                    shiftConfigRepository.save(old);
                });
        }

        ShiftConfig config = ShiftConfig.builder()
            .name(request.getName())
            .shiftCount(request.getShiftCount())
            .isActive(true)
            .build();

        List<Shift> shifts = request.getShifts().stream()
            .map(dto -> Shift.builder()
                .name(dto.getName())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .displayOrder(dto.getDisplayOrder())
                .color(dto.getColor())
                .shiftConfig(config)
                .build())
            .collect(Collectors.toList());

        config.setShifts(shifts);
        ShiftConfig saved = shiftConfigRepository.save(config);
        return toResponse(saved);
    }

    public ShiftConfigResponse getActiveConfig() {
        ShiftConfig config = shiftConfigRepository.findByIsActiveTrue()
            .orElseThrow(() -> new CustomException.NotFound("No active shift configuration found"));
        return toResponse(config);
    }

    public List<ShiftConfigResponse> getAllConfigs() {
        return shiftConfigRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public List<Shift> getShiftsForConfig(String configId) {
        return shiftRepository.findByShiftConfigIdOrderByDisplayOrder(configId);
    }

    public List<String> getShiftNamesForConfig(String configId) {
        return getShiftsForConfig(configId).stream()
            .map(Shift::getName)
            .collect(Collectors.toList());
    }

    @Transactional
    public ShiftConfigResponse setActiveConfig(String configId) {
        // Деактивируем все
        shiftConfigRepository.findAll().forEach(c -> {
            c.setIsActive(false);
            shiftConfigRepository.save(c);
        });

        ShiftConfig config = shiftConfigRepository.findById(configId)
            .orElseThrow(() -> new CustomException.NotFound("Config not found"));
        config.setIsActive(true);
        shiftConfigRepository.save(config);
        return toResponse(config);
    }

    private ShiftConfigResponse toResponse(ShiftConfig config) {
        return ShiftConfigResponse.builder()
            .id(config.getId())
            .name(config.getName())
            .shiftCount(config.getShiftCount())
            .isActive(config.getIsActive())
            .shifts(config.getShifts().stream()
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