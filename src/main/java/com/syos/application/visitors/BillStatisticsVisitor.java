package com.syos.application.visitors;

import com.syos.domain.entities.*;
import com.syos.domain.valueobjects.Money;
import java.math.BigDecimal;
import java.util.*;

public class BillStatisticsVisitor implements BillVisitor {
    private int billCount = 0;
    private Money totalRevenue = new Money(BigDecimal.ZERO);
    private Money totalDiscount = new Money(BigDecimal.ZERO);
    private Map<String, Integer> itemFrequency = new HashMap<>();

    @Override
    public void visit(Bill bill) {
        billCount++;
        totalRevenue = totalRevenue.add(bill.getFinalAmount());
        totalDiscount = totalDiscount.add(bill.getDiscount());

        for (BillItem item : bill.getItems()) {
            String itemName = item.getItem().getName();
            itemFrequency.merge(itemName, item.getQuantity().getValue(), Integer::sum);
        }
    }

    public int getBillCount() {
        return billCount;
    }

    public Money getTotalRevenue() {
        return totalRevenue;
    }

    public Money getAverageTransaction() {
        if (billCount == 0) {
            return new Money(BigDecimal.ZERO);
        }
        return new Money(totalRevenue.getValue().divide(
                BigDecimal.valueOf(billCount), 2, BigDecimal.ROUND_HALF_UP));
    }

    public Money getTotalDiscount() {
        return totalDiscount;
    }

    public Map<String, Integer> getItemFrequency() {
        return new HashMap<>(itemFrequency);
    }

    public String getMostPopularItem() {
        return itemFrequency.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
    }
}