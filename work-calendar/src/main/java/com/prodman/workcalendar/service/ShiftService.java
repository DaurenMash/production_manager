package com.prodman.workcalendar.service;

import com.prodman.workcalendar.dto.response.ShiftResponse;
import com.prodman.workcalendar.model.Employee;
import com.prodman.workcalendar.model.EmployeeStatus;
import com.prodman.workcalendar.model.Schedule;
import com.prodman.workcalendar.model.Shift;
import com.prodman.workcalendar.model.ShiftType;
import com.prodman.workcalendar.repository.EmployeeRepository;
import com.prodman.workcalendar.repository.ScheduleRepository;
import com.prodman.workcalendar.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final EmployeeRepository employeeRepository;
    private final ScheduleRepository scheduleRepository;

    public ShiftResponse assignShift(String employeeId, String scheduleId, String shiftType) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        // Проверка конфликтов
        if (employee.getStatus() != EmployeeStatus.AVAILABLE) {
            throw new RuntimeException("Employee is not available: " + employee.getStatus().getDisplayName());
        }

        // Проверка на уже назначенную смену
        List<Shift> existing = shiftRepository.findByEmployeeIdAndDateAndAvailableTrue(employeeId, schedule.getDate());
        if (!existing.isEmpty()) {
            throw new RuntimeException("Employee already has a shift on this date");
        }

        Shift shift = Shift.builder()
                .employee(employee)
                .schedule(schedule)
                .date(schedule.getDate())
                .shiftType(ShiftType.valueOf(shiftType))
                .available(true)
                .build();

        return toResponse(shiftRepository.save(shift));
    }

    public void createDefaultShiftsForSchedule(Schedule schedule) {
        List<Employee> availableEmployees = employeeRepository.findByStatus(EmployeeStatus.AVAILABLE);
        if (availableEmployees.isEmpty()) {
            return;
        }

        String config = schedule.getShiftConfig();
        int shiftsPerDay = config.equals("2_SHIFTS_12H") ? 2 : 3;

        for (int i = 0; i < shiftsPerDay; i++) {
            Employee employee = availableEmployees.get(i % availableEmployees.size());
            ShiftType shiftType = getShiftTypeForIndex(i, config);

            Shift shift = Shift.builder()
                    .employee(employee)
                    .schedule(schedule)
                    .date(schedule.getDate())
                    .shiftType(shiftType)
                    .available(true)
                    .build();

            shiftRepository.save(shift);
        }
    }

    private ShiftType getShiftTypeForIndex(int index, String config) {
        if (config.equals("2_SHIFTS_12H")) {
            return index == 0 ? ShiftType.DAY_12 : ShiftType.NIGHT_12;
        } else {
            return switch (index) {
                case 0 -> ShiftType.MORNING;
                case 1 -> ShiftType.EVENING;
                default -> ShiftType.NIGHT;
            };
        }
    }

    public void deleteShiftsForSchedule(String scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        shiftRepository.deleteAll(shiftRepository.findByDate(schedule.getDate()));
    }

    public ShiftResponse updateShiftStatus(String shiftId, boolean available) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found"));
        shift.setAvailable(available);
        return toResponse(shiftRepository.save(shift));
    }

    public List<ShiftResponse> getShiftsByEmployee(String employeeId) {
        return shiftRepository.findByEmployeeId(employeeId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ShiftResponse> getShiftsByEmployeeAndRange(String employeeId, LocalDate start, LocalDate end) {
        return shiftRepository.findByEmployeeIdAndDateBetween(employeeId, start, end).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ShiftResponse> getShiftsByScheduleId(String scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        return shiftRepository.findByDate(schedule.getDate()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<String> findConflictsForDate(LocalDate date) {
        List<String> conflicts = new ArrayList<>();
        List<Shift> shifts = shiftRepository.findByDate(date);

        // Проверка на дублирование сотрудников
        for (int i = 0; i < shifts.size(); i++) {
            for (int j = i + 1; j < shifts.size(); j++) {
                if (shifts.get(i).getEmployee().getId().equals(shifts.get(j).getEmployee().getId())) {
                    conflicts.add("Employee " + shifts.get(i).getEmployee().getFirstName() + " has duplicate shift");
                }
            }
        }

        // Проверка на недоступных сотрудников
        for (Shift shift : shifts) {
            if (shift.getEmployee().getStatus() != EmployeeStatus.AVAILABLE) {
                conflicts.add("Employee " + shift.getEmployee().getFirstName() + " is not available");
            }
        }

        return conflicts;
    }

    public List<ShiftResponse> findConflicts() {
        LocalDate now = LocalDate.now();
        List<Shift> shifts = shiftRepository.findByDateBetween(now, now.plusDays(30));
        List<ShiftResponse> responses = new ArrayList<>();

        for (Shift shift : shifts) {
            if (shift.getEmployee().getStatus() != EmployeeStatus.AVAILABLE) {
                responses.add(toResponse(shift));
            }
        }

        return responses;
    }

    private ShiftResponse toResponse(Shift shift) {
        return ShiftResponse.builder()
                .id(shift.getId())
                .employeeId(shift.getEmployee().getId())
                .employeeName(shift.getEmployee().getFirstName() + " " + shift.getEmployee().getLastName())
                .date(shift.getDate())
                .shiftType(shift.getShiftType().getDisplayName())
                .available(shift.isAvailable())
                .status(shift.getEmployee().getStatus().getDisplayName())
                .build();
    }
}