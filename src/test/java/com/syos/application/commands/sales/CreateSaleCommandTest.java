// CreateSaleCommandTest.java - 12 tests following all testing standards
package com.syos.application.commands.sales;

import com.syos.application.services.SalesService;
import com.syos.domain.entities.*;
import com.syos.domain.valueobjects.*;
import com.syos.domain.exceptions.*;
import com.syos.infrastructure.ui.presenters.SalesPresenter;
import com.syos.infrastructure.ui.cli.InputReader;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Create Sale Command Tests")
class CreateSaleCommandTest {

    @Mock private SalesService salesService;
    @Mock private SalesPresenter presenter;
    @Mock private InputReader inputReader;
    @Mock private SalesService.SaleBuilder saleBuilder;
    @Mock private Bill mockBill;
    @InjectMocks private CreateSaleCommand createSaleCommand;

    private List<Item> availableItems;
    private Item testItem;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create test data
        testItem = createTestItem("ITEM001", "Test Item", new BigDecimal("10.00"), 50);
        availableItems = Arrays.asList(testItem);

        // Setup default behaviors
        when(salesService.startNewSale()).thenReturn(saleBuilder);
        when(salesService.getAvailableItems()).thenReturn(availableItems);
        when(saleBuilder.getSubtotal()).thenReturn(new Money(new BigDecimal("50.00")));
    }

    @Test
    @DisplayName("Should show sale header and available items when command starts")
    void should_show_sale_header_and_available_items_when_command_starts() {
        // Arrange
        when(inputReader.readString("Enter item code (or 'DONE' to finish): ")).thenReturn("DONE");

        // Act
        createSaleCommand.execute();

        // Assert
        verify(presenter).showSaleHeader();
        verify(salesService).getAvailableItems();
        verify(salesService).startNewSale();
    }

    @Test
    @DisplayName("Should show warning when no items available for sale")
    void should_show_warning_when_no_items_available_for_sale() {
        // Arrange
        when(salesService.getAvailableItems()).thenReturn(Collections.emptyList());
        when(inputReader.readString("Enter item code (or 'DONE' to finish): ")).thenReturn("DONE");

        // Act
        createSaleCommand.execute();

        // Assert
        verify(presenter).showWarning("No items available for sale!");
        verify(presenter).showInfo("Please move items to shelf first (Inventory → Move to Shelf)");
    }

    @Test
    @DisplayName("Should show error and cancel sale when no items added")
    void should_show_error_and_cancel_sale_when_no_items_added() {
        // Arrange
        when(inputReader.readString("Enter item code (or 'DONE' to finish): ")).thenReturn("DONE");

        // Act
        createSaleCommand.execute();

        // Assert
        verify(presenter).showError("No items added to sale. Sale cancelled.");
        verify(saleBuilder, never()).completeSale(any());
        verify(salesService, never()).saveBill(any());
    }

    @Test
    @DisplayName("Should convert item code to uppercase and add item successfully")
    void should_convert_item_code_to_uppercase_and_add_item_successfully() {
        // Arrange
        when(inputReader.readString("Enter item code (or 'DONE' to finish): "))
                .thenReturn("item001", "DONE");
        when(inputReader.readInt("Enter quantity: ")).thenReturn(5);
        when(inputReader.readBigDecimal("Enter cash amount: $")).thenReturn(new BigDecimal("60.00"));
        when(salesService.isItemAvailable("ITEM001")).thenReturn(true);
        when(saleBuilder.completeSale(new BigDecimal("60.00"))).thenReturn(mockBill);

        // Act
        createSaleCommand.execute();

        // Assert
        verify(salesService).isItemAvailable("ITEM001"); // Uppercase conversion
        verify(saleBuilder).addItem("ITEM001", 5);
        verify(presenter).showItemAdded("ITEM001", 5);
        verify(presenter, atLeastOnce()).showSubtotal(any(Money.class));
    }

    @Test
    @DisplayName("Should show error when item not available for sale")
    void should_show_error_when_item_not_available_for_sale() {
        // Arrange
        when(inputReader.readString("Enter item code (or 'DONE' to finish): "))
                .thenReturn("INVALID001", "DONE");
        when(salesService.isItemAvailable("INVALID001")).thenReturn(false);

        // Act
        createSaleCommand.execute();

        // Assert
        verify(presenter).showError("Item INVALID001 not found or not available for sale");
        verify(saleBuilder, never()).addItem(anyString(), anyInt());
    }

    @Test
    @DisplayName("Should show error when quantity is zero or negative")
    void should_show_error_when_quantity_is_zero_or_negative() {
        // Arrange
        when(inputReader.readString("Enter item code (or 'DONE' to finish): "))
                .thenReturn("ITEM001", "ITEM001", "DONE");
        when(inputReader.readInt("Enter quantity: ")).thenReturn(0, -5);
        when(salesService.isItemAvailable("ITEM001")).thenReturn(true);

        // Act
        createSaleCommand.execute();

        // Assert
        verify(presenter, times(2)).showError("Quantity must be greater than zero");
        verify(saleBuilder, never()).addItem(anyString(), anyInt());
    }

    @Test
    @DisplayName("Should handle ItemNotFoundException when adding item")
    void should_handle_item_not_found_exception_when_adding_item() {
        // Arrange
        when(inputReader.readString("Enter item code (or 'DONE' to finish): "))
                .thenReturn("ITEM001", "DONE");
        when(inputReader.readInt("Enter quantity: ")).thenReturn(5);
        when(salesService.isItemAvailable("ITEM001")).thenReturn(true);
        when(saleBuilder.addItem("ITEM001", 5))
                .thenThrow(new ItemNotFoundException("Item ITEM001 not found"));

        // Act
        createSaleCommand.execute();

        // Assert
        verify(presenter).showError("Item not found: Item ITEM001 not found");
        verify(presenter).showError("No items added to sale. Sale cancelled.");
    }

    @Test
    @DisplayName("Should handle InsufficientStockException when adding item")
    void should_handle_insufficient_stock_exception_when_adding_item() {
        // Arrange
        when(inputReader.readString("Enter item code (or 'DONE' to finish): "))
                .thenReturn("ITEM001", "DONE");
        when(inputReader.readInt("Enter quantity: ")).thenReturn(100);
        when(salesService.isItemAvailable("ITEM001")).thenReturn(true);
        when(saleBuilder.addItem("ITEM001", 100))
                .thenThrow(new InsufficientStockException("Not enough stock for item Test Item"));

        // Act
        createSaleCommand.execute();

        // Assert
        verify(presenter).showError("Insufficient stock: Not enough stock for item Test Item");
        verify(presenter).showError("No items added to sale. Sale cancelled.");
    }

    @Test
    @DisplayName("Should handle generic exception when adding item")
    void should_handle_generic_exception_when_adding_item() {
        // Arrange
        when(inputReader.readString("Enter item code (or 'DONE' to finish): "))
                .thenReturn("ITEM001", "DONE");
        when(inputReader.readInt("Enter quantity: ")).thenReturn(5);
        when(salesService.isItemAvailable("ITEM001")).thenReturn(true);
        when(saleBuilder.addItem("ITEM001", 5))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        createSaleCommand.execute();

        // Assert
        verify(presenter).showError("Error adding item: Database error");
        verify(presenter).showError("No items added to sale. Sale cancelled.");
    }

    @Test
    @DisplayName("Should complete sale successfully with valid payment")
    void should_complete_sale_successfully_with_valid_payment() {
        // Arrange
        when(inputReader.readString("Enter item code (or 'DONE' to finish): "))
                .thenReturn("ITEM001", "DONE");
        when(inputReader.readInt("Enter quantity: ")).thenReturn(5);
        when(inputReader.readBigDecimal("Enter cash amount: $")).thenReturn(new BigDecimal("60.00"));
        when(salesService.isItemAvailable("ITEM001")).thenReturn(true);
        when(saleBuilder.getSubtotal()).thenReturn(new Money(new BigDecimal("50.00")));
        when(saleBuilder.completeSale(new BigDecimal("60.00"))).thenReturn(mockBill);

        // Act
        createSaleCommand.execute();

        // Assert
        verify(saleBuilder).addItem("ITEM001", 5);
        verify(saleBuilder).completeSale(new BigDecimal("60.00"));
        verify(salesService).saveBill(mockBill);
        verify(presenter).showBill(mockBill);
        verify(presenter).showSuccess("Sale completed successfully!");
    }

    @Test
    @DisplayName("Should show error when cash amount is insufficient")
    void should_show_error_when_cash_amount_is_insufficient() {
        // Arrange
        when(inputReader.readString("Enter item code (or 'DONE' to finish): "))
                .thenReturn("ITEM001", "DONE");
        when(inputReader.readInt("Enter quantity: ")).thenReturn(5);
        when(inputReader.readBigDecimal("Enter cash amount: $")).thenReturn(new BigDecimal("30.00"));
        when(salesService.isItemAvailable("ITEM001")).thenReturn(true);
        when(saleBuilder.getSubtotal()).thenReturn(new Money(new BigDecimal("50.00")));

        // Act
        createSaleCommand.execute();

        // Assert
        verify(presenter).showError("Insufficient cash. Required: $50.00");
        verify(saleBuilder, never()).completeSale(any());
        verify(salesService, never()).saveBill(any());
    }

    @Test
    @DisplayName("Should handle EmptySaleException during payment processing")
    void should_handle_empty_sale_exception_during_payment_processing() {
        // Arrange
        when(inputReader.readString("Enter item code (or 'DONE' to finish): "))
                .thenReturn("ITEM001", "DONE");
        when(inputReader.readInt("Enter quantity: ")).thenReturn(5);
        when(inputReader.readBigDecimal("Enter cash amount: $")).thenReturn(new BigDecimal("60.00"));
        when(salesService.isItemAvailable("ITEM001")).thenReturn(true);
        when(saleBuilder.getSubtotal()).thenReturn(new Money(new BigDecimal("50.00")));
        when(saleBuilder.completeSale(new BigDecimal("60.00")))
                .thenThrow(new EmptySaleException("Cannot complete sale with no items"));

        // Act
        createSaleCommand.execute();

        // Assert
        verify(presenter).showError("Cannot complete sale: Cannot complete sale with no items");
        verify(salesService, never()).saveBill(any());
    }

    @Test
    @DisplayName("Should return correct description for command")
    void should_return_correct_description_for_command() {
        // Arrange & Act
        String description = createSaleCommand.getDescription();

        // Assert
        assertEquals("Create New Sale", description);
    }

    // Helper method
    private Item createTestItem(String code, String name, BigDecimal price, int quantity) {
        Item item = mock(Item.class);
        when(item.getCode()).thenReturn(new ItemCode(code));
        when(item.getName()).thenReturn(name);
        when(item.getPrice()).thenReturn(new Money(price));
        when(item.getQuantity()).thenReturn(new Quantity(quantity));
        return item;
    }
}