// CommandFactoryTest.java - 10 tests following all testing standards
package com.syos.infrastructure.factories;

import com.syos.application.interfaces.Command;
import com.syos.application.commands.*;
import com.syos.application.commands.sales.*;
import com.syos.application.commands.inventory.*;
import com.syos.application.commands.reports.*;
import com.syos.application.commands.user.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Command Factory Tests")
class CommandFactoryTest {

    private CommandFactory commandFactory;

    @BeforeEach
    void setUp() {
        commandFactory = CommandFactory.getInstance();
    }

    @Test
    @DisplayName("Should return same instance when getInstance called multiple times")
    void should_return_same_instance_when_get_instance_called_multiple_times() {
        // Arrange & Act
        CommandFactory instance1 = CommandFactory.getInstance();
        CommandFactory instance2 = CommandFactory.getInstance();
        CommandFactory instance3 = CommandFactory.getInstance();

        // Assert
        assertSame(instance1, instance2);
        assertSame(instance2, instance3);
        assertSame(instance1, instance3);
    }

    @Test
    @DisplayName("Should create CreateSaleCommand when CREATE_SALE command type provided")
    void should_create_create_sale_command_when_create_sale_command_type_provided() {
        // Arrange & Act
        Command command = commandFactory.createCommand("CREATE_SALE");

        // Assert
        assertNotNull(command);
        assertTrue(command instanceof CreateSaleCommand);
    }

    @Test
    @DisplayName("Should create AddStockCommand when ADD_STOCK command type provided")
    void should_create_add_stock_command_when_add_stock_command_type_provided() {
        // Arrange & Act
        Command command = commandFactory.createCommand("ADD_STOCK");

        // Assert
        assertNotNull(command);
        assertTrue(command instanceof AddStockCommand);
    }

    @Test
    @DisplayName("Should create ViewBillsCommand when VIEW_BILLS command type provided")
    void should_create_view_bills_command_when_view_bills_command_type_provided() {
        // Arrange & Act
        Command command = commandFactory.createCommand("VIEW_BILLS");

        // Assert
        assertNotNull(command);
        assertTrue(command instanceof ViewBillsCommand);
    }

    @Test
    @DisplayName("Should create ViewItemsCommand when VIEW_ITEMS command type provided")
    void should_create_view_items_command_when_view_items_command_type_provided() {
        // Arrange & Act
        Command command = commandFactory.createCommand("VIEW_ITEMS");

        // Assert
        assertNotNull(command);
        assertTrue(command instanceof ViewItemsCommand);
    }

    @Test
    @DisplayName("Should create MoveToShelfCommand when MOVE_TO_SHELF command type provided")
    void should_create_move_to_shelf_command_when_move_to_shelf_command_type_provided() {
        // Arrange & Act
        Command command = commandFactory.createCommand("MOVE_TO_SHELF");

        // Assert
        assertNotNull(command);
        assertTrue(command instanceof MoveToShelfCommand);
    }

    @Test
    @DisplayName("Should create RegisterUserCommand when REGISTER_USER command type provided")
    void should_create_register_user_command_when_register_user_command_type_provided() {
        // Arrange & Act
        Command command = commandFactory.createCommand("REGISTER_USER");

        // Assert
        assertNotNull(command);
        assertTrue(command instanceof RegisterUserCommand);
    }

    @Test
    @DisplayName("Should create ExitCommand when EXIT command type provided")
    void should_create_exit_command_when_exit_command_type_provided() {
        // Arrange & Act
        Command command = commandFactory.createCommand("EXIT");

        // Assert
        assertNotNull(command);
        assertTrue(command instanceof ExitCommand);
    }

    @Test
    @DisplayName("Should create NullCommand when invalid command type provided")
    void should_create_null_command_when_invalid_command_type_provided() {
        // Arrange & Act
        Command command = commandFactory.createCommand("INVALID_COMMAND");

        // Assert
        assertNotNull(command);
        assertTrue(command instanceof NullCommand);
    }

    @ParameterizedTest
    @ValueSource(strings = {"create_sale", "CREATE_SALE", "Create_Sale", "ADD_STOCK", "add_stock"})
    @DisplayName("Should handle case insensitive command types correctly")
    void should_handle_case_insensitive_command_types_correctly(String commandType) {
        // Arrange & Act
        Command command = commandFactory.createCommand(commandType);

        // Assert
        assertNotNull(command);

        // Verify correct command type is created regardless of case
        String upperCaseType = commandType.toUpperCase();
        if (upperCaseType.equals("CREATE_SALE")) {
            assertTrue(command instanceof CreateSaleCommand);
        } else if (upperCaseType.equals("ADD_STOCK")) {
            assertTrue(command instanceof AddStockCommand);
        }
    }

    @Test
    @DisplayName("Should create different instances for each factory method call")
    void should_create_different_instances_for_each_factory_method_call() {
        // Arrange & Act
        Command command1 = commandFactory.createCommand("CREATE_SALE");
        Command command2 = commandFactory.createCommand("CREATE_SALE");
        Command command3 = commandFactory.createCommand("ADD_STOCK");

        // Assert
        assertNotNull(command1);
        assertNotNull(command2);
        assertNotNull(command3);

        // Each call should create a new instance
        assertNotSame(command1, command2);
        assertNotSame(command1, command3);
        assertNotSame(command2, command3);

        // But they should be of the correct types
        assertTrue(command1 instanceof CreateSaleCommand);
        assertTrue(command2 instanceof CreateSaleCommand);
        assertTrue(command3 instanceof AddStockCommand);
    }


    @Test
    @DisplayName("Should handle empty string command type gracefully")
    void should_handle_empty_string_command_type_gracefully() {
        // Arrange & Act
        Command command = commandFactory.createCommand("");

        // Assert
        assertNotNull(command);
        assertTrue(command instanceof NullCommand);
    }
}