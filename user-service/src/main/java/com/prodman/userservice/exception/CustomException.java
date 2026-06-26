package com.prodman.userservice.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public static class NotFound extends CustomException {
        public NotFound(String message) {
            super(ErrorCode.NOT_FOUND, message);
        }
    }

    public static class BadRequest extends CustomException {
        public BadRequest(String message) {
            super(ErrorCode.BAD_REQUEST, message);
        }
    }

    public static class Conflict extends CustomException {
        public Conflict(String message) {
            super(ErrorCode.CONFLICT, message);
        }
    }

    public static class Unauthorized extends CustomException {
        public Unauthorized(String message) {
            super(ErrorCode.UNAUTHORIZED, message);
        }
    }

    public static class Forbidden extends CustomException {
        public Forbidden(String message) {
            super(ErrorCode.FORBIDDEN, message);
        }
    }
}