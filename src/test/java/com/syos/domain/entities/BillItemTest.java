// BillItemTest.java - 8 tests
package com.syos.domain.entities;

import com.syos.domain.valueobjects.*;
import com.syos.domain.exceptions.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("BillItem Entity Tests")
class BillItemTest {

    private Item mockItem;
    private BillItem billItem;

    @BeforeEach
    void setUp() {
        mockItem = mock(Item.class);
        when(mockItem.getPrice()).thenReturn(new Money(new BigDecimal("10.00")));
    }

    @Test
    @DisplayName("Should create bill item with valid item and quantity")
    void should_create_bill_item_with_valid_item_and_quantity() {
        // Arrange & Act
        billItem = new BillItem(mockItem, 5);

        // Assert
        assertNotNull(billItem);
        assertEquals(mockItem, billItem.getItem());
        assertEquals(5, billItem.getQuantity().getValue());
        assertEquals(new BigDecimal("50.00"), billItem.getTotalPrice().getValue());
    }

    @Test
    @DisplayName("Should throw exception for null item")
    void should_throw_exception_for_null_item() {
        // Act & Assert
        assertThrows(NullPointerException.class, () ->
                new BillItem(null, 1));
    }

    @Test
    @DisplayName("Should throw exception for negative quantity")
    void should_throw_exception_for_negative_quantity() {
        // Act & Assert
        assertThrows(InvalidQuantityException.class, () ->
                new BillItem(mockItem, -1));
    }

    @Test
    @DisplayName("Should handle zero quantity")
    void should_handle_zero_quantity() {
        // Arrange & Act
        billItem = new BillItem(mockItem, 0);

        // Assert
        assertEquals(0, billItem.getQuantity().getValue());
        assertEquals(new BigDecimal("0.00"), billItem.getTotalPrice().getValue());
    }

    @ParameterizedTest
    @CsvSource({
            "1, 10.00, 10.00",
            "5, 15.50, 77.50",
            "10, 25.99, 259.90",
            "3, 100.00, 300.00",
            "7, 12.75, 89.25"
    })
    @DisplayName("Should calculate total price correctly for different quantities and item prices")
    void should_calculate_total_price_correctly(int quantity, String itemPrice, String expectedTotal) {
        // Arrange
        when(mockItem.getPrice()).thenReturn(new Money(new BigDecimal(itemPrice)));

        // Act
        billItem = new BillItem(mockItem, quantity);

        // Assert
        assertEquals(new BigDecimal(expectedTotal), billItem.getTotalPrice().getValue());
    }

    @Test
    @DisplayName("Should return correct item reference")
    void should_return_correct_item_reference() {
        // Arrange
        Item specificItem = mock(Item.class);
        when(specificItem.getPrice()).thenReturn(new Money(new BigDecimal("20.00")));

        // Act
        billItem = new BillItem(specificItem, 2);

        // Assert
        assertSame(specificItem, billItem.getItem());
        assertNotSame(mockItem, billItem.getItem());
    }

    @Test
    @DisplayName("Should return correct quantity as Quantity value object")
    void should_return_correct_quantity_as_quantity_value_object() {
        // Arrange & Act
        billItem = new BillItem(mockItem, 8);

        // Assert
        assertNotNull(billItem.getQuantity());
        assertTrue(billItem.getQuantity() instanceof Quantity);
        assertEquals(8, billItem.getQuantity().getValue());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10, 25, 100})
    @DisplayName("Should maintain consistency between quantity input and quantity getter")
    void should_maintain_consistency_between_quantity_input_and_getter(int inputQuantity) {
        // Arrange & Act
        billItem = new BillItem(mockItem, inputQuantity);

        // Assert
        assertEquals(inputQuantity, billItem.getQuantity().getValue());

        // Verify total price calculation consistency
        Money expectedTotal = mockItem.getPrice().multiply(inputQuantity);
        assertEquals(expectedTotal.getValue(), billItem.getTotalPrice().getValue());
    }
}