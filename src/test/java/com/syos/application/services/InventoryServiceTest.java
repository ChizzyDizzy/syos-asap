// InventoryServiceTest.java - 15 tests
package com.syos.application.services;

import com.syos.domain.entities.*;
import com.syos.domain.interfaces.ItemState;
import com.syos.domain.valueobjects.*;
import com.syos.domain.exceptions.*;
import com.syos.infrastructure.persistence.gateways.ItemGateway;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Inventory Service Tests")
class InventoryServiceTest {

    @Mock private ItemGateway itemGateway;
    @InjectMocks private InventoryService inventoryService;

    private Item testItem;
    private Item shelfItem;
    private Item expiredItem;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create test items with different states
        testItem = createTestItem("ITEM001", "Test Item", new BigDecimal("10.00"),
                100, LocalDate.now().plusDays(30), "IN_STORE");
        shelfItem = createTestItem("ITEM002_SHELF", "Shelf Item", new BigDecimal("15.00"),
                25, LocalDate.now().plusDays(20), "ON_SHELF");
        expiredItem = createTestItem("ITEM003", "Expired Item", new BigDecimal("20.00"),
                10, LocalDate.now().minusDays(5), "EXPIRED");
    }

    @Test
    @DisplayName("Should add new stock item when item does not exist")
    void should_add_new_stock_item_when_item_does_not_exist() {
        // Arrange
        when(itemGateway.findByCode("NEW001")).thenReturn(null);

        // Act
        inventoryService.addStock("NEW001", "New Item", new BigDecimal("25.00"),
                50, LocalDate.now().plusDays(60));

        // Assert
        verify(itemGateway).findByCode("NEW001");
        verify(itemGateway).insert(any(Item.class));
        verify(itemGateway, never()).update(any(Item.class));
    }

    @Test
    @DisplayName("Should update existing stock quantity when item exists")
    void should_update_existing_stock_quantity_when_item_exists() {
        // Arrange
        when(itemGateway.findByCode("ITEM001")).thenReturn(testItem);

        // Act
        inventoryService.addStock("ITEM001", "Test Item", new BigDecimal("10.00"),
                50, LocalDate.now().plusDays(30));

        // Assert
        verify(itemGateway).findByCode("ITEM001");
        verify(itemGateway).update(any(Item.class));
        verify(itemGateway, never()).insert(any(Item.class));
    }

    @Test
    @DisplayName("Should move items from store to shelf successfully")
    void should_move_items_from_store_to_shelf_successfully() {
        // Arrange
        when(itemGateway.findByCode("ITEM001")).thenReturn(testItem);
        when(itemGateway.findByCode("ITEM001_SHELF")).thenReturn(null);

        // Act
        inventoryService.moveToShelf("ITEM001", 30);

        // Assert
        verify(itemGateway).findByCode("ITEM001");
        verify(itemGateway).findByCode("ITEM001_SHELF");
        verify(testItem).moveToShelf(30);
        verify(itemGateway).insert(any(Item.class)); // New shelf item
        verify(itemGateway).update(testItem); // Updated store item
    }

    @Test
    @DisplayName("Should update existing shelf item when moving items to shelf")
    void should_update_existing_shelf_item_when_moving_items_to_shelf() {
        // Arrange
        when(itemGateway.findByCode("ITEM001")).thenReturn(testItem);
        when(itemGateway.findByCode("ITEM001_SHELF")).thenReturn(shelfItem);

        // Act
        inventoryService.moveToShelf("ITEM001", 20);

        // Assert
        verify(itemGateway).findByCode("ITEM001");
        verify(itemGateway).findByCode("ITEM001_SHELF");
        verify(testItem).moveToShelf(20);
        verify(itemGateway).update(any(Item.class)); // Updated shelf item
        verify(itemGateway).update(testItem); // Updated store item
        verify(itemGateway, never()).insert(any(Item.class));
    }

    @Test
    @DisplayName("Should throw ItemNotFoundException when moving non-existent item to shelf")
    void should_throw_item_not_found_exception_when_moving_non_existent_item_to_shelf() {
        // Arrange
        when(itemGateway.findByCode("NONEXISTENT")).thenReturn(null);

        // Act & Assert
        ItemNotFoundException exception = assertThrows(ItemNotFoundException.class, () ->
                inventoryService.moveToShelf("NONEXISTENT", 10));

        assertTrue(exception.getMessage().contains("Item not found: NONEXISTENT"));
        verify(itemGateway, never()).update(any(Item.class));
        verify(itemGateway, never()).insert(any(Item.class));
    }

    @Test
    @DisplayName("Should get all items from gateway")
    void should_get_all_items_from_gateway() {
        // Arrange
        List<Item> allItems = Arrays.asList(testItem, shelfItem, expiredItem);
        when(itemGateway.findAll()).thenReturn(allItems);

        // Act
        List<Item> result = inventoryService.getAllItems();

        // Assert
        assertEquals(3, result.size());
        assertTrue(result.contains(testItem));
        assertTrue(result.contains(shelfItem));
        assertTrue(result.contains(expiredItem));
        verify(itemGateway).findAll();
    }

    @Test
    @DisplayName("Should filter and return only items in store")
    void should_filter_and_return_only_items_in_store() {
        // Arrange
        List<Item> allItems = Arrays.asList(testItem, shelfItem, expiredItem);
        when(itemGateway.findAll()).thenReturn(allItems);

        // Act
        List<Item> inStoreItems = inventoryService.getItemsInStore();

        // Assert
        assertEquals(1, inStoreItems.size());
        assertTrue(inStoreItems.contains(testItem));
        assertFalse(inStoreItems.contains(shelfItem));
        assertFalse(inStoreItems.contains(expiredItem));
    }

    @Test
    @DisplayName("Should filter and return only items on shelf")
    void should_filter_and_return_only_items_on_shelf() {
        // Arrange
        List<Item> allItems = Arrays.asList(testItem, shelfItem, expiredItem);
        when(itemGateway.findAll()).thenReturn(allItems);

        // Act
        List<Item> onShelfItems = inventoryService.getItemsOnShelf();

        // Assert
        assertEquals(1, onShelfItems.size());
        assertTrue(onShelfItems.contains(shelfItem));
        assertFalse(onShelfItems.contains(testItem));
        assertFalse(onShelfItems.contains(expiredItem));
    }

    @Test
    @DisplayName("Should get item by code from gateway")
    void should_get_item_by_code_from_gateway() {
        // Arrange
        when(itemGateway.findByCode("ITEM001")).thenReturn(testItem);

        // Act
        Item result = inventoryService.getItemByCode("ITEM001");

        // Assert
        assertEquals(testItem, result);
        verify(itemGateway).findByCode("ITEM001");
    }

    @Test
    @DisplayName("Should get low stock items using reorder threshold")
    void should_get_low_stock_items_using_reorder_threshold() {
        // Arrange
        List<Item> lowStockItems = Arrays.asList(shelfItem); // quantity 25 < 50
        when(itemGateway.findLowStock(50)).thenReturn(lowStockItems);

        // Act
        List<Item> result = inventoryService.getLowStockItems();

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.contains(shelfItem));
        verify(itemGateway).findLowStock(50);
    }

    @Test
    @DisplayName("Should get expiring items with custom days ahead")
    void should_get_expiring_items_with_custom_days_ahead() {
        // Arrange
        List<Item> expiringItems = Arrays.asList(shelfItem);
        when(itemGateway.findExpiringSoon(15)).thenReturn(expiringItems);

        // Act
        List<Item> result = inventoryService.getExpiringItems(15);

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.contains(shelfItem));
        verify(itemGateway).findExpiringSoon(15);
    }

    @Test
    @DisplayName("Should get expiring items with default warning period")
    void should_get_expiring_items_with_default_warning_period() {
        // Arrange
        List<Item> expiringItems = Arrays.asList(shelfItem);
        when(itemGateway.findExpiringSoon(7)).thenReturn(expiringItems);

        // Act
        List<Item> result = inventoryService.getExpiringItems();

        // Assert
        assertEquals(1, result.size());
        verify(itemGateway).findExpiringSoon(7); // Default EXPIRY_WARNING_DAYS
    }

    @Test
    @DisplayName("Should check and update expired items")
    void should_check_and_update_expired_items() {
        // Arrange
        Item expiredButNotMarked = createTestItem("ITEM004", "Should Expire",
                new BigDecimal("5.00"), 15,
                LocalDate.now().minusDays(2), "IN_STORE");
        when(expiredButNotMarked.isExpired()).thenReturn(true);

        List<Item> allItems = Arrays.asList(testItem, expiredButNotMarked);
        when(itemGateway.findAll()).thenReturn(allItems);

        // Act
        inventoryService.checkAndUpdateExpiredItems();

        // Assert
        verify(expiredButNotMarked).expire();
        verify(itemGateway).update(expiredButNotMarked);
        verify(itemGateway, never()).update(testItem); // Not expired
    }

    @Test
    @DisplayName("Should update item price successfully")
    void should_update_item_price_successfully() {
        // Arrange
        when(itemGateway.findByCode("ITEM001")).thenReturn(testItem);
        BigDecimal newPrice = new BigDecimal("15.00");

        // Act
        inventoryService.updateItemPrice("ITEM001", newPrice);

        // Assert
        verify(itemGateway).findByCode("ITEM001");
        verify(itemGateway).update(any(Item.class));
    }

    @Test
    @DisplayName("Should throw ItemNotFoundException when updating price of non-existent item")
    void should_throw_item_not_found_exception_when_updating_price_of_non_existent_item() {
        // Arrange
        when(itemGateway.findByCode("NONEXISTENT")).thenReturn(null);

        // Act & Assert
        ItemNotFoundException exception = assertThrows(ItemNotFoundException.class, () ->
                inventoryService.updateItemPrice("NONEXISTENT", new BigDecimal("20.00")));

        assertTrue(exception.getMessage().contains("Item not found: NONEXISTENT"));
        verify(itemGateway, never()).update(any(Item.class));
    }

    @Test
    @DisplayName("Should calculate total inventory value excluding expired items")
    void should_calculate_total_inventory_value_excluding_expired_items() {
        // Arrange
        List<Item> allItems = Arrays.asList(testItem, shelfItem, expiredItem);
        when(itemGateway.findAll()).thenReturn(allItems);

        // Act
        BigDecimal totalValue = inventoryService.getTotalInventoryValue();

        // Assert
        // testItem: 10.00 * 100 = 1000.00
        // shelfItem: 15.00 * 25 = 375.00
        // expiredItem: excluded (EXPIRED state)
        // Total: 1375.00
        assertEquals(new BigDecimal("1375.00"), totalValue);
    }

    @Test
    @DisplayName("Should generate comprehensive inventory statistics")
    void should_generate_comprehensive_inventory_statistics() {
        // Arrange
        Item lowStockItem = createTestItem("ITEM005", "Low Stock", new BigDecimal("8.00"),
                20, LocalDate.now().plusDays(30), "ON_SHELF");
        Item expiringItem = createTestItem("ITEM006", "Expiring Soon", new BigDecimal("12.00"),
                40, LocalDate.now().plusDays(5), "ON_SHELF");

        List<Item> allItems = Arrays.asList(testItem, shelfItem, expiredItem, lowStockItem, expiringItem);
        List<Item> lowStockItems = Arrays.asList(lowStockItem, shelfItem); // quantity < 50
        List<Item> expiringItems = Arrays.asList(expiringItem);

        when(itemGateway.findAll()).thenReturn(allItems);
        when(itemGateway.findLowStock(50)).thenReturn(lowStockItems);
        when(itemGateway.findExpiringSoon(7)).thenReturn(expiringItems);

        // Act
        InventoryService.InventoryStatistics stats = inventoryService.getInventoryStatistics();

        // Assert
        assertEquals(5, stats.totalItems);
        assertEquals(195, stats.totalQuantity); // 100+25+10+20+40
        assertEquals(1, stats.expiredCount);
        assertEquals(2, stats.lowStockCount);
        assertEquals(1, stats.expiringCount);
        // Total value: 1000 + 375 + 160 + 480 = 2015 (excluding expired item)
        assertEquals(new BigDecimal("2015.00"), stats.totalValue);
    }

    // Helper method to create test items
    private Item createTestItem(String code, String name, BigDecimal price, int quantity,
                                LocalDate expiryDate, String stateName) {
        Item item = mock(Item.class);
        ItemState state = mock(ItemState.class);

        when(item.getCode()).thenReturn(new ItemCode(code));
        when(item.getName()).thenReturn(name);
        when(item.getPrice()).thenReturn(new Money(price));
        when(item.getQuantity()).thenReturn(new Quantity(quantity));
        when(item.getExpiryDate()).thenReturn(expiryDate);
        when(item.getState()).thenReturn(state);
        when(state.getStateName()).thenReturn(stateName);
        when(item.getPurchaseDate()).thenReturn(LocalDate.now());

        // Setup expiry logic
        when(item.isExpired()).thenReturn(expiryDate != null && expiryDate.isBefore(LocalDate.now()));

        return item;
    }
}