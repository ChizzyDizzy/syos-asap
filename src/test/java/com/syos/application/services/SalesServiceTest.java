package com.syos.application.services;

import com.syos.domain.entities.*;
import com.syos.domain.interfaces.ItemState;
import com.syos.domain.valueobjects.*;
import com.syos.domain.exceptions.*;
import com.syos.infrastructure.persistence.gateways.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Sales Service Tests")
class SalesServiceTest {

    @Mock private BillGateway billGateway;
    @Mock private ItemGateway itemGateway;
    @InjectMocks private SalesService salesService;

    private Item testItem;
    private Item onShelfItem;
    private Item inStoreItem;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create test items with different states
        testItem = createTestItem("ITEM001", "Test Item", new BigDecimal("10.00"), 50, "ON_SHELF");
        onShelfItem = createTestItem("ITEM002", "Shelf Item", new BigDecimal("15.00"), 30, "ON_SHELF");
        inStoreItem = createTestItem("ITEM003", "Store Item", new BigDecimal("20.00"), 100, "IN_STORE");
    }

    @Test
    @DisplayName("Should start new sale and return SaleBuilder")
    void should_start_new_sale_and_return_sale_builder() {
        // Act
        SalesService.SaleBuilder saleBuilder = salesService.startNewSale();

        // Assert
        assertNotNull(saleBuilder);
        assertEquals(new BigDecimal("0.00"), saleBuilder.getSubtotal().getValue());
    }

    @Test
    @DisplayName("Should add item to sale and calculate subtotal correctly")
    void should_add_item_to_sale_and_calculate_subtotal_correctly() {
        // Arrange
        when(itemGateway.findByCode("ITEM001")).thenReturn(testItem);
        SalesService.SaleBuilder saleBuilder = salesService.startNewSale();

        // Act
        saleBuilder.addItem("ITEM001", 3);

        // Assert
        assertEquals(new BigDecimal("30.00"), saleBuilder.getSubtotal().getValue());
    }

    @Test
    @DisplayName("Should add multiple items and calculate total subtotal")
    void should_add_multiple_items_and_calculate_total_subtotal() {
        // Arrange
        when(itemGateway.findByCode("ITEM001")).thenReturn(testItem);
        when(itemGateway.findByCode("ITEM002")).thenReturn(onShelfItem);
        SalesService.SaleBuilder saleBuilder = salesService.startNewSale();

        // Act
        saleBuilder.addItem("ITEM001", 2)  // 20.00
                .addItem("ITEM002", 3); // 45.00

        // Assert
        assertEquals(new BigDecimal("65.00"), saleBuilder.getSubtotal().getValue());
    }

    @Test
    @DisplayName("Should throw ItemNotFoundException for non-existent item")
    void should_throw_item_not_found_exception_for_non_existent_item() {
        // Arrange
        when(itemGateway.findByCode("NONEXISTENT")).thenReturn(null);
        SalesService.SaleBuilder saleBuilder = salesService.startNewSale();

        // Act & Assert
        ItemNotFoundException exception = assertThrows(ItemNotFoundException.class, () ->
                saleBuilder.addItem("NONEXISTENT", 1));

        assertTrue(exception.getMessage().contains("NONEXISTENT"));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when requested quantity exceeds available stock")
    void should_throw_insufficient_stock_exception_when_quantity_exceeds_stock() {
        // Arrange
        when(itemGateway.findByCode("ITEM001")).thenReturn(testItem);
        SalesService.SaleBuilder saleBuilder = salesService.startNewSale();

        // Act & Assert
        InsufficientStockException exception = assertThrows(InsufficientStockException.class, () ->
                saleBuilder.addItem("ITEM001", 100)); // More than available 50

        assertTrue(exception.getMessage().contains("Not enough stock"));
        assertTrue(exception.getMessage().contains("Test Item"));
    }

    @Test
    @DisplayName("Should complete sale successfully with valid items and payment")
    void should_complete_sale_successfully_with_valid_items_and_payment() {
        // Arrange
        when(itemGateway.findByCode("ITEM001")).thenReturn(testItem);
        SalesService.SaleBuilder saleBuilder = salesService.startNewSale();
        saleBuilder.addItem("ITEM001", 2); // 20.00

        // Act
        Bill bill = saleBuilder.completeSale(new BigDecimal("25.00"));

        // Assert
        assertNotNull(bill);
        assertEquals(1, bill.getItems().size());
        assertEquals(new BigDecimal("20.00"), bill.getTotalAmount().getValue());
        assertEquals(new BigDecimal("25.00"), bill.getCashTendered().getValue());
        assertEquals(new BigDecimal("5.00"), bill.getChange().getValue());
        assertEquals(TransactionType.IN_STORE, bill.getTransactionType());
    }

    @Test
    @DisplayName("Should throw EmptySaleException when completing sale with no items")
    void should_throw_empty_sale_exception_when_completing_sale_with_no_items() {
        // Arrange
        SalesService.SaleBuilder saleBuilder = salesService.startNewSale();

        // Act & Assert
        EmptySaleException exception = assertThrows(EmptySaleException.class, () ->
                saleBuilder.completeSale(new BigDecimal("100.00")));

        assertTrue(exception.getMessage().contains("Cannot complete sale with no items"));
    }

    @Test
    @DisplayName("Should save bill and update item quantities")
    void should_save_bill_and_update_item_quantities() {
        // Arrange
        Bill mockBill = createMockBill();
        BillItem mockBillItem = mock(BillItem.class);
        when(mockBillItem.getItem()).thenReturn(testItem);
        when(mockBillItem.getQuantity()).thenReturn(new Quantity(5));
        when(mockBill.getItems()).thenReturn(Arrays.asList(mockBillItem));

        // Act
        salesService.saveBill(mockBill);

        // Assert
        verify(billGateway).saveBillWithItems(mockBill);
        verify(testItem).sell(5);
        verify(itemGateway).update(testItem);
    }

    @Test
    @DisplayName("Should get bills for today")
    void should_get_bills_for_today() {
        // Arrange
        List<Bill> expectedBills = Arrays.asList(createMockBill(), createMockBill());
        when(billGateway.findByDate(LocalDate.now())).thenReturn(expectedBills);

        // Act
        List<Bill> result = salesService.getBillsForToday();

        // Assert
        assertEquals(2, result.size());
        verify(billGateway).findByDate(LocalDate.now());
    }

    @Test
    @DisplayName("Should check if item is available for sale correctly")
    void should_check_if_item_is_available_for_sale_correctly() {
        // Arrange
        when(itemGateway.findByCode("ITEM001")).thenReturn(testItem); // ON_SHELF, qty 50
        when(itemGateway.findByCode("ITEM003")).thenReturn(inStoreItem); // IN_STORE, qty 100
        when(itemGateway.findByCode("NONEXISTENT")).thenReturn(null);

        // Act & Assert
        assertTrue(salesService.isItemAvailable("ITEM001")); // On shelf with stock
        assertFalse(salesService.isItemAvailable("ITEM003")); // In store, not on shelf
        assertFalse(salesService.isItemAvailable("NONEXISTENT")); // Doesn't exist
    }

    @Test
    @DisplayName("Should get all available items on shelf with stock")
    void should_get_all_available_items_on_shelf_with_stock() {
        // Arrange
        Item zeroStockItem = createTestItem("ITEM004", "Zero Stock", new BigDecimal("5.00"), 0, "ON_SHELF");
        List<Item> allItems = Arrays.asList(testItem, onShelfItem, inStoreItem, zeroStockItem);
        when(itemGateway.findAll()).thenReturn(allItems);

        // Act
        List<Item> availableItems = salesService.getAvailableItems();

        // Assert
        assertEquals(2, availableItems.size()); // Only testItem and onShelfItem
        assertTrue(availableItems.contains(testItem));
        assertTrue(availableItems.contains(onShelfItem));
        assertFalse(availableItems.contains(inStoreItem)); // In store, not on shelf
        assertFalse(availableItems.contains(zeroStockItem)); // Zero stock
    }

    @Test
    @DisplayName("Should handle insufficient payment in complete sale")
    void should_handle_insufficient_payment_in_complete_sale() {
        // Arrange
        when(itemGateway.findByCode("ITEM001")).thenReturn(testItem);
        SalesService.SaleBuilder saleBuilder = salesService.startNewSale();
        saleBuilder.addItem("ITEM001", 3); // 30.00 total

        // Act & Assert
        assertThrows(InsufficientPaymentException.class, () ->
                saleBuilder.completeSale(new BigDecimal("20.00"))); // Less than 30.00
    }

    // Helper methods
    private Item createTestItem(String code, String name, BigDecimal price, int quantity, String stateName) {
        Item item = mock(Item.class);
        ItemState state = mock(ItemState.class);

        when(item.getCode()).thenReturn(new ItemCode(code));
        when(item.getName()).thenReturn(name);
        when(item.getPrice()).thenReturn(new Money(price));
        when(item.getQuantity()).thenReturn(new Quantity(quantity));
        when(item.getState()).thenReturn(state);
        when(state.getStateName()).thenReturn(stateName);

        return item;
    }

    private Bill createMockBill() {
        Bill bill = mock(Bill.class);
        when(bill.getBillNumber()).thenReturn(new BillNumber(1001));
        when(bill.getTotalAmount()).thenReturn(new Money(new BigDecimal("100.00")));
        when(bill.getItems()).thenReturn(new ArrayList<>());
        return bill;
    }
}