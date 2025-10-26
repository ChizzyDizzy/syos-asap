package com.syos.web.exception;

/**
 * ============================================
 * CONCURRENCY EXCEPTION
 * ============================================
 *
 * Thrown when a concurrency conflict is detected:
 * - Optimistic locking conflicts (version mismatch)
 * - Lock acquisition failures
 * - Thread interruptions during concurrent operations
 *
 * This is a RuntimeException so it doesn't need to be
 * explicitly declared in method signatures.
 *
 * ============================================
 */
public class ConcurrencyException extends RuntimeException {

    private String conflictType;
    private Object expectedValue;
    private Object actualValue;

    /**
     * Constructor with message only
     */
    public ConcurrencyException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause
     */
    public ConcurrencyException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with detailed conflict information
     */
    public ConcurrencyException(String message, String conflictType,
                                Object expectedValue, Object actualValue) {
        super(message);
        this.conflictType = conflictType;
        this.expectedValue = expectedValue;
        this.actualValue = actualValue;
    }

    /**
     * Get conflict type (e.g., "VERSION_MISMATCH", "LOCK_TIMEOUT")
     */
    public String getConflictType() {
        return conflictType;
    }

    /**
     * Get expected value (e.g., expected version number)
     */
    public Object getExpectedValue() {
        return expectedValue;
    }

    /**
     * Get actual value (e.g., actual version number)
     */
    public Object getActualValue() {
        return actualValue;
    }

    /**
     * Get detailed error message
     */
    @Override
    public String toString() {
        if (conflictType != null) {
            return String.format("ConcurrencyException[type=%s, expected=%s, actual=%s]: %s",
                    conflictType, expectedValue, actualValue, getMessage());
        }
        return super.toString();
    }
}