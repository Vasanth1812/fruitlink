package com.fruitlink.common;

/**
 * Domain-level exception for business rule violations.
 * Results in HTTP 422 Unprocessable Entity.
 *
 * Examples:
 *  - Credit limit exceeded
 *  - Order window closed
 *  - Return claim already approved
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
