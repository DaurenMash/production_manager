package com.prodman.userservice.service;

import com.prodman.userservice.dto.response.TenantResponse;
import com.prodman.userservice.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;

    @Transactional(readOnly = true)
    public Page<TenantResponse> listAll(Pageable pageable) {
        return tenantRepository.findAll(pageable)
                .map(t -> TenantResponse.builder()
                        .id(t.getId())
                        .name(t.getName())
                        .slug(t.getSlug())
                        .status(t.getStatus())
                        .plan(t.getPlan())
                        .trialEndsAt(t.getTrialEndsAt())
                        .subscriptionEndsAt(t.getSubscriptionEndsAt())
                        .createdAt(t.getCreatedAt())
                        .build()
                );
    }
}