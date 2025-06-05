package com.syos.domain.valueobjects;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserId Value Object Tests")
class UserIdTest {

    @Test
    @DisplayName("Should create user id with valid value")
    void should_create_user_id_with_valid_value() {
        UserId userId = new UserId(1L);
        assertEquals(1L, userId.getValue());
    }

    @Test
    @DisplayName("Should throw exception for invalid user id")
    void should_throw_exception_for_invalid_user_id() {
        assertThrows(IllegalArgumentException.class, () -> new UserId(null));
        assertThrows(IllegalArgumentException.class, () -> new UserId(0L));
        assertThrows(IllegalArgumentException.class, () -> new UserId(-1L));
    }
}