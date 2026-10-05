package com.prodman.userservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "tenants")
public class Tenant {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", updatable = false, nullable = false)
    @ToString.Include
    private UUID id;

    @Column(name = "name", nullable = false)
    @ToString.Include
    private String name;

    /** Короткий идентификатор тенанта, уникален глобально. */
    @Column(name = "slug", nullable = false)
    @ToString.Include
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private TenantStatus status = TenantStatus.TRIAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan", nullable = false)
    @Builder.Default
    private TenantPlan plan = TenantPlan.FREE;

    /** Окончание триала. NULL, если не триал. */
    @Column(name = "trial_ends_at")
    private LocalDateTime trialEndsAt;

    /** Окончание оплаченного периода. NULL, если не оплачено. */
    @Column(name = "subscription_ends_at")
    private LocalDateTime subscriptionEndsAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) status = TenantStatus.TRIAL;
        if (plan == null) plan = TenantPlan.FREE;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}