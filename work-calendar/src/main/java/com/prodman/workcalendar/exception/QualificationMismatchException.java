package com.prodman.workcalendar.exception;

import java.util.UUID;

public class QualificationMismatchException extends RuntimeException {

    private final UUID requiredQualificationId;

    public QualificationMismatchException(UUID requiredQualificationId) {
        super("Employee does not have required qualification: " + requiredQualificationId);
        this.requiredQualificationId = requiredQualificationId;
    }

    public UUID getRequiredQualificationId() {
        return requiredQualificationId;
    }
}