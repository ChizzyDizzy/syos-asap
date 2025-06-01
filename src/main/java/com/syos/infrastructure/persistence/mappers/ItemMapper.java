package com.syos.infrastructure.persistence.mappers;

import com.syos.domain.entities.*;
import com.syos.domain.valueobjects.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;

public class ItemMapper {

    public Item mapRow(ResultSet rs) throws SQLException {
        return new Item.Builder()
                .withCode(rs.getString("code"))
                .withName(rs.getString("name"))
                .withPrice(rs.getBigDecimal("price"))
                .withQuantity(rs.getInt("quantity"))
                .withState(mapState(rs.getString("state")))
                .withPurchaseDate(rs.getDate("purchase_date").toLocalDate())
                .withExpiryDate(rs.getDate("expiry_date") != null ?
                        rs.getDate("expiry_date").toLocalDate() : null)
                .build();
    }

    private ItemState mapState(String stateName) {
        return switch (stateName) {
            case "IN_STORE" -> new InStoreState();
            case "ON_SHELF" -> new OnShelfState();
            case "EXPIRED" -> new ExpiredState();
            case "SOLD_OUT" -> new SoldOutState();
            default -> new InStoreState();
        };
    }
}