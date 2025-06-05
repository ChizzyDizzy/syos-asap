package com.syos.infrastructure.factories;

import com.syos.infrastructure.ui.presenters.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class PresenterFactoryTest {

    @BeforeEach
    void setUp() throws Exception {
        // Reset singleton instance before each test to ensure test isolation
        Field instanceField = PresenterFactory.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    @Test
    void should_implement_singleton_pattern_correctly() {
        // Act - Get multiple instances
        PresenterFactory instance1 = PresenterFactory.getInstance();
        PresenterFactory instance2 = PresenterFactory.getInstance();
        PresenterFactory instance3 = PresenterFactory.getInstance();

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
    }

    @Test
    void should_provide_all_presenter_instances_through_factory_methods() {
        // Arrange
        PresenterFactory factory = PresenterFactory.getInstance();

        // Act - Get all presenters
        SalesPresenter salesPresenter = factory.getSalesPresenter();
        InventoryPresenter inventoryPresenter = factory.getInventoryPresenter();
        ReportPresenter reportPresenter = factory.getReportPresenter();
        UserPresenter userPresenter = factory.getUserPresenter();

        // Assert - All presenters should be properly instantiated
        assertNotNull(salesPresenter, "SalesPresenter should not be null");
        assertNotNull(inventoryPresenter, "InventoryPresenter should not be null");
        assertNotNull(reportPresenter, "ReportPresenter should not be null");
        assertNotNull(userPresenter, "UserPresenter should not be null");

        // Verify correct types
        assertInstanceOf(SalesPresenter.class, salesPresenter);
        assertInstanceOf(InventoryPresenter.class, inventoryPresenter);
        assertInstanceOf(ReportPresenter.class, reportPresenter);
        assertInstanceOf(UserPresenter.class, userPresenter);

        // Verify consistency - same factory should return same presenter instances
        assertSame(salesPresenter, factory.getSalesPresenter(),
                "Factory should return the same SalesPresenter instance");
        assertSame(inventoryPresenter, factory.getInventoryPresenter(),
                "Factory should return the same InventoryPresenter instance");
        assertSame(reportPresenter, factory.getReportPresenter(),
                "Factory should return the same ReportPresenter instance");
        assertSame(userPresenter, factory.getUserPresenter(),
                "Factory should return the same UserPresenter instance");
    }

    @Test
    void should_maintain_presenter_instances_across_multiple_factory_accesses() {
        // Arrange - Get factory instance and initial presenters
        PresenterFactory factory1 = PresenterFactory.getInstance();
        SalesPresenter salesPresenter1 = factory1.getSalesPresenter();
        InventoryPresenter inventoryPresenter1 = factory1.getInventoryPresenter();
        ReportPresenter reportPresenter1 = factory1.getReportPresenter();
        UserPresenter userPresenter1 = factory1.getUserPresenter();

        // Act - Get factory instance again and presenters again
        PresenterFactory factory2 = PresenterFactory.getInstance();
        SalesPresenter salesPresenter2 = factory2.getSalesPresenter();
        InventoryPresenter inventoryPresenter2 = factory2.getInventoryPresenter();
        ReportPresenter reportPresenter2 = factory2.getReportPresenter();
        UserPresenter userPresenter2 = factory2.getUserPresenter();

        // Assert - Factory instances should be the same
        assertSame(factory1, factory2, "Factory instances should be the same");

        // Assert - Presenter instances should be the same across factory accesses
        assertSame(salesPresenter1, salesPresenter2,
                "SalesPresenter should be the same instance across factory accesses");
        assertSame(inventoryPresenter1, inventoryPresenter2,
                "InventoryPresenter should be the same instance across factory accesses");
        assertSame(reportPresenter1, reportPresenter2,
                "ReportPresenter should be the same instance across factory accesses");
        assertSame(userPresenter1, userPresenter2,
                "UserPresenter should be the same instance across factory accesses");

        // Verify all presenters are distinct from each other
        assertNotSame(salesPresenter1, inventoryPresenter1,
                "Different presenter types should be different instances");
        assertNotSame(salesPresenter1, reportPresenter1,
                "Different presenter types should be different instances");
        assertNotSame(salesPresenter1, userPresenter1,
                "Different presenter types should be different instances");
        assertNotSame(inventoryPresenter1, reportPresenter1,
                "Different presenter types should be different instances");
        assertNotSame(inventoryPresenter1, userPresenter1,
                "Different presenter types should be different instances");
        assertNotSame(reportPresenter1, userPresenter1,
                "Different presenter types should be different instances");

        // Verify that all presenters are functional (basic smoke test)
        assertDoesNotThrow(() -> {
            // These calls should not throw exceptions if presenters are properly initialized
            assertNotNull(salesPresenter1.toString());
            assertNotNull(inventoryPresenter1.toString());
            assertNotNull(reportPresenter1.toString());
            assertNotNull(userPresenter1.toString());
        }, "All presenters should be properly initialized and functional");
    }
}