package com.syos.infrastructure.persistence.mappers;

import com.syos.domain.entities.*;
import com.syos.domain.interfaces.ItemState;
import com.syos.domain.valueobjects.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Mapper for converting ResultSet to Bill entities
 */
public class BillMapper {

    /**
     * Map a ResultSet row to a Bill entity
     * @param rs The ResultSet positioned at a valid row
     * @return A Bill entity
     * @throws SQLException if database access error occurs
     */
    public Bill mapRow(ResultSet rs) throws SQLException {
        // Create the bill without items first
        Bill.Builder builder = new Bill.Builder()
                .withBillNumber(rs.getInt("bill_number"))
                .withDate(rs.getTimestamp("bill_date").toLocalDateTime())
                .withDiscount(rs.getBigDecimal("discount"))
                .withCashTendered(rs.getBigDecimal("cash_tendered"))
                .withTransactionType(TransactionType.valueOf(rs.getString("transaction_type")));

        // Note: Items will be added separately when joining with bill_items table
        return builder.build();
    }

    /**
     * Map a ResultSet row to a BillItem entity
     * This assumes the ResultSet contains joined data from bill_items and items tables
     * @param rs The ResultSet positioned at a valid row with bill_items and items data
     * @return A BillItem entity
     * @throws SQLException if database access error occurs
     */
    public BillItem mapBillItem(ResultSet rs) throws SQLException {
        // First, map the Item from the joined items table data
        Item item = mapItem(rs);

        // Then create the BillItem with the quantity from bill_items table
        int quantity = rs.getInt("quantity"); // This is from bill_items table

        return new BillItem(item, quantity);
    }

    /**
     * Map item data from ResultSet
     * This is used when the ResultSet contains item data from a JOIN
     * @param rs The ResultSet containing item data
     * @return An Item entity
     * @throws SQLException if database access error occurs
     */
    private Item mapItem(ResultSet rs) throws SQLException {
        Item.Builder itemBuilder = new Item.Builder()
                .withCode(rs.getString("code"))
                .withName(rs.getString("name"))
                .withPrice(rs.getBigDecimal("price"))
                .withQuantity(rs.getInt("quantity")) // This is the item's current quantity
                .withPurchaseDate(rs.getDate("purchase_date").toLocalDate());

        // Handle optional expiry date
        java.sql.Date expiryDate = rs.getDate("expiry_date");
        if (expiryDate != null) {
            itemBuilder.withExpiryDate(expiryDate.toLocalDate());
        }

        // Set the appropriate state
        String stateName = rs.getString("state");
        ItemState state = mapState(stateName);
        itemBuilder.withState(state);

        return itemBuilder.build();
    }

    /**
     * Map state name to ItemState instance
     * @param stateName The state name from database
     * @return The corresponding ItemState instance
     */
    private ItemState mapState(String stateName) {
        return switch (stateName) {
            case "IN_STORE" -> new InStoreState();
            case "ON_SHELF" -> new OnShelfState();
            case "EXPIRED" -> new ExpiredState();
            case "SOLD_OUT" -> new SoldOutState();
            default -> throw new IllegalArgumentException("Unknown state: " + stateName);
        };
    }

    /**
     * Map a complete Bill with items from multiple ResultSet rows
     * This method is useful when you need to build a complete bill from multiple rows
     * @param rs The ResultSet containing bill and bill_items data
     * @return A complete Bill with all its items
     * @throws SQLException if database access error occurs
     */
    public Bill mapFullBill(ResultSet rs) throws SQLException {
        Bill bill = null;

        while (rs.next()) {
            if (bill == null) {
                // First row - create the bill
                bill = mapRow(rs);
            }

            // If there's item data in this row, add it to the bill
            if (rs.getString("item_code") != null) {
                BillItem billItem = mapBillItem(rs);
                bill.getItems().add(billItem);
            }
        }

        return bill;
    }

    /**
     * Create a simple Bill without items (for cases where items are loaded separately)
     * @param billNumber The bill number
     * @param billDate The bill date
     * @param totalAmount The total amount
     * @param discount The discount amount
     * @param cashTendered The cash tendered
     * @param changeAmount The change amount
     * @param transactionType The transaction type
     * @return A Bill entity without items
     */
    public Bill createBillWithoutItems(
            int billNumber,
            LocalDateTime billDate,
            java.math.BigDecimal totalAmount,
            java.math.BigDecimal discount,
            java.math.BigDecimal cashTendered,
            java.math.BigDecimal changeAmount,
            String transactionType) {

        return new Bill.Builder()
                .withBillNumber(billNumber)
                .withDate(billDate)
                .withDiscount(discount)
                .withCashTendered(cashTendered)
                .withTransactionType(TransactionType.valueOf(transactionType))
                .build();
    }
}