package com.prodman.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "shifts")
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name; // "Утро", "День", "Ночь"

    @Column(name = "start_time", nullable = false)
    private String startTime; // "08:00"

    @Column(name = "end_time", nullable = false)
    private String endTime; // "20:00"

    @Column(nullable = false)
    private Integer displayOrder; // 1, 2, 3

    @Column(nullable = false)
    private String color; // Цвет для отображения в календаре

    @ManyToOne
    @JoinColumn(name = "shift_config_id")
    private ShiftConfig shiftConfig;
}