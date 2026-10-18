package com.prodman.workstation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class SetWorkstationQualificationsRequest {

    /** Полный новый набор. Пустой список = очистить. */
    @NotNull
    private List<UUID> qualificationIds;
}