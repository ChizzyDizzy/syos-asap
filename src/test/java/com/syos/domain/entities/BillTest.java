package com.syos.domain.entities;

import com.syos.domain.valueobjects.*;
import com.syos.domain.exceptions.*;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Bill Entity Tests")
class BillTest {

    private Item mockItem;

    @BeforeEach
    void setUp() {
        mockItem = mock(Item.class);
        when(mockItem.getCode()).thenReturn(new ItemCode("ITEM001"));
        when(mockItem.getName()).thenReturn("Test Item");
        when(mockItem.getPrice()).thenReturn(new Money(new BigDecimal("10.00")));
    }

    @Test
    @DisplayName("Should create bill with valid data")
    void should_create_bill_with_valid_data() {
        Bill bill = new Bill.Builder()
                .withBillNumber(1001)
                .addItem(mockItem, 2)
                .withCashTendered(new BigDecimal("25.00"))
                .build();

        assertNotNull(bill);
        assertEquals(1001, bill.getBillNumber().getValue());
        assertEquals(1, bill.getItems().size());
        assertEquals(new BigDecimal("20.00"), bill.getTotalAmount().getValue());
        assertEquals(new BigDecimal("5.00"), bill.getChange().getValue());
    }

    @Test
    @DisplayName("Should throw exception for empty bill")
    void should_throw_exception_for_empty_bill() {
        assertThrows(EmptySaleException.class, () ->
                new Bill.Builder()
                        .withBillNumber(1001)
                        .withCashTendered(new BigDecimal("100.00"))
                        .build()
        );
    }

    @Test
    @DisplayName("Should calculate change correctly")
    void should_calculate_change_correctly() {
        Bill bill = new Bill.Builder()
                .withBillNumber(1001)
                .addItem(mockItem, 3) // 30.00
                .withCashTendered(new BigDecimal("50.00"))
                .build();

        assertEquals(new BigDecimal("20.00"), bill.getChange().getValue());
    }

    @Test
    @DisplayName("Should throw exception for insufficient payment")
    void should_throw_exception_for_insufficient_payment() {
        assertThrows(InsufficientPaymentException.class, () ->
                new Bill.Builder()
                        .withBillNumber(1001)
                        .addItem(mockItem, 5) // 50.00
                        .withCashTendered(new BigDecimal("40.00"))
                        .build()
        );
    }

    @Test
    @DisplayName("Should apply discount correctly")
    void should_apply_discount_correctly() {
        Bill bill = new Bill.Builder()
                .withBillNumber(1001)
                .addItem(mockItem, 5) // 50.00
                .withDiscount(new BigDecimal("5.00"))
                .withCashTendered(new BigDecimal("50.00"))
                .build();

        assertEquals(new BigDecimal("50.00"), bill.getTotalAmount().getValue());
        assertEquals(new BigDecimal("5.00"), bill.getDiscount().getValue());
        assertEquals(new BigDecimal("45.00"), bill.getFinalAmount().getValue());
        assertEquals(new BigDecimal("5.00"), bill.getChange().getValue());
    }

    @Test
    @DisplayName("Should set transaction type correctly")
    void should_set_transaction_type_correctly() {
        Bill bill = new Bill.Builder()
                .withBillNumber(1001)
                .addItem(mockItem, 1)
                .withCashTendered(new BigDecimal("20.00"))
                .withTransactionType(TransactionType.ONLINE)
                .build();

        assertEquals(TransactionType.ONLINE, bill.getTransactionType());
    }
}
