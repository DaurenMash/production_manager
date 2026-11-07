package com.prodman.userservice.model;

public enum Role {
    PLATFORM_ADMIN,
    ADMIN,
    HEAD_OF_DEPARTMENT,
    TENANT_VISITOR,
    DEPT_VISITOR,
    VISITOR,
    ANALYST_1,
    ANALYST_2;

    /** Чем больше — тем выше в иерархии тенанта. PLATFORM_ADMIN вне иерархии (0). */
    public int level() {
        return switch (this) {
            case ADMIN              -> 100;
            case HEAD_OF_DEPARTMENT -> 80;
            case TENANT_VISITOR     -> 60;
            case DEPT_VISITOR       -> 40;
            case VISITOR            -> 20;
            case PLATFORM_ADMIN, ANALYST_1, ANALYST_2 -> 0;
        };
    }
}