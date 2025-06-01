package com.syos.infrastructure.factories;

import com.syos.infrastructure.ui.presenters.*;

public class PresenterFactory {
    private static PresenterFactory instance;

    private final SalesPresenter salesPresenter;
    private final InventoryPresenter inventoryPresenter;
    private final ReportPresenter reportPresenter;
    private final UserPresenter userPresenter;

    private PresenterFactory() {
        this.salesPresenter = new SalesPresenter();
        this.inventoryPresenter = new InventoryPresenter();
        this.reportPresenter = new ReportPresenter();
        this.userPresenter = new UserPresenter();
    }

    public static PresenterFactory getInstance() {
        if (instance == null) {
            instance = new PresenterFactory();
        }
        return instance;
    }

    public SalesPresenter getSalesPresenter() { return salesPresenter; }
    public InventoryPresenter getInventoryPresenter() { return inventoryPresenter; }
    public ReportPresenter getReportPresenter() { return reportPresenter; }
    public UserPresenter getUserPresenter() { return userPresenter; }
}