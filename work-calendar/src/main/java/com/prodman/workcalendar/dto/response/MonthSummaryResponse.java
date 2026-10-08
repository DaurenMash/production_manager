package com.prodman.workcalendar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthSummaryResponse {
    private LocalDate from;
    private LocalDate to;
    private List<DaySummary> days;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DaySummary {
        private LocalDate date;
        /** Всего станков в отделе (или во всём тенанте, если отдел не задан). */
        private int totalWorkstations;
        /** Сколько станков работает в этот день. */
        private int workingWorkstations;
        /** Всего слотов на этот день. */
        private int totalSlots;
        /** Заполнено слотов. */
        private int filledSlots;
        /** Свободных слотов. */
        private int openSlots;
        /** Список названий работающих станков (для UI, если их ≤10). */
        private List<WorkstationRef> workstations;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class WorkstationRef {
            private java.util.UUID id;
            private String code;
            private String name;
        }
    }
}