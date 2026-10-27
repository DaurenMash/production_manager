package com.prodman.employeeservice.service;

import com.prodman.employeeservice.dto.request.UpdateTenantSettingsRequest;
import com.prodman.employeeservice.dto.response.TenantSettingsResponse;
import com.prodman.employeeservice.model.MonthCrossingMode;
import com.prodman.employeeservice.model.TenantSettings;
import com.prodman.employeeservice.repository.TenantSettingsRepository;
import com.prodman.employeeservice.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantSettingsService {

    private final TenantSettingsRepository repository;

    @Transactional
    public TenantSettingsResponse get() {
        UUID tenantId = TenantContext.require();
        TenantSettings settings = repository.findById(tenantId)
                .orElseGet(() -> createDefaults(tenantId));
        return toResponse(settings);
    }

    @Transactional
    public TenantSettingsResponse update(UpdateTenantSettingsRequest req) {
        UUID tenantId = TenantContext.require();
        TenantSettings s = repository.findById(tenantId)
                .orElseGet(() -> createDefaults(tenantId));

        s.setMonthCrossingMode(req.getMonthCrossingMode());
        s.setBaseCoefficientName(req.getBaseCoefficientName());
        s.setBaseCoefficientValue(req.getBaseCoefficientValue());
        s.setWeekendCoefficientName(req.getWeekendCoefficientName());
        s.setWeekendCoefficientValue(req.getWeekendCoefficientValue());
        s.setNightCoefficientName(req.getNightCoefficientName());
        s.setNightCoefficientValue(req.getNightCoefficientValue());

        return toResponse(repository.save(s));
    }

    private TenantSettings createDefaults(UUID tenantId) {
        TenantSettings s = TenantSettings.builder()
                .tenantId(tenantId)
                .monthCrossingMode(MonthCrossingMode.KEEP_START_DAY)
                .baseCoefficientName("Базовая смена")
                .baseCoefficientValue(BigDecimal.ONE)
                .weekendCoefficientName("Работа в выходной")
                .weekendCoefficientValue(new BigDecimal("2.00"))
                .nightCoefficientName("Ночная надбавка")
                .nightCoefficientValue(new BigDecimal("1.20"))
                .build();
        return repository.save(s);
    }

    private TenantSettingsResponse toResponse(TenantSettings s) {
        return TenantSettingsResponse.builder()
                .tenantId(s.getTenantId())
                .monthCrossingMode(s.getMonthCrossingMode())
                .baseCoefficientName(s.getBaseCoefficientName())
                .baseCoefficientValue(s.getBaseCoefficientValue())
                .weekendCoefficientName(s.getWeekendCoefficientName())
                .weekendCoefficientValue(s.getWeekendCoefficientValue())
                .nightCoefficientName(s.getNightCoefficientName())
                .nightCoefficientValue(s.getNightCoefficientValue())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}