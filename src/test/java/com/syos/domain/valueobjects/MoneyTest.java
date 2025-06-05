package com.syos.domain.valueobjects;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Money Value Object Tests")
class MoneyTest {

    @Test
    @DisplayName("Should create money with valid positive amount")
    void should_create_money_with_valid_positive_amount() {
        Money money = new Money(new BigDecimal("10.50"));
        assertEquals(new BigDecimal("10.50"), money.getValue());
    }

    @Test
    @DisplayName("Should create money with zero amount")
    void should_create_money_with_zero_amount() {
        Money money = new Money(BigDecimal.ZERO);
        assertEquals(new BigDecimal("0.00"), money.getValue());
    }

    @Test
    @DisplayName("Should throw exception for null amount")
    void should_throw_exception_for_null_amount() {
        assertThrows(IllegalArgumentException.class, () -> new Money(null));
    }

    @Test
    @DisplayName("Should round to 2 decimal places using HALF_UP")
    void should_round_to_two_decimal_places() {
        Money money1 = new Money(new BigDecimal("10.555"));
        Money money2 = new Money(new BigDecimal("10.554"));
        assertEquals(new BigDecimal("10.56"), money1.getValue());
        assertEquals(new BigDecimal("10.55"), money2.getValue());
    }

    @ParameterizedTest
    @CsvSource({"10.50,5.25,15.75", "0.00,10.00,10.00", "99.99,0.01,100.00"})
    @DisplayName("Should add money values correctly")
    void should_add_money_values_correctly(String val1, String val2, String expected) {
        Money result = new Money(new BigDecimal(val1)).add(new Money(new BigDecimal(val2)));
        assertEquals(new BigDecimal(expected), result.getValue());
    }

    @Test
    @DisplayName("Should subtract money values and handle negative results")
    void should_subtract_money_values() {
        Money money1 = new Money(new BigDecimal("5.00"));
        Money money2 = new Money(new BigDecimal("10.00"));
        Money result = money1.subtract(money2);
        assertEquals(new BigDecimal("-5.00"), result.getValue());
        assertTrue(result.isNegative());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 5, 10, 100})
    @DisplayName("Should multiply money by quantity correctly")
    void should_multiply_money_by_quantity(int quantity) {
        Money money = new Money(new BigDecimal("10.00"));
        Money result = money.multiply(quantity);
        assertEquals(new BigDecimal(10.00 * quantity).setScale(2), result.getValue());
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void should_implement_equals_and_hashcode() {
        Money money1 = new Money(new BigDecimal("10.50"));
        Money money2 = new Money(new BigDecimal("10.50"));
        Money money3 = new Money(new BigDecimal("10.51"));

        assertEquals(money1, money2);
        assertEquals(money1.hashCode(), money2.hashCode());
        assertNotEquals(money1, money3);
    }
}