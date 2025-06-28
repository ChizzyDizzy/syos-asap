package com.syos.domain.valueobjects;

import com.syos.domain.exceptions.InvalidItemCodeException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ItemCode Value Object Tests")
class ItemCodeTest {

    @Test
    @DisplayName("Should create item code with valid format")
    void should_create_item_code_with_valid_format() {
        ItemCode code = new ItemCode("ITEM001");
        assertEquals("ITEM001", code.getValue());
    }


    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   "})
    @DisplayName("Should throw exception for empty or whitespace codes")
    void should_throw_exception_for_empty_codes(String invalidCode) {
        assertThrows(InvalidItemCodeException.class, () -> new ItemCode(invalidCode));
    }

    @Test
    @DisplayName("Should throw exception for null code")
    void should_throw_exception_for_null_code() {
        assertThrows(InvalidItemCodeException.class, () -> new ItemCode(null));
    }



}