package com.prodman.workcalendar.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "equipment_schedules")
public class EquipmentSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String equipmentId;

    @Column(nullable = false)
    private String equipmentName;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "is_working")
    private boolean working = true;

    @Column(name = "shift_type")
    private String shiftType; // MORNING, EVENING, NIGHT, FULL_DAY, OFF

    private String note;

    @Column(name = "maintenance_scheduled")
    private boolean maintenanceScheduled = false;
}