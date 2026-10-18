package com.prodman.workcalendar.repository;

import com.prodman.workcalendar.model.EmployeeQualificationRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface EmployeeQualificationRefRepository
        extends JpaRepository<EmployeeQualificationRef, UUID> {

    List<EmployeeQualificationRef> findAllByTenantIdAndEmployeeId(UUID tenantId, UUID employeeId);

    List<EmployeeQualificationRef> findAllByTenantId(UUID tenantId);

    void deleteAllByTenantIdAndEmployeeId(UUID tenantId, UUID employeeId);

    /**
     * Идемпотентная вставка связи "сотрудник — квалификация".
     * При конфликте по (tenant_id, employee_id, qualification_id) ничего не делает.
     * Не порождает исключений — безопасно вызывать внутри @Transactional.
     */
    @Modifying
    @Query(value = """
            INSERT INTO employee_qualifications_ref
                (id, tenant_id, employee_id, qualification_id, synced_at)
            VALUES
                (:id, :tenantId, :employeeId, :qualificationId, :syncedAt)
            ON CONFLICT (tenant_id, employee_id, qualification_id) DO NOTHING
            """, nativeQuery = true)
    int upsertIgnoreConflict(@Param("id") UUID id,
                             @Param("tenantId") UUID tenantId,
                             @Param("employeeId") UUID employeeId,
                             @Param("qualificationId") UUID qualificationId,
                             @Param("syncedAt") LocalDateTime syncedAt);
}