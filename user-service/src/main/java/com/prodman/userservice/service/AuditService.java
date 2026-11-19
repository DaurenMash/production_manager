package com.prodman.userservice.service;

import com.prodman.userservice.dto.response.AuditEventResponse;
import com.prodman.userservice.model.AuditEvent;
import com.prodman.userservice.repository.AuditEventRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditEventRepository auditEventRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(UUID tenantId,
                       UUID userId,
                       String username,
                       String action,
                       String target,
                       String ip,
                       String metadata) {
        try {
            AuditEvent event = AuditEvent.builder()
                    .tenantId(tenantId)
                    .userId(userId)
                    .username(username)
                    .action(action)
                    .target(target)
                    .ip(ip)
                    .metadata(metadata)
                    .build();
            auditEventRepository.save(event);
        } catch (Exception e) {
            log.error("Не удалось записать audit event action={} target={}: {}",
                    action, target, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditEventResponse> search(UUID tenantId,
                                           String action,
                                           LocalDateTime from,
                                           LocalDateTime to,
                                           Pageable pageable) {
        Specification<AuditEvent> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (tenantId != null) {
                predicates.add(cb.equal(root.get("tenantId"), tenantId));
            }
            if (action != null && !action.isBlank()) {
                predicates.add(cb.equal(root.get("action"), action));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return auditEventRepository.findAll(spec, pageable)
                .map(e -> AuditEventResponse.builder()
                        .id(e.getId())
                        .tenantId(e.getTenantId())
                        .userId(e.getUserId())
                        .username(e.getUsername())
                        .action(e.getAction())
                        .target(e.getTarget())
                        .ip(e.getIp())
                        .metadata(e.getMetadata())
                        .createdAt(e.getCreatedAt())
                        .build()
                );
    }
}