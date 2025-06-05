package com.syos.domain.valueobjects;

import com.syos.domain.exceptions.InvalidQuantityException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Quantity Value Object Tests")
class QuantityTest {

    @Test
    @DisplayName("Should create quantity with valid positive value")
    void should_create_quantity_with_valid_positive_value() {
        Quantity qty = new Quantity(100);
        assertEquals(100, qty.getValue());
    }

    @Test
    @DisplayName("Should create quantity with zero value")
    void should_create_quantity_with_zero_value() {
        Quantity qty = new Quantity(0);
        assertEquals(0, qty.getValue());
    }

    @Test
    @DisplayName("Should throw exception for negative quantity")
    void should_throw_exception_for_negative_quantity() {
        assertThrows(InvalidQuantityException.class, () -> new Quantity(-1));
    }

    @ParameterizedTest
    @CsvSource({"100,50,150", "0,100,100", "50,50,100"})
    @DisplayName("Should add quantities correctly")
    void should_add_quantities_correctly(int initial, int add, int expected) {
        Quantity qty = new Quantity(initial);
        Quantity result = qty.add(add);
        assertEquals(expected, result.getValue());
    }

    @Test
    @DisplayName("Should subtract quantities when sufficient")
    void should_subtract_quantities_when_sufficient() {
        Quantity qty = new Quantity(100);
        Quantity result = qty.subtract(30);
        assertEquals(70, result.getValue());
    }

    @Test
    @DisplayName("Should throw exception when subtracting more than available")
    void should_throw_exception_when_subtracting_more_than_available() {
        Quantity qty = new Quantity(50);
        assertThrows(InvalidQuantityException.class, () -> qty.subtract(51));
    }
}
