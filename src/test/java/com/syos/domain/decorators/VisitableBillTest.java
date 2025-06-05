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

class VisitableBillTest {

    @Mock
    private Bill mockBill;

    @Mock
    private BillVisitor mockVisitor1;

    @Mock
    private BillVisitor mockVisitor2;

    @Mock
    private BillVisitor mockVisitor3;

    @Mock
    private BillNumber mockBillNumber;

    @Mock
    private Money mockMoney;

    @Mock
    private BillItem mockBillItem;

    private VisitableBill visitableBill;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup common mock behavior
        when(mockBill.getBillNumber()).thenReturn(mockBillNumber);
        when(mockBill.getBillDate()).thenReturn(LocalDateTime.now());
        when(mockBill.getTransactionType()).thenReturn(TransactionType.IN_STORE);

        visitableBill = new VisitableBill(mockBill);
    }

    @Test
    void should_properly_delegate_all_bill_interface_methods_to_wrapped_bill() {
        // Arrange
        LocalDateTime billDate = LocalDateTime.of(2024, 6, 5, 14, 30);
        List<BillItem> items = Arrays.asList(mockBillItem);
        Money totalAmount = new Money(new BigDecimal("500.00"));
        Money discount = new Money(new BigDecimal("50.00"));
        Money cashTendered = new Money(new BigDecimal("500.00"));
        Money change = new Money(new BigDecimal("50.00"));
        Money finalAmount = new Money(new BigDecimal("450.00"));

        // Setup mock bill behavior
        when(mockBill.getBillDate()).thenReturn(billDate);
        when(mockBill.getItems()).thenReturn(items);
        when(mockBill.getTotalAmount()).thenReturn(totalAmount);
        when(mockBill.getDiscount()).thenReturn(discount);
        when(mockBill.getCashTendered()).thenReturn(cashTendered);
        when(mockBill.getChange()).thenReturn(change);
        when(mockBill.getFinalAmount()).thenReturn(finalAmount);
        when(mockBill.getTransactionType()).thenReturn(TransactionType.ONLINE);

        // Act & Assert - Verify all delegated methods
        assertEquals(mockBillNumber, visitableBill.getBillNumber());
        assertEquals(billDate, visitableBill.getBillDate());
        assertEquals(items, visitableBill.getItems());
        assertEquals(totalAmount, visitableBill.getTotalAmount());
        assertEquals(discount, visitableBill.getDiscount());
        assertEquals(cashTendered, visitableBill.getCashTendered());
        assertEquals(change, visitableBill.getChange());
        assertEquals(finalAmount, visitableBill.getFinalAmount());
        assertEquals(TransactionType.ONLINE, visitableBill.getTransactionType());

        // Verify original bill access
        assertEquals(mockBill, visitableBill.getOriginalBill());

        // Verify all delegated methods were called
        verify(mockBill).getBillNumber();
        verify(mockBill).getBillDate();
        verify(mockBill).getItems();
        verify(mockBill).getTotalAmount();
        verify(mockBill).getDiscount();
        verify(mockBill).getCashTendered();
        verify(mockBill).getChange();
        verify(mockBill).getFinalAmount();
        verify(mockBill).getTransactionType();

        // Verify initial state
        assertTrue(visitableBill.getVisitorsHistory().isEmpty());
        assertFalse(visitableBill.hasBeenVisitedBy(BillVisitor.class));
    }

    @Test
    void should_track_visitor_history_and_support_multiple_visitors() {
        // Act - Single visitor
        visitableBill.accept(mockVisitor1);

        // Assert - Single visitor
        verify(mockVisitor1).visit(mockBill);
        assertEquals(1, visitableBill.getVisitorsHistory().size());
        assertTrue(visitableBill.getVisitorsHistory().contains(mockVisitor1));
        assertTrue(visitableBill.hasBeenVisitedBy(mockVisitor1.getClass()));

        // Act - Multiple visitors via list
        List<BillVisitor> visitors = Arrays.asList(mockVisitor2, mockVisitor3);
        visitableBill.accept(visitors);

        // Assert - Multiple visitors
        verify(mockVisitor2).visit(mockBill);
        verify(mockVisitor3).visit(mockBill);
        assertEquals(3, visitableBill.getVisitorsHistory().size());
        assertTrue(visitableBill.getVisitorsHistory().contains(mockVisitor2));
        assertTrue(visitableBill.getVisitorsHistory().contains(mockVisitor3));

        // Act - Visitors in specific order
        BillVisitor mockVisitor4 = mock(BillVisitor.class);
        BillVisitor mockVisitor5 = mock(BillVisitor.class);
        visitableBill.acceptInOrder(mockVisitor4, mockVisitor5);

        // Assert - Order verification
        verify(mockVisitor4).visit(mockBill);
        verify(mockVisitor5).visit(mockBill);
        assertEquals(5, visitableBill.getVisitorsHistory().size());

        // Verify order in history
        List<BillVisitor> history = visitableBill.getVisitorsHistory();
        assertEquals(mockVisitor1, history.get(0));
        assertEquals(mockVisitor2, history.get(1));
        assertEquals(mockVisitor3, history.get(2));
        assertEquals(mockVisitor4, history.get(3));
        assertEquals(mockVisitor5, history.get(4));
    }

    @Test
    void should_handle_result_visitors_and_visitor_history_management() {
        // Arrange - Create a concrete ResultVisitor implementation
        TestResultVisitor resultVisitor = new TestResultVisitor("Test Result");

        // Act - Process with result visitor
        String result = visitableBill.processWithVisitor(resultVisitor);

        // Assert - Result visitor functionality
        assertEquals("Test Result", result);
        verify(mockBill, never()).accept(any()); // Should not delegate to original bill's accept method

        // Test visitor type checking
        visitableBill.accept(mockVisitor1);
        visitableBill.accept(mockVisitor2);

        assertTrue(visitableBill.hasBeenVisitedBy(mockVisitor1.getClass()));
        assertTrue(visitableBill.hasBeenVisitedBy(mockVisitor2.getClass()));
        assertFalse(visitableBill.hasBeenVisitedBy(TestResultVisitor.class)); // processWithVisitor doesn't add to history

        // Test history management
        List<BillVisitor> historyBeforeClear = visitableBill.getVisitorsHistory();
        assertEquals(2, historyBeforeClear.size());

        // Test history immutability (defensive copy)
        historyBeforeClear.clear();
        assertEquals(2, visitableBill.getVisitorsHistory().size()); // Original should be unchanged

        // Test clear functionality
        visitableBill.clearVisitorHistory();
        assertTrue(visitableBill.getVisitorsHistory().isEmpty());
        assertFalse(visitableBill.hasBeenVisitedBy(mockVisitor1.getClass()));

        // Test null bill validation
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new VisitableBill(null);
        });
        assertEquals("Bill cannot be null", exception.getMessage());
    }

    // Test implementation of ResultVisitor for testing purposes
    private static class TestResultVisitor implements VisitableBill.ResultVisitor<String> {
        private final String result;
        private boolean visited = false;

        public TestResultVisitor(String result) {
            this.result = result;
        }

        @Override
        public void visit(Bill bill) {
            visited = true;
        }

        @Override
        public String getResult() {
            return visited ? result : null;
        }
    }
}