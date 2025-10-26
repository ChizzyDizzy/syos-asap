package com.syos.web.exception;

/**
 * ============================================
 * INSUFFICIENT STOCK EXCEPTION
 * ============================================
 *
 * Thrown when a stock operation fails due to insufficient quantity:
 * - Sale requests more items than available
 * - Stock move exceeds available quantity
 * - Negative stock would result from operation
 *
 * Provides detailed information about:
 * - Which product has insufficient stock
 * - How much was requested
 * - How much is actually available
 *
 * This is a RuntimeException for easier handling in service layer
 *
 * ============================================
 */
public class InsufficientStockException extends RuntimeException {

    private final String productCode;
    private final String productName;
    private final int requestedQuantity;
    private final int availableQuantity;
    private final String stockType; // "SHELF" or "STORE"

    /**
     * Constructor with product code and quantities
     */
    public InsufficientStockException(String productCode, int requestedQuantity,
                                      int availableQuantity) {
        super(String.format(
                "Insufficient stock for product %s: requested %d, available %d",
                productCode, requestedQuantity, availableQuantity
        ));
        this.productCode = productCode;
        this.productName = null;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
        this.stockType = "SHELF";
    }

    /**
     * Constructor with full product details
     */
    public InsufficientStockException(String productCode, String productName,
                                      int requestedQuantity, int availableQuantity,
                                      String stockType) {
        super(String.format(
                "Insufficient stock for product %s (%s): requested %d, available %d in %s",
                productCode, productName, requestedQuantity, availableQuantity, stockType
        ));
        this.productCode = productCode;
        this.productName = productName;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
        this.stockType = stockType;
    }

    /**
     * Get product code
     */
    public String getProductCode() {
        return productCode;
    }

    /**
     * Get product name
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Get requested quantity
     */
    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    /**
     * Get available quantity
     */
    public int getAvailableQuantity() {
        return availableQuantity;
    }

    /**
     * Get stock type (SHELF or STORE)
     */
    public String getStockType() {
        return stockType;
    }

    /**
     * Get shortage amount
     */
    public int getShortage() {
        return requestedQuantity - availableQuantity;
    }

    /**
     * Get user-friendly error message
     */
    public String getUserMessage() {
        if (productName != null) {
            return String.format(
                    "Cannot complete operation: Product '%s' (%s) has only %d units available in %s, but %d units were requested.",
                    productName, productCode, availableQuantity, stockType.toLowerCase(), requestedQuantity
            );
        } else {
            return String.format(
                    "Cannot complete operation: Product %s has only %d units available, but %d units were requested.",
                    productCode, availableQuantity, requestedQuantity
            );
        }
    }

    /**
     * Get detailed error message with suggestions
     */
    public String getDetailedMessage() {
        StringBuilder sb = new StringBuilder(getUserMessage());
        sb.append("\n\nSuggestions:\n");

        if (availableQuantity > 0) {
            sb.append(String.format("- Reduce quantity to %d or less\n", availableQuantity));
        }

        if ("SHELF".equals(stockType)) {
            sb.append("- Check if more stock is available in store and reshelve\n");
        }

        sb.append("- Check with inventory manager for reorder\n");

        return sb.toString();
    }
}