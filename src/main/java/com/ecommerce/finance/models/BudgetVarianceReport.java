package com.ecommerce.finance.models;

import java.util.List;
import java.util.stream.Collectors;

public class BudgetVarianceReport {
    private List<BudgetVarianceRow> rows;

    public BudgetVarianceReport(List<BudgetVarianceRow> rows) {
        this.rows = rows;
    }

    public List<BudgetVarianceRow> getRows() { return rows; }

    public List<BudgetVarianceRow> getOverBudgetRows() {
        return rows.stream().filter(BudgetVarianceRow::isOverBudget).collect(Collectors.toList());
    }

    public List<BudgetVarianceRow> getOnBudgetRows() {
        return rows.stream().filter(r -> !r.isOverBudget()).collect(Collectors.toList());
    }

    public boolean hasOverBudgetItems() {
        return !getOverBudgetRows().isEmpty();
    }

    public boolean isEmpty() {
        return rows == null || rows.isEmpty();
    }
}