package com.prodman.workcalendar.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = true)
    private String email;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "employee_positions", joinColumns = @JoinColumn(name = "employee_id"))
    @Column(name = "position")
    private List<String> positions = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private EmployeeStatus status = EmployeeStatus.AVAILABLE;

    @Column(nullable = true)
    private String phoneNumber;
    @Column(nullable = true)
    private String department;

    @Column(name = "max_hours_per_week")
    private Integer maxHoursPerWeek = 40;

    @Column(name = "preferred_shift")
    private String preferredShift; // MORNING, EVENING, NIGHT

    @Column(name = "vacation_start")
    private LocalDate vacationStart;

    @Column(name = "vacation_end")
    private LocalDate vacationEnd;

    @Column(name = "sick_leave_start")
    private LocalDate sickLeaveStart;

    @Column(name = "sick_leave_end")
    private LocalDate sickLeaveEnd;
}