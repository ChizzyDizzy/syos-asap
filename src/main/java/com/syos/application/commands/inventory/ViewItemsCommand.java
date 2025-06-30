package com.syos.application.commands.inventory;

import com.syos.application.interfaces.Command;
import com.syos.application.services.InventoryService;
import com.syos.infrastructure.ui.presenters.InventoryPresenter;

public class ViewItemsCommand implements Command {
    private final InventoryService inventoryService;
    private final InventoryPresenter presenter;

    public ViewItemsCommand(InventoryService inventoryService,
                            InventoryPresenter presenter) {
        this.inventoryService = inventoryService;
        this.presenter = presenter;
    }

    @Override
    public void execute() {
        try {
            var items = inventoryService.getAllItems();
            presenter.showItems(items);
        } catch (Exception e) {
            presenter.showError("Failed to retrieve items: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "View All Items";
    }
}

