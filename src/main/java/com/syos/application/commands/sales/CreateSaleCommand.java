package com.syos.application.commands.sales;

import com.syos.application.interfaces.Command;
import com.syos.application.services.SalesService;
import com.syos.infrastructure.ui.presenters.SalesPresenter;
import com.syos.infrastructure.ui.cli.InputReader;

public class CreateSaleCommand implements Command {
    private final SalesService salesService;
    private final SalesPresenter presenter;
    private final InputReader inputReader;

    public CreateSaleCommand(SalesService salesService,
                             SalesPresenter presenter,
                             InputReader inputReader) {
        this.salesService = salesService;
        this.presenter = presenter;
        this.inputReader = inputReader;
    }

    @Override
    public void execute() {
        presenter.showSaleHeader();

        // Build the sale
        var saleBuilder = salesService.startNewSale();

        boolean addingItems = true;
        while (addingItems) {
            String itemCode = inputReader.readString("Enter item code (or 'DONE' to finish): ");

            if ("DONE".equalsIgnoreCase(itemCode)) {
                addingItems = false;
                continue;
            }

            try {
                int quantity = inputReader.readInt("Enter quantity: ");
                saleBuilder.addItem(itemCode, quantity);
                presenter.showItemAdded(itemCode, quantity);
            } catch (Exception e) {
                presenter.showError(e.getMessage());
            }
        }

        // Process payment
        try {
            var subtotal = saleBuilder.getSubtotal();
            presenter.showSubtotal(subtotal);

            var cashTendered = inputReader.readBigDecimal("Enter cash amount: ");
            var bill = saleBuilder.completeSale(cashTendered);

            presenter.showBill(bill);
            salesService.saveBill(bill);

        } catch (Exception e) {
            presenter.showError("Sale failed: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "Create New Sale";
    }
}