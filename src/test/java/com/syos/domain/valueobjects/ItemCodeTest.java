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

    @Test
    @DisplayName("Should convert lowercase to uppercase")
    void should_convert_lowercase_to_uppercase() {
        ItemCode code = new ItemCode("item001");
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

    @ParameterizedTest
    @ValueSource(strings = {"ABC", "TOOLONGCODE123", "ITEM-001", "ITEM@001", "123"})
    @DisplayName("Should throw exception for invalid code formats")
    void should_throw_exception_for_invalid_formats(String invalidCode) {
        assertThrows(InvalidItemCodeException.class, () -> new ItemCode(invalidCode));
    }

    @Test
    @DisplayName("Should implement equals for same codes")
    void should_implement_equals_for_same_codes() {
        ItemCode code1 = new ItemCode("ITEM001");
        ItemCode code2 = new ItemCode("item001"); // lowercase
        assertEquals(code1, code2);
    }
}