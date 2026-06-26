package com.prodman.userservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    NOT_FOUND(404, "Resource not found"),
    BAD_REQUEST(400, "Bad request"),
    CONFLICT(409, "Resource already exists"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    INTERNAL_ERROR(500, "Internal server error");

    private final int status;
    private final String defaultMessage;
}