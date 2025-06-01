package com.syos.application.commands.sales;

import com.syos.application.interfaces.Command;
import com.syos.application.services.SalesService;
import com.syos.infrastructure.ui.presenters.SalesPresenter;

public class ViewBillsCommand implements Command {
    public ViewBillsCommand(SalesService salesService, SalesPresenter salesPresenter) {
    }

    @Override
    public void execute() {

    }

    @Override
    public String getDescription() {
        return "";
    }
}
