package com.syos.domain.decorators;

import com.syos.domain.entities.Bill;
import com.syos.domain.entities.BillItem;
import com.syos.domain.interfaces.BillVisitor;
import com.syos.domain.valueobjects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class OnlineTransactionDecoratorTest {

    @Mock
    private Bill mockBill;

    @Mock
    private BillVisitor mockVisitor;

    @Mock
    private BillNumber mockBillNumber;

    @Mock
    private Money mockMoney;

    @Mock
    private BillItem mockBillItem;

    private OnlineTransactionDecorator onlineDecorator;
    private final String validEmail = "customer@test.com";
    private final String validAddress = "123 Main St, Colombo";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup common mock behavior
        when(mockBill.getBillNumber()).thenReturn(mockBillNumber);
        when(mockBillNumber.getValue()).thenReturn(12345);
        when(mockBill.getBillDate()).thenReturn(LocalDateTime.now());
        when(mockBill.getFinalAmount()).thenReturn(mockMoney);
        when(mockMoney.toString()).thenReturn("$150.00");
    }

    @Test
    void should_properly_delegate_all_bill_interface_methods_and_add_online_functionality() {
        // Arrange
        LocalDateTime billDate = LocalDateTime.of(2024, 6, 5, 10, 30);
        List<BillItem> items = Arrays.asList(mockBillItem);
        Money totalAmount = new Money(new BigDecimal("200.00"));
        Money discount = new Money(new BigDecimal("10.00"));
        Money cashTendered = new Money(new BigDecimal("200.00"));
        Money change = new Money(new BigDecimal("10.00"));
        Money finalAmount = new Money(new BigDecimal("190.00"));

        // Setup mock bill behavior
        when(mockBill.getBillDate()).thenReturn(billDate);
        when(mockBill.getItems()).thenReturn(items);
        when(mockBill.getTotalAmount()).thenReturn(totalAmount);
        when(mockBill.getDiscount()).thenReturn(discount);
        when(mockBill.getCashTendered()).thenReturn(cashTendered);
        when(mockBill.getChange()).thenReturn(change);
        when(mockBill.getFinalAmount()).thenReturn(finalAmount);
        when(mockBill.getTransactionType()).thenReturn(TransactionType.IN_STORE);

        // Act
        onlineDecorator = new OnlineTransactionDecorator(mockBill, validEmail, validAddress);

        // Assert - Verify delegation to wrapped bill
        assertEquals(mockBillNumber, onlineDecorator.getBillNumber());
        assertEquals(billDate, onlineDecorator.getBillDate());
        assertEquals(items, onlineDecorator.getItems());
        assertEquals(totalAmount, onlineDecorator.getTotalAmount());
        assertEquals(discount, onlineDecorator.getDiscount());
        assertEquals(cashTendered, onlineDecorator.getCashTendered());
        assertEquals(change, onlineDecorator.getChange());
        assertEquals(finalAmount, onlineDecorator.getFinalAmount());

        // Assert - Verify transaction type override
        assertEquals(TransactionType.ONLINE, onlineDecorator.getTransactionType());

        // Assert - Verify online-specific properties
        assertEquals(validEmail, onlineDecorator.getCustomerEmail());
        assertEquals(validAddress, onlineDecorator.getDeliveryAddress());
        assertNotNull(onlineDecorator.getTrackingNumber());
        assertTrue(onlineDecorator.getTrackingNumber().startsWith("SYOS-"));
        assertTrue(onlineDecorator.getTrackingNumber().contains("12345")); // Bill number
        assertNotNull(onlineDecorator.getEstimatedDeliveryDate());
        assertEquals(billDate.plusDays(3), onlineDecorator.getEstimatedDeliveryDate());

        // Assert - Verify original bill access
        assertEquals(mockBill, onlineDecorator.getOriginalBill());

        // Verify all delegated methods were called
        verify(mockBill).getBillNumber();
        verify(mockBill).getBillDate();
        verify(mockBill).getItems();
        verify(mockBill).getTotalAmount();
        verify(mockBill).getDiscount();
        verify(mockBill).getCashTendered();
        verify(mockBill).getChange();
        verify(mockBill).getFinalAmount();
    }

    @Test
    void should_validate_constructor_inputs_and_throw_appropriate_exceptions() {
        // Test null bill
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new OnlineTransactionDecorator(null, validEmail, validAddress);
        });
        assertEquals("Bill cannot be null", exception.getMessage());

        // Test null email
        exception = assertThrows(IllegalArgumentException.class, () -> {
            new OnlineTransactionDecorator(mockBill, null, validAddress);
        });
        assertEquals("Customer email is required", exception.getMessage());

        // Test empty email
        exception = assertThrows(IllegalArgumentException.class, () -> {
            new OnlineTransactionDecorator(mockBill, "   ", validAddress);
        });
        assertEquals("Customer email is required", exception.getMessage());

        // Test null address
        exception = assertThrows(IllegalArgumentException.class, () -> {
            new OnlineTransactionDecorator(mockBill, validEmail, null);
        });
        assertEquals("Delivery address is required", exception.getMessage());

        // Test empty address
        exception = assertThrows(IllegalArgumentException.class, () -> {
            new OnlineTransactionDecorator(mockBill, validEmail, "");
        });
        assertEquals("Delivery address is required", exception.getMessage());

        // Test invalid email format
        exception = assertThrows(IllegalArgumentException.class, () -> {
            new OnlineTransactionDecorator(mockBill, "invalid-email", validAddress);
        });
        assertEquals("Invalid email format", exception.getMessage());

        // Test valid construction should not throw
        assertDoesNotThrow(() -> {
            new OnlineTransactionDecorator(mockBill, validEmail, validAddress);
        });
    }

    @Test
    void should_handle_visitor_pattern_and_provide_online_specific_operations() {
        // Arrange
        onlineDecorator = new OnlineTransactionDecorator(mockBill, validEmail, validAddress);

        // Act & Assert - Test visitor pattern
        onlineDecorator.accept(mockVisitor);

        // Verify visitor was called on both the decorator's original bill and the wrapped bill
        verify(mockVisitor).visit(mockBill);
        verify(mockBill).accept(mockVisitor);

        // Test online-specific operations (these are void methods, so we verify they don't throw)
        assertDoesNotThrow(() -> {
            onlineDecorator.sendEmailConfirmation();
        });

        assertDoesNotThrow(() -> {
            onlineDecorator.scheduleDelivery();
        });

        assertDoesNotThrow(() -> {
            onlineDecorator.processOnlinePayment();
        });

        assertDoesNotThrow(() -> {
            onlineDecorator.updateDeliveryStatus("Shipped");
        });

        // Verify tracking number format and uniqueness
        String trackingNumber1 = onlineDecorator.getTrackingNumber();

        // Create another decorator to test tracking number uniqueness
        OnlineTransactionDecorator anotherDecorator = new OnlineTransactionDecorator(mockBill, validEmail, validAddress);
        String trackingNumber2 = anotherDecorator.getTrackingNumber();

        assertNotEquals(trackingNumber1, trackingNumber2, "Tracking numbers should be unique");
        assertTrue(trackingNumber1.startsWith("SYOS-"));
        assertTrue(trackingNumber2.startsWith("SYOS-"));

        // Verify estimated delivery date calculation
        LocalDateTime billDate = LocalDateTime.of(2024, 6, 5, 14, 30);
        when(mockBill.getBillDate()).thenReturn(billDate);

        OnlineTransactionDecorator dateTestDecorator = new OnlineTransactionDecorator(mockBill, validEmail, validAddress);
        assertEquals(billDate.plusDays(3), dateTestDecorator.getEstimatedDeliveryDate());
    }
}