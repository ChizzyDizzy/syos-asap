// AddStockCommandTest.java - 11 tests following all testing standards
package com.syos.application.commands.inventory;

import com.syos.application.services.InventoryService;
import com.syos.infrastructure.ui.presenters.InventoryPresenter;
import com.syos.infrastructure.ui.cli.InputReader;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Add Stock Command Tests")
class AddStockCommandTest {

    @Mock private InventoryService inventoryService;
    @Mock private InventoryPresenter presenter;
    @Mock private InputReader inputReader;
    @InjectMocks private AddStockCommand addStockCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should execute successfully when valid inputs provided and confirmed")
    void should_execute_successfully_when_valid_inputs_provided_and_confirmed() {
        // Arrange
        when(inputReader.readString("Enter item code (e.g., ITEM001): ")).thenReturn("ITEM001");
        when(inputReader.readString("Enter item name: ")).thenReturn("Test Item");
        when(inputReader.readBigDecimal("Enter price per unit: $")).thenReturn(new BigDecimal("25.00"));
        when(inputReader.readInt("Enter quantity: ")).thenReturn(100);
        when(inputReader.readString("")).thenReturn("2024-12-31");
        when(inputReader.readBoolean("Confirm addition?")).thenReturn(true);

        LocalDate expectedExpiryDate = LocalDate.of(2024, 12, 31);

        // Act
        addStockCommand.execute();

        // Assert
        verify(presenter).showHeader("Add New Stock");
        verify(inventoryService).addStock("ITEM001", "Test Item", new BigDecimal("25.00"), 100, expectedExpiryDate);
        verify(presenter).showSuccess("Stock added successfully: ITEM001 - Test Item (Qty: 100)");
    }

    @Test
    @DisplayName("Should execute successfully when no expiry date provided")
    void should_execute_successfully_when_no_expiry_date_provided() {
        // Arrange
        when(inputReader.readString("Enter item code (e.g., ITEM001): ")).thenReturn("STOCK001");
        when(inputReader.readString("Enter item name: ")).thenReturn("No Expiry Item");
        when(inputReader.readBigDecimal("Enter price per unit: $")).thenReturn(new BigDecimal("15.50"));
        when(inputReader.readInt("Enter quantity: ")).thenReturn(50);
        when(inputReader.readString("")).thenReturn(""); // Empty string for no expiry
        when(inputReader.readBoolean("Confirm addition?")).thenReturn(true);

        // Act
        addStockCommand.execute();

        // Assert
        verify(presenter).showHeader("Add New Stock");
        verify(inventoryService).addStock("STOCK001", "No Expiry Item", new BigDecimal("15.50"), 50, null);
        verify(presenter).showSuccess("Stock added successfully: STOCK001 - No Expiry Item (Qty: 50)");
    }

    @Test
    @DisplayName("Should convert item code to uppercase and validate format")
    void should_convert_item_code_to_uppercase_and_validate_format() {
        // Arrange - First invalid code, then valid lowercase code
        when(inputReader.readString("Enter item code (e.g., ITEM001): "))
                .thenReturn("AB", "item002"); // First invalid (too short), then valid lowercase
        when(inputReader.readString("Enter item name: ")).thenReturn("Test Item");
        when(inputReader.readBigDecimal("Enter price per unit: $")).thenReturn(new BigDecimal("10.00"));
        when(inputReader.readInt("Enter quantity: ")).thenReturn(25);
        when(inputReader.readString("")).thenReturn("");
        when(inputReader.readBoolean("Confirm addition?")).thenReturn(true);

        // Act
        addStockCommand.execute();

        // Assert
        verify(presenter).showError("Invalid code format. Use 4-10 alphanumeric characters.");
        verify(inventoryService).addStock("ITEM002", "Test Item", new BigDecimal("10.00"), 25, null);
        verify(presenter).showSuccess("Stock added successfully: ITEM002 - Test Item (Qty: 25)");
    }

    @Test
    @DisplayName("Should validate item name length and show error for invalid names")
    void should_validate_item_name_length_and_show_error_for_invalid_names() {
        // Arrange
        when(inputReader.readString("Enter item code (e.g., ITEM001): ")).thenReturn("ITEM003");
        when(inputReader.readString("Enter item name: "))
                .thenReturn("", "Valid Item Name"); // First empty, then valid
        when(inputReader.readBigDecimal("Enter price per unit: $")).thenReturn(new BigDecimal("20.00"));
        when(inputReader.readInt("Enter quantity: ")).thenReturn(30);
        when(inputReader.readString("")).thenReturn("");
        when(inputReader.readBoolean("Confirm addition?")).thenReturn(true);

        // Act
        addStockCommand.execute();

        // Assert
        verify(presenter).showError("Item name must be between 1 and 100 characters.");
        verify(inventoryService).addStock("ITEM003", "Valid Item Name", new BigDecimal("20.00"), 30, null);
    }

    @Test
    @DisplayName("Should validate price is greater than zero")
    void should_validate_price_is_greater_than_zero() {
        // Arrange
        when(inputReader.readString("Enter item code (e.g., ITEM001): ")).thenReturn("ITEM004");
        when(inputReader.readString("Enter item name: ")).thenReturn("Price Test Item");
        when(inputReader.readBigDecimal("Enter price per unit: $"))
                .thenReturn(BigDecimal.ZERO, new BigDecimal("15.00")); // First zero, then valid
        when(inputReader.readInt("Enter quantity: ")).thenReturn(40);
        when(inputReader.readString("")).thenReturn("");
        when(inputReader.readBoolean("Confirm addition?")).thenReturn(true);

        // Act
        addStockCommand.execute();

        // Assert
        verify(presenter).showError("Price must be greater than zero.");
        verify(inventoryService).addStock("ITEM004", "Price Test Item", new BigDecimal("15.00"), 40, null);
    }

    @Test
    @DisplayName("Should validate quantity is greater than zero")
    void should_validate_quantity_is_greater_than_zero() {
        // Arrange
        when(inputReader.readString("Enter item code (e.g., ITEM001): ")).thenReturn("ITEM005");
        when(inputReader.readString("Enter item name: ")).thenReturn("Quantity Test Item");
        when(inputReader.readBigDecimal("Enter price per unit: $")).thenReturn(new BigDecimal("12.50"));
        when(inputReader.readInt("Enter quantity: "))
                .thenReturn(0, 35); // First zero, then valid
        when(inputReader.readString("")).thenReturn("");
        when(inputReader.readBoolean("Confirm addition?")).thenReturn(true);

        // Act
        addStockCommand.execute();

        // Assert
        verify(presenter).showError("Quantity must be greater than zero.");
        verify(inventoryService).addStock("ITEM005", "Quantity Test Item", new BigDecimal("12.50"), 35, null);
    }

    @Test
    @DisplayName("Should validate expiry date is in the future")
    void should_validate_expiry_date_is_in_the_future() {
        // Arrange
        String pastDate = "2023-01-01";
        String futureDate = "2024-12-31";

        when(inputReader.readString("Enter item code (e.g., ITEM001): ")).thenReturn("ITEM006");
        when(inputReader.readString("Enter item name: ")).thenReturn("Expiry Test Item");
        when(inputReader.readBigDecimal("Enter price per unit: $")).thenReturn(new BigDecimal("18.00"));
        when(inputReader.readInt("Enter quantity: ")).thenReturn(60);
        when(inputReader.readString("")).thenReturn(pastDate);
        when(inputReader.readString("Enter expiry date (YYYY-MM-DD): ")).thenReturn(futureDate);
        when(inputReader.readBoolean("Confirm addition?")).thenReturn(true);

        // Act
        addStockCommand.execute();

        // Assert
        verify(presenter).showError("Expiry date must be in the future.");
        verify(inventoryService).addStock("ITEM006", "Expiry Test Item", new BigDecimal("18.00"), 60, LocalDate.of(2024, 12, 31));
    }

    @Test
    @DisplayName("Should handle invalid date format and retry")
    void should_handle_invalid_date_format_and_retry() {
        // Arrange
        when(inputReader.readString("Enter item code (e.g., ITEM001): ")).thenReturn("ITEM007");
        when(inputReader.readString("Enter item name: ")).thenReturn("Date Format Test");
        when(inputReader.readBigDecimal("Enter price per unit: $")).thenReturn(new BigDecimal("22.00"));
        when(inputReader.readInt("Enter quantity: ")).thenReturn(45);
        when(inputReader.readString("")).thenReturn("invalid-date");
        when(inputReader.readString("Enter expiry date (YYYY-MM-DD): ")).thenReturn("2024-06-15");
        when(inputReader.readBoolean("Confirm addition?")).thenReturn(true);

        // Act
        addStockCommand.execute();

        // Assert
        verify(presenter).showError("Invalid date format. Use YYYY-MM-DD.");
        verify(inventoryService).addStock("ITEM007", "Date Format Test", new BigDecimal("22.00"), 45, LocalDate.of(2024, 6, 15));
    }

    @Test
    @DisplayName("Should show error and not add stock when user cancels confirmation")
    void should_show_error_and_not_add_stock_when_user_cancels_confirmation() {
        // Arrange
        when(inputReader.readString("Enter item code (e.g., ITEM001): ")).thenReturn("ITEM008");
        when(inputReader.readString("Enter item name: ")).thenReturn("Cancelled Item");
        when(inputReader.readBigDecimal("Enter price per unit: $")).thenReturn(new BigDecimal("30.00"));
        when(inputReader.readInt("Enter quantity: ")).thenReturn(20);
        when(inputReader.readString("")).thenReturn("");
        when(inputReader.readBoolean("Confirm addition?")).thenReturn(false);

        // Act
        addStockCommand.execute();

        // Assert
        verify(presenter).showHeader("Add New Stock");
        verify(inventoryService, never()).addStock(anyString(), anyString(), any(BigDecimal.class), anyInt(), any());
        verify(presenter).showError("Stock addition cancelled.");
        verify(presenter, never()).showSuccess(anyString());
    }

    @Test
    @DisplayName("Should handle exception from inventory service gracefully")
    void should_handle_exception_from_inventory_service_gracefully() {
        // Arrange
        when(inputReader.readString("Enter item code (e.g., ITEM001): ")).thenReturn("ITEM009");
        when(inputReader.readString("Enter item name: ")).thenReturn("Error Item");
        when(inputReader.readBigDecimal("Enter price per unit: $")).thenReturn(new BigDecimal("25.00"));
        when(inputReader.readInt("Enter quantity: ")).thenReturn(50);
        when(inputReader.readString("")).thenReturn("");
        when(inputReader.readBoolean("Confirm addition?")).thenReturn(true);

        doThrow(new RuntimeException("Database connection failed"))
                .when(inventoryService).addStock(anyString(), anyString(), any(BigDecimal.class), anyInt(), any());

        // Act
        addStockCommand.execute();

        // Assert
        verify(presenter).showHeader("Add New Stock");
        verify(inventoryService).addStock("ITEM009", "Error Item", new BigDecimal("25.00"), 50, null);
        verify(presenter).showError("Failed to add stock: Database connection failed");
        verify(presenter, never()).showSuccess(anyString());
    }

    @Test
    @DisplayName("Should return correct description for command")
    void should_return_correct_description_for_command() {
        // Arrange & Act
        String description = addStockCommand.getDescription();

        // Assert
        assertEquals("Add New Stock", description);
    }
}