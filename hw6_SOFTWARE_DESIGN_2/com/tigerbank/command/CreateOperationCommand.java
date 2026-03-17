package com.tigerbank.command;

import com.tigerbank.domain.Operation;
import com.tigerbank.enums.OperationType;
import com.tigerbank.service.OperationService;
import java.math.BigDecimal;
import java.util.UUID;

public class CreateOperationCommand extends AbstractCommand {
    private final OperationService operationService;
    private final OperationType type;
    private final UUID accountId;
    private final BigDecimal amount;
    private final UUID categoryId;
    private final String description;
    private Operation createdOperation;

    public CreateOperationCommand(OperationService operationService,
            OperationType type, UUID accountId,
            BigDecimal amount, UUID categoryId,
            String description) {
        this.operationService = operationService;
        this.type = type;
        this.accountId = accountId;
        this.amount = amount;
        this.categoryId = categoryId;
        this.description = description;
    }

    @Override
    public void execute() {
        this.createdOperation = operationService.createOperation(
                type, accountId, amount, categoryId, description);
    }

    @Override
    public void undo() {
        if (createdOperation != null) {
            operationService.deleteOperation(createdOperation.getId());
        }
    }

    @Override
    public String getName() {
        return String.format("Создание операции %s: %.2f",
                type.getDescription(), amount);
    }
}