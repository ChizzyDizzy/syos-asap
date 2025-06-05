package com.syos.domain.valueobjects;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BillNumber Value Object Tests")
class BillNumberTest {

    @Test
    @DisplayName("Should create bill number with valid positive value")
    void should_create_bill_number_with_valid_positive_value() {
        BillNumber billNumber = new BillNumber(1001);
        assertEquals(1001, billNumber.getValue());
    }

    @Test
    @DisplayName("Should throw exception for zero or negative bill number")
    void should_throw_exception_for_zero_or_negative_bill_number() {
        assertThrows(IllegalArgumentException.class, () -> new BillNumber(0));
        assertThrows(IllegalArgumentException.class, () -> new BillNumber(-1));
    }

    @Test
    @DisplayName("Should format bill number as string with padding")
    void should_format_bill_number_as_string_with_padding() {
        BillNumber billNumber = new BillNumber(123);
        assertEquals("BILL-000123", billNumber.toString());
    }
}