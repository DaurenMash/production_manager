package com.prodman.workstation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "workstations")
public class Workstation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String title;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // ⚠️ ManyToMany через отдельную таблицу (не JPA связь, а просто список ID)
    @ElementCollection
    @CollectionTable(
        name = "workstation_employees",
        joinColumns = @JoinColumn(name = "workstation_id")
    )
    @Column(name = "employee_id")
    @Builder.Default
    private List<String> employeeIds = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}