package com.prodman.workcalendar.repository;

import com.prodman.workcalendar.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, String> {
    Optional<Schedule> findByDate(LocalDate date);
    
    List<Schedule> findByDateBetween(LocalDate start, LocalDate end);
    
    List<Schedule> findByShiftConfig(String shiftConfig);
}