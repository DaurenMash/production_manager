package com.prodman.workcalendar.service;

import com.prodman.workcalendar.dto.response.ShiftResponse;
import com.prodman.workcalendar.model.Schedule;
import com.prodman.workcalendar.model.Shift;
import com.prodman.workcalendar.model.ShiftType;
import com.prodman.workcalendar.repository.ScheduleRepository;
import com.prodman.workcalendar.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final ScheduleRepository scheduleRepository;
    private final RestTemplate restTemplate;

    @Value("${employee.service.url:http://employee-service-app:8084}")
    private String employeeServiceUrl;

    // Получение сотрудника из employee-service
    private EmployeeInfo getEmployeeFromService(String employeeId) {
        String url = employeeServiceUrl + "/api/v1/employees/" + employeeId;
        return restTemplate.getForObject(url, EmployeeInfo.class);
    }

    public ShiftResponse assignShift(String employeeId, String scheduleId, String shiftType) {
        // ✅ Получаем сотрудника из employee-service
        EmployeeInfo employee = getEmployeeFromService(employeeId);
        if (employee == null) {
            throw new RuntimeException("Employee not found");
        }

        // Проверка статуса
        if (!"AVAILABLE".equals(employee.getStatus())) {
            throw new RuntimeException("Employee is not available: " + employee.getStatus());
        }

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        // Проверка на уже назначенную смену
        List<Shift> existing = shiftRepository.findByEmployeeIdAndDateAndAvailableTrue(employeeId, schedule.getDate());
        if (!existing.isEmpty()) {
            throw new RuntimeException("Employee already has a shift on this date");
        }

        Shift shift = Shift.builder()
                .employeeId(employeeId)
                .schedule(schedule)
                .date(schedule.getDate())
                .shiftType(ShiftType.valueOf(shiftType))
                .available(true)
                .build();

        return toResponse(shiftRepository.save(shift));
    }

    public void createDefaultShiftsForSchedule(Schedule schedule) {
        // ✅ Получаем список сотрудников из employee-service
        String url = employeeServiceUrl + "/api/v1/employees";
        EmployeeInfo[] employees = restTemplate.getForObject(url, EmployeeInfo[].class);
        if (employees == null || employees.length == 0) {
            return;
        }

        String config = schedule.getShiftConfig();
        int shiftsPerDay = config.equals("2_SHIFTS_12H") ? 2 : 3;

        for (int i = 0; i < shiftsPerDay; i++) {
            EmployeeInfo employee = employees[i % employees.length];
            ShiftType shiftType = getShiftTypeForIndex(i, config);

            Shift shift = Shift.builder()
                    .employeeId(employee.getId())
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
                if (shifts.get(i).getEmployeeId().equals(shifts.get(j).getEmployeeId())) {
                    // ✅ Получаем имя сотрудника из employee-service
                    EmployeeInfo emp = getEmployeeFromService(shifts.get(i).getEmployeeId());
                    String name = emp != null ? emp.getFirstName() : shifts.get(i).getEmployeeId();
                    conflicts.add("Employee " + name + " has duplicate shift");
                }
            }
        }

        // Проверка на недоступных сотрудников
        for (Shift shift : shifts) {
            EmployeeInfo emp = getEmployeeFromService(shift.getEmployeeId());
            if (emp != null && !"AVAILABLE".equals(emp.getStatus())) {
                conflicts.add("Employee " + emp.getFirstName() + " is not available");
            }
        }

        return conflicts;
    }

    public List<ShiftResponse> findConflicts() {
        LocalDate now = LocalDate.now();
        List<Shift> shifts = shiftRepository.findByDateBetween(now, now.plusDays(30));
        List<ShiftResponse> responses = new ArrayList<>();

        for (Shift shift : shifts) {
            EmployeeInfo emp = getEmployeeFromService(shift.getEmployeeId());
            if (emp != null && !"AVAILABLE".equals(emp.getStatus())) {
                responses.add(toResponse(shift));
            }
        }

        return responses;
    }

    private ShiftResponse toResponse(Shift shift) {
        // ✅ Получаем имя сотрудника из employee-service
        String employeeName = shift.getEmployeeId();
        try {
            EmployeeInfo emp = getEmployeeFromService(shift.getEmployeeId());
            if (emp != null) {
                employeeName = emp.getFirstName() + " " + emp.getLastName();
            }
        } catch (Exception e) {
            // Если не удалось получить — используем ID
        }

        return ShiftResponse.builder()
                .id(shift.getId())
                .employeeId(shift.getEmployeeId())
                .employeeName(employeeName)
                .date(shift.getDate())
                .shiftType(shift.getShiftType().getDisplayName())
                .available(shift.isAvailable())
                .status("") // статус можно получить из employee-service
                .build();
    }

    // DTO для получения данных из employee-service
    public static class EmployeeInfo {
        private String id;
        private String firstName;
        private String lastName;
        private String status;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}