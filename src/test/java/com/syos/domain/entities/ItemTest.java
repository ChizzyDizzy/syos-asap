package com.syos.domain.entities;

import com.syos.domain.valueobjects.*;
import com.syos.domain.exceptions.*;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Item Entity Tests")
class ItemTest {

    private Item testItem;

    @BeforeEach
    void setUp() {
        testItem = new Item.Builder()
                .withCode("TEST001")
                .withName("Test Item")
                .withPrice(new BigDecimal("10.00"))
                .withQuantity(100)
                .withExpiryDate(LocalDate.now().plusDays(30))
                .build();
    }

    @Test
    @DisplayName("Should create item with all required fields")
    void should_create_item_with_all_required_fields() {
        assertNotNull(testItem);
        assertEquals("TEST001", testItem.getCode().getValue());
        assertEquals("Test Item", testItem.getName());
        assertEquals(new BigDecimal("10.00"), testItem.getPrice().getValue());
        assertEquals(100, testItem.getQuantity().getValue());
    }

    @Test
    @DisplayName("Should throw exception when creating item without code")
    void should_throw_exception_when_creating_item_without_code() {
        assertThrows(InvalidItemException.class, () ->
                new Item.Builder()
                        .withName("Test")
                        .withPrice(new BigDecimal("10.00"))
                        .build()
        );
    }

    @Test
    @DisplayName("Should set default state as IN_STORE")
    void should_set_default_state_as_in_store() {
        assertEquals("IN_STORE", testItem.getState().getStateName());
    }

    @Test
    @DisplayName("Should move items from store to shelf successfully")
    void should_move_items_from_store_to_shelf_successfully() {
        int initialQty = testItem.getQuantity().getValue();
        testItem.moveToShelf(40);
        assertEquals(initialQty - 40, testItem.getQuantity().getValue());
    }

    @Test
    @DisplayName("Should throw exception when moving more items than available")
    void should_throw_exception_when_moving_more_items_than_available() {
        assertThrows(InvalidStateTransitionException.class, () ->
                testItem.moveToShelf(150)
        );
    }

    @Test
    @DisplayName("Should not allow selling from store state")
    void should_not_allow_selling_from_store_state() {
        assertThrows(InvalidStateTransitionException.class, () ->
                testItem.sell(10)
        );
    }

    @Test
    @DisplayName("Should identify expired items correctly")
    void should_identify_expired_items_correctly() {
        Item expiredItem = new Item.Builder()
                .withCode("EXP001")
                .withName("Expired Item")
                .withPrice(new BigDecimal("5.00"))
                .withQuantity(10)
                .withExpiryDate(LocalDate.now().minusDays(1))
                .build();

        assertTrue(expiredItem.isExpired());
        assertFalse(testItem.isExpired());
    }

    @Test
    @DisplayName("Should calculate days until expiry")
    void should_calculate_days_until_expiry() {
        Item itemExpiring = new Item.Builder()
                .withCode("EXP002")
                .withName("Expiring Soon")
                .withPrice(new BigDecimal("5.00"))
                .withQuantity(10)
                .withExpiryDate(LocalDate.now().plusDays(5))
                .build();

        assertEquals(5, itemExpiring.daysUntilExpiry());
    }

    @Test
    @DisplayName("Should handle items without expiry date")
    void should_handle_items_without_expiry_date() {
        Item noExpiryItem = new Item.Builder()
                .withCode("NOEXP001")
                .withName("No Expiry Item")
                .withPrice(new BigDecimal("10.00"))
                .withQuantity(50)
                .build();

        assertNull(noExpiryItem.getExpiryDate());
        assertEquals(Long.MAX_VALUE, noExpiryItem.daysUntilExpiry());
        assertFalse(noExpiryItem.isExpired());
    }

    @Test
    @DisplayName("Should expire item and change state")
    void should_expire_item_and_change_state() {
        testItem.expire();
        assertEquals("EXPIRED", testItem.getState().getStateName());
    }
}
