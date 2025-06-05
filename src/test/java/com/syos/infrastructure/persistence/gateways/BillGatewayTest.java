package com.syos.infrastructure.persistence.gateways;

import com.syos.domain.entities.Bill;
import com.syos.domain.entities.BillItem;
import com.syos.domain.entities.Item;
import com.syos.domain.valueobjects.*;
import com.syos.infrastructure.persistence.connection.DatabaseConnectionPool;
import com.syos.infrastructure.persistence.connection.ConnectionManager;
import com.syos.infrastructure.persistence.mappers.BillMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class BillGatewayTest {

    @Mock
    private DatabaseConnectionPool mockPool;

    @Mock
    private ConnectionManager mockConnectionManager;

    @Mock
    private BillMapper mockMapper;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private Bill mockBill;

    @Mock
    private BillItem mockBillItem;

    @Mock
    private Item mockItem;

    private BillGateway billGateway;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Setup common mock behavior
        when(mockBill.getBillDate()).thenReturn(LocalDateTime.of(2024, 6, 5, 14, 30));
        when(mockBill.getTotalAmount()).thenReturn(new Money(new BigDecimal("500.00")));
        when(mockBill.getDiscount()).thenReturn(new Money(new BigDecimal("50.00")));
        when(mockBill.getCashTendered()).thenReturn(new Money(new BigDecimal("500.00")));
        when(mockBill.getChange()).thenReturn(new Money(new BigDecimal("50.00")));
        when(mockBill.getTransactionType()).thenReturn(TransactionType.IN_STORE);

        billGateway = new BillGateway(mockPool);

        // Inject mock mapper using reflection since it's private
        java.lang.reflect.Field mapperField = BillGateway.class.getDeclaredField("mapper");
        mapperField.setAccessible(true);
        mapperField.set(billGateway, mockMapper);

        // Inject mock connection manager using reflection (inherited from parent)
        java.lang.reflect.Field connectionManagerField = OracleDatabaseGateway.class.getDeclaredField("connectionManager");
        connectionManagerField.setAccessible(true);
        connectionManagerField.set(billGateway, mockConnectionManager);
    }

    @Test
    void should_provide_correct_sql_statements_and_handle_unsupported_operations() throws SQLException {
        // Test SQL statement methods (these are template method implementations)

        // Test insert SQL
        String insertSQL = billGateway.getInsertSQL();
        assertTrue(insertSQL.contains("INSERT INTO bills"));
        assertTrue(insertSQL.contains("bill_date, total_amount, discount, cash_tendered, change_amount, transaction_type"));

        // Test delete SQL
        String deleteSQL = billGateway.getDeleteSQL();
        assertTrue(deleteSQL.contains("DELETE FROM bills WHERE bill_number = ?"));

        // Test find by ID SQL
        String findByIdSQL = billGateway.getFindByIdSQL();
        assertTrue(findByIdSQL.contains("SELECT * FROM bills WHERE bill_number = ?"));

        // Test unsupported update operations
        assertThrows(UnsupportedOperationException.class, () -> {
            billGateway.getUpdateSQL();
        });

        assertThrows(UnsupportedOperationException.class, () -> {
            billGateway.setUpdateParameters(mockPreparedStatement, mockBill);
        });

        // Test parameter setting for insert
        billGateway.setInsertParameters(mockPreparedStatement, mockBill);

        // Verify all parameters were set correctly
        verify(mockPreparedStatement).setTimestamp(eq(1), any(Timestamp.class));
        verify(mockPreparedStatement).setBigDecimal(2, new BigDecimal("500.00"));
        verify(mockPreparedStatement).setBigDecimal(3, new BigDecimal("50.00"));
        verify(mockPreparedStatement).setBigDecimal(4, new BigDecimal("500.00"));
        verify(mockPreparedStatement).setBigDecimal(5, new BigDecimal("50.00"));
        verify(mockPreparedStatement).setString(6, "IN_STORE");

        // Test result set mapping
        when(mockMapper.mapRow(mockResultSet)).thenReturn(mockBill);
        Bill mappedBill = billGateway.mapResultSetToEntity(mockResultSet);
        assertEquals(mockBill, mappedBill);
        verify(mockMapper).mapRow(mockResultSet);
    }

    @Test
    void should_save_bill_with_items_in_transaction_successfully() throws SQLException {
        // Arrange
        BillItem billItem1 = createMockBillItem("ITEM001", 2, new BigDecimal("100.00"));
        BillItem billItem2 = createMockBillItem("ITEM002", 3, new BigDecimal("150.00"));

        when(mockBill.getItems()).thenReturn(Arrays.asList(billItem1, billItem2));

        // Mock transaction execution
        doAnswer(invocation -> {
            ConnectionManager.TransactionCallback callback = invocation.getArgument(0);
            callback.execute(mockConnection);
            return null;
        }).when(mockConnectionManager).executeWithTransaction(any());

        // Mock prepared statement creation and execution
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);
        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        // Mock generated keys
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong(1)).thenReturn(12345L);

        // Act
        billGateway.saveBillWithItems(mockBill);

        // Assert
        verify(mockConnectionManager).executeWithTransaction(any());

        // Verify bill insert parameters were set
        verify(mockPreparedStatement).setTimestamp(eq(1), any(Timestamp.class));
        verify(mockPreparedStatement).setBigDecimal(2, new BigDecimal("500.00"));
        verify(mockPreparedStatement).setBigDecimal(3, new BigDecimal("50.00"));
        verify(mockPreparedStatement).setBigDecimal(4, new BigDecimal("500.00"));
        verify(mockPreparedStatement).setBigDecimal(5, new BigDecimal("50.00"));
        verify(mockPreparedStatement).setString(6, "IN_STORE");

        // Verify bill items batch operations
        verify(mockPreparedStatement, times(2)).addBatch(); // Two items
        verify(mockPreparedStatement).executeBatch();

        // Verify bill items parameters were set
        verify(mockPreparedStatement, times(2)).setLong(1, 12345L); // Bill ID for both items
        verify(mockPreparedStatement).setString(2, "ITEM001");
        verify(mockPreparedStatement).setString(2, "ITEM002");
        verify(mockPreparedStatement).setInt(3, 2); // Quantity for item1
        verify(mockPreparedStatement).setInt(3, 3); // Quantity for item2
    }

    @Test
    void should_find_bills_by_date_correctly() throws SQLException {
        // Arrange
        LocalDate searchDate = LocalDate.of(2024, 6, 5);
        Bill bill1 = createMockBill(1);
        Bill bill2 = createMockBill(2);
        List<Bill> expectedBills = Arrays.asList(bill1, bill2);

        // Mock connection execution
        doAnswer(invocation -> {
            ConnectionManager.ConnectionCallback<?> callback = invocation.getArgument(0);
            return callback.execute(mockConnection);
        }).when(mockConnectionManager).executeWithConnection(any());

        // Mock prepared statement and result set
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // Mock result set iteration
        when(mockResultSet.next()).thenReturn(true, true, false); // Two rows then end
        when(mockMapper.mapRow(mockResultSet)).thenReturn(bill1, bill2);

        // Act
        List<Bill> result = billGateway.findByDate(searchDate);

        // Assert
        assertEquals(2, result.size());
        assertEquals(bill1, result.get(0));
        assertEquals(bill2, result.get(1));

        // Verify connection and statement operations
        verify(mockConnectionManager).executeWithConnection(any());
        verify(mockConnection).prepareStatement("SELECT * FROM bills WHERE DATE(bill_date) = ?");
        verify(mockPreparedStatement).setDate(1, Date.valueOf(searchDate));
        verify(mockPreparedStatement).executeQuery();

        // Verify result set mapping
        verify(mockMapper, times(2)).mapRow(mockResultSet);
        verify(mockResultSet, times(3)).next(); // Called until returns false

        // Verify resources are properly closed (try-with-resources)
        verify(mockPreparedStatement).close();
        verify(mockResultSet).close();
    }

    // Helper method to create mock BillItem
    private BillItem createMockBillItem(String itemCode, int quantity, BigDecimal unitPrice) {
        BillItem billItem = mock(BillItem.class);
        Item item = mock(Item.class);
        ItemCode code = new ItemCode(itemCode);
        Quantity qty = new Quantity(quantity);
        Money price = new Money(unitPrice);
        Money totalPrice = new Money(unitPrice.multiply(BigDecimal.valueOf(quantity)));

        when(billItem.getItem()).thenReturn(item);
        when(billItem.getQuantity()).thenReturn(qty);
        when(billItem.getTotalPrice()).thenReturn(totalPrice);
        when(item.getCode()).thenReturn(code);
        when(item.getPrice()).thenReturn(price);

        return billItem;
    }

    // Helper method to create mock Bill
    private Bill createMockBill(int billNumber) {
        Bill bill = mock(Bill.class);
        when(bill.getBillNumber()).thenReturn(new BillNumber(billNumber));
        when(bill.getBillDate()).thenReturn(LocalDateTime.of(2024, 6, 5, 14, 30));
        return bill;
    }
}