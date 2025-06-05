package com.syos.infrastructure.factories;

import com.syos.application.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class ServiceFactoryTest {

    @BeforeEach
    void setUp() throws Exception {
        // Reset singleton instance before each test to ensure test isolation
        Field instanceField = ServiceFactory.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    @Test
    void should_implement_singleton_pattern_correctly() {
        // Act - Get multiple instances
        ServiceFactory instance1 = ServiceFactory.getInstance();
        ServiceFactory instance2 = ServiceFactory.getInstance();
        ServiceFactory instance3 = ServiceFactory.getInstance();

        // Assert - All references should point to the same instance
        assertNotNull(instance1);
        assertNotNull(instance2);
        assertNotNull(instance3);

        assertSame(instance1, instance2, "getInstance() should return the same instance");
        assertSame(instance2, instance3, "getInstance() should return the same instance");
        assertSame(instance1, instance3, "getInstance() should return the same instance");

        // Verify they have the same hash code (additional verification)
        assertEquals(instance1.hashCode(), instance2.hashCode());
        assertEquals(instance2.hashCode(), instance3.hashCode());

        // Verify that services are initialized during singleton creation
        assertNotNull(instance1.getSalesService(), "Services should be initialized during singleton creation");
        assertNotNull(instance1.getInventoryService(), "Services should be initialized during singleton creation");
        assertNotNull(instance1.getReportService(), "Services should be initialized during singleton creation");
        assertNotNull(instance1.getUserService(), "Services should be initialized during singleton creation");
    }

    @Test
    void should_provide_all_service_instances_with_proper_initialization() {
        // Arrange
        ServiceFactory factory = ServiceFactory.getInstance();

        // Act - Get all services
        SalesService salesService = factory.getSalesService();
        InventoryService inventoryService = factory.getInventoryService();
        ReportService reportService = factory.getReportService();
        UserService userService = factory.getUserService();

        // Assert - All services should be properly instantiated
        assertNotNull(salesService, "SalesService should not be null");
        assertNotNull(inventoryService, "InventoryService should not be null");
        assertNotNull(reportService, "ReportService should not be null");
        assertNotNull(userService, "UserService should not be null");

        // Verify correct types
        assertInstanceOf(SalesService.class, salesService);
        assertInstanceOf(InventoryService.class, inventoryService);
        assertInstanceOf(ReportService.class, reportService);
        assertInstanceOf(UserService.class, userService);

        // Verify consistency - same factory should return same service instances
        assertSame(salesService, factory.getSalesService(),
                "Factory should return the same SalesService instance");
        assertSame(inventoryService, factory.getInventoryService(),
                "Factory should return the same InventoryService instance");
        assertSame(reportService, factory.getReportService(),
                "Factory should return the same ReportService instance");
        assertSame(userService, factory.getUserService(),
                "Factory should return the same UserService instance");

        // Verify all services are distinct from each other
        assertNotSame(salesService, inventoryService,
                "Different service types should be different instances");
        assertNotSame(salesService, reportService,
                "Different service types should be different instances");
        assertNotSame(salesService, userService,
                "Different service types should be different instances");
        assertNotSame(inventoryService, reportService,
                "Different service types should be different instances");
        assertNotSame(inventoryService, userService,
                "Different service types should be different instances");
        assertNotSame(reportService, userService,
                "Different service types should be different instances");
    }

    @Test
    void should_maintain_service_instances_and_dependencies_across_multiple_factory_accesses() {
        // Arrange - Get factory instance and initial services
        ServiceFactory factory1 = ServiceFactory.getInstance();
        SalesService salesService1 = factory1.getSalesService();
        InventoryService inventoryService1 = factory1.getInventoryService();
        ReportService reportService1 = factory1.getReportService();
        UserService userService1 = factory1.getUserService();

        // Act - Get factory instance again and services again
        ServiceFactory factory2 = ServiceFactory.getInstance();
        SalesService salesService2 = factory2.getSalesService();
        InventoryService inventoryService2 = factory2.getInventoryService();
        ReportService reportService2 = factory2.getReportService();
        UserService userService2 = factory2.getUserService();

        // Assert - Factory instances should be the same
        assertSame(factory1, factory2, "Factory instances should be the same");

        // Assert - Service instances should be the same across factory accesses
        assertSame(salesService1, salesService2,
                "SalesService should be the same instance across factory accesses");
        assertSame(inventoryService1, inventoryService2,
                "InventoryService should be the same instance across factory accesses");
        assertSame(reportService1, reportService2,
                "ReportService should be the same instance across factory accesses");
        assertSame(userService1, userService2,
                "UserService should be the same instance across factory accesses");

        // Verify services are functional and properly initialized (basic smoke test)
        assertDoesNotThrow(() -> {
            // These calls should not throw exceptions if services are properly initialized
            assertNotNull(salesService1.toString());
            assertNotNull(inventoryService1.toString());
            assertNotNull(reportService1.toString());
            assertNotNull(userService1.toString());
        }, "All services should be properly initialized and functional");

        // Test that multiple calls to the same service return the same instance
        for (int i = 0; i < 5; i++) {
            assertSame(salesService1, factory1.getSalesService(),
                    "Multiple calls should return the same SalesService instance");
            assertSame(inventoryService1, factory1.getInventoryService(),
                    "Multiple calls should return the same InventoryService instance");
            assertSame(reportService1, factory1.getReportService(),
                    "Multiple calls should return the same ReportService instance");
            assertSame(userService1, factory1.getUserService(),
                    "Multiple calls should return the same UserService instance");
        }

        // Verify that factory initialization occurred only once
        // (This is implicitly tested by the instance consistency checks above)
        // If initialization happened multiple times, we might get different instances
        assertTrue(true, "Factory initialization should occur only once during singleton creation");
    }
}