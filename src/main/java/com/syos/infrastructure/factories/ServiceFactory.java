package com.syos.infrastructure.factories;

import com.syos.application.services.*;
import com.syos.infrastructure.persistence.gateways.*;
import com.syos.infrastructure.persistence.connection.DatabaseConnectionPool;

public class ServiceFactory {
    private static ServiceFactory instance;
    private final DatabaseConnectionPool connectionPool;

    private SalesService salesService;
    private InventoryService inventoryService;
    private ReportService reportService;
    private UserService userService;

    private ServiceFactory() {
        this.connectionPool = DatabaseConnectionPool.getInstance();
        initializeServices();
    }

    public static ServiceFactory getInstance() {
        if (instance == null) {
            instance = new ServiceFactory();
        }
        return instance;
    }

    private void initializeServices() {
        // Initialize gateways
        var itemGateway = new ItemGateway(connectionPool);
        var billGateway = new BillGateway(connectionPool);
        var userGateway = new UserGateway(connectionPool);

        // Initialize services with gateways
        this.inventoryService = new InventoryService(itemGateway);
        this.salesService = new SalesService(billGateway, itemGateway);
        this.reportService = new ReportService(billGateway, itemGateway);
        this.userService = new UserService(userGateway);
    }

    public SalesService getSalesService() { return salesService; }
    public InventoryService getInventoryService() { return inventoryService; }
    public ReportService getReportService() { return reportService; }
    public UserService getUserService() { return userService; }
}
