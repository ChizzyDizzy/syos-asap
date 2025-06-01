package com.syos.infrastructure.persistence.mappers;

import com.syos.domain.entities.*;
import com.syos.domain.valueobjects.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BillMapper {

    public Bill mapRow(ResultSet rs) throws SQLException {
        return new Bill.Builder()
                .withBillNumber(rs.getInt("bill_number"))
                .withDate(rs.getTimestamp("bill_date").toLocalDateTime())
                .withDiscount(rs.getBigDecimal("discount"))
                .withCashTendered(rs.getBigDecimal("cash_tendered"))
                .withTransactionType(TransactionType.valueOf(rs.getString("transaction_type")))
                .build();
    }
}