package com.prodman.workcalendar.service;

import com.prodman.workcalendar.model.EmployeeRef;
import com.prodman.workcalendar.model.WorkstationRef;
import com.prodman.workcalendar.repository.EmployeeQualificationRefRepository;
import com.prodman.workcalendar.repository.EmployeeRefRepository;
import com.prodman.workcalendar.repository.WorkstationRefRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefSyncService {

    private static final int SYNC_THREADS = 8;

    private final WorkstationRefRepository workstationRefRepo;
    private final EmployeeRefRepository employeeRefRepo;
    private final EmployeeQualificationRefRepository employeeQualificationRefRepo;
    private final PlatformTransactionManager transactionManager;

    @Value("${workstation.service.url:http://workstation-app:8083}")
    private String workstationServiceUrl;

    @Value("${employee.service.url:http://employee-service-app:8084}")
    private String employeeServiceUrl;

    private final RestClient restClient = RestClient.create();

    /**
     * Синхронизация станков тенанта.
     * Ожидаемый формат ответа workstation-service: Page<WorkstationResponse>.
     */
    @Transactional
    public int syncWorkstations(UUID tenantId) {
        try {
            Map<?, ?> page = restClient.get()
                    .uri(workstationServiceUrl + "/api/workstations?size=500")
                    .header("X-Tenant-Id", tenantId.toString())
                    .retrieve()
                    .body(Map.class);

            if (page == null) return 0;
            Object content = page.get("content");
            if (!(content instanceof List<?> list)) return 0;

            workstationRefRepo.deleteAllByTenantId(tenantId);

            int count = 0;
            for (Object raw : list) {
                if (!(raw instanceof Map<?, ?> m)) continue;
                UUID id = UUID.fromString(String.valueOf(m.get("id")));
                WorkstationRef ref = WorkstationRef.builder()
                        .id(id)
                        .tenantId(tenantId)
                        .code((String) m.get("code"))
                        .name((String) m.get("name"))
                        .departmentId(m.get("departmentId") == null ? null
                                : UUID.fromString(String.valueOf(m.get("departmentId"))))
                        .isActive(m.get("isActive") == null || Boolean.TRUE.equals(m.get("isActive")))
                        .build();
                workstationRefRepo.save(ref);
                count++;
            }
            log.info("Synced {} workstations for tenant {}", count, tenantId);
            return count;
        } catch (Exception ex) {
            log.error("Failed to sync workstations: {}", ex.getMessage());
            return 0;
        }
    }

    /**
     * Синхронизация сотрудников тенанта.
     */
    @Transactional
    public int syncEmployees(UUID tenantId) {
        try {
            Map<?, ?> page = restClient.get()
                    .uri(employeeServiceUrl + "/api/employees?size=1000")
                    .header("X-Tenant-Id", tenantId.toString())
                    .retrieve()
                    .body(Map.class);

            if (page == null) return 0;
            Object content = page.get("content");
            if (!(content instanceof List<?> list)) return 0;

            employeeRefRepo.deleteAllByTenantId(tenantId);

            int count = 0;
            for (Object raw : list) {
                if (!(raw instanceof Map<?, ?> m)) continue;
                UUID id = UUID.fromString(String.valueOf(m.get("id")));
                String lastName = (String) m.get("lastName");
                String firstName = (String) m.get("firstName");
                String middleName = (String) m.get("middleName");
                String full = String.join(" ",
                        lastName == null ? "" : lastName,
                        firstName == null ? "" : firstName,
                        middleName == null ? "" : middleName).trim();

                EmployeeRef ref = EmployeeRef.builder()
                        .id(id)
                        .tenantId(tenantId)
                        .fullName(full.isEmpty() ? null : full)
                        .departmentId(m.get("departmentId") == null ? null
                                : UUID.fromString(String.valueOf(m.get("departmentId"))))
                        .isActive(true)
                        .build();
                employeeRefRepo.save(ref);
                count++;
            }
            log.info("Synced {} employees for tenant {}", count, tenantId);
            return count;
        } catch (Exception ex) {
            log.error("Failed to sync employees: {}", ex.getMessage());
            return 0;
        }
    }

    /**
     * Последовательная синхронизация квалификаций (для одного сотрудника или небольшого набора).
     * Используется из ShiftSlotService.assignEmployee, где сотрудник один.
     * Делает ПОЛНЫЙ sync: удаляет старые записи ref и вставляет актуальные из employee-service.
     * Транзакция держится только на запись в БД — HTTP-вызовы идут до неё.
     */
    @Transactional
    public int syncEmployeeQualifications(UUID tenantId) {
        try {
            List<EmployeeRef> employees = employeeRefRepo.findAllByTenantId(tenantId);

            int count = 0;
            for (EmployeeRef emp : employees) {
                try {
                    List<UUID> ids = fetchQualificationIds(emp.getId(), tenantId);
                    if (ids == null) {
                        // null = сервис недоступен / ошибка. Не трогаем ref этого сотрудника,
                        // иначе удалим актуальные данные из-за временного сбоя.
                        continue;
                    }

                    employeeQualificationRefRepo.deleteAllByTenantIdAndEmployeeId(
                            tenantId, emp.getId());

                    LocalDateTime now = LocalDateTime.now();
                    for (UUID qid : ids) {
                        employeeQualificationRefRepo.upsertIgnoreConflict(
                                UUID.randomUUID(), tenantId, emp.getId(), qid, now);
                        count++;
                    }
                } catch (Exception ex) {
                    log.warn("Failed to sync quals for employee {}: {}", emp.getId(), ex.getMessage());
                }
            }
            log.info("Synced {} employee qualifications for tenant {}", count, tenantId);
            return count;
        } catch (Exception ex) {
            log.error("Failed to sync employee qualifications: {}", ex.getMessage());
            return 0;
        }
    }

    /**
     * Параллельная синхронизация квалификаций всех сотрудников тенанта.
     *
     * Схема:
     *  1) Параллельно (CompletableFuture, пул из SYNC_THREADS потоков) — только HTTP-вызовы
     *     в employee-service. Собираем список EmployeeQuals (employeeId + qualificationIds|null).
     *     null — ошибка HTTP; пустой список — у сотрудника нет квалификаций.
     *  2) Последовательно, в одной транзакции (TransactionTemplate) — для каждого сотрудника
     *     удаляем старые связи в ref и вставляем актуальные через upsertIgnoreConflict.
     *     Так удалённые в employee-service квалификации пропадают и из ref.
     *
     * Не помечен @Transactional: HTTP-вызовы не должны держать транзакцию БД открытой.
     * Транзакция открывается только на фазу записи через TransactionTemplate.
     */
    public SyncResult syncEmployeeQualificationsParallel(UUID tenantId) {
        List<EmployeeRef> employees = employeeRefRepo.findAllByTenantId(tenantId);
        if (employees.isEmpty()) {
            log.info("No employees to sync for tenant {}", tenantId);
            return new SyncResult(0, 0, 0);
        }

        ExecutorService pool = Executors.newFixedThreadPool(SYNC_THREADS);
        List<CompletableFuture<EmployeeQuals>> futures;
        try {
            // 1) Параллельно — только HTTP
            futures = employees.stream()
                    .map(emp -> CompletableFuture
                            .supplyAsync(() -> new EmployeeQuals(
                                    emp.getId(),
                                    fetchQualificationIds(emp.getId(), tenantId)), pool)
                            .exceptionally(ex -> {
                                log.warn("Failed to fetch quals for employee {}: {}",
                                        emp.getId(), ex.getMessage());
                                return new EmployeeQuals(emp.getId(), null);
                            }))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            pool.shutdown();
        }

        // 2) Последовательно — запись в БД в одной транзакции
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        SyncResult result = tx.execute(status -> {
            int employeesOk = 0;
            int employeesFailed = 0;
            int totalQuals = 0;
            LocalDateTime now = LocalDateTime.now();

            for (CompletableFuture<EmployeeQuals> f : futures) {
                EmployeeQuals eq = f.join();
                if (eq.qualificationIds() == null) {
                    // HTTP-ошибка: не трогаем ref этого сотрудника
                    employeesFailed++;
                    continue;
                }

                // Полный sync для этого сотрудника: удаляем старые, вставляем актуальные.
                // Если список пустой — просто очищаем ref, призраков не остаётся.
                employeeQualificationRefRepo.deleteAllByTenantIdAndEmployeeId(
                        tenantId, eq.employeeId());

                for (UUID qid : eq.qualificationIds()) {
                    employeeQualificationRefRepo.upsertIgnoreConflict(
                            UUID.randomUUID(), tenantId, eq.employeeId(), qid, now);
                    totalQuals++;
                }
                employeesOk++;
            }

            log.info("Parallel quals sync done for tenant {}: employees ok={}, failed={}, quals={}",
                    tenantId, employeesOk, employeesFailed, totalQuals);
            return new SyncResult(employeesOk, employeesFailed, totalQuals);
        });

        return result != null ? result : new SyncResult(0, 0, 0);
    }

    /**
     * Публичный метод для контроллера «Обновить квалификации».
     * Возвращает статистику по синку.
     */
    public SyncResult refreshQualifications(UUID tenantId) {
        log.info("Manual qualifications refresh requested for tenant {}", tenantId);
        return syncEmployeeQualificationsParallel(tenantId);
    }

    /**
     * Полная синхронизация тенанта.
     * Квалификации — параллельно.
     */
    @Transactional
    public void syncAll(UUID tenantId) {
        syncWorkstations(tenantId);
        syncEmployees(tenantId);
        syncEmployeeQualificationsParallel(tenantId);
    }

    // ========== helpers ==========

    /**
     * Возвращает список qualificationId сотрудника из employee-service.
     * null — если сервис недоступен / вернул ошибку (в отличие от пустого списка).
     */
    private List<UUID> fetchQualificationIds(UUID employeeId, UUID tenantId) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = restClient.get()
                .uri(employeeServiceUrl + "/api/employees/" + employeeId + "/qualifications")
                .header("X-Tenant-Id", tenantId.toString())
                .retrieve()
                .body(List.class);

        if (list == null) return List.of();
        List<UUID> ids = new ArrayList<>(list.size());
        for (Map<String, Object> q : list) {
            Object qid = q.get("qualificationId");
            if (qid != null) ids.add(UUID.fromString(qid.toString()));
        }
        return ids;
    }

    private record EmployeeQuals(UUID employeeId, List<UUID> qualificationIds) {}

    public record SyncResult(int employeesOk, int employeesFailed, int qualificationsSynced) {}
}