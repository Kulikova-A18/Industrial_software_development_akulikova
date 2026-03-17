package com.tigerbank.command;

import com.tigerbank.domain.BankAccount;
import com.tigerbank.domain.Operation;
import com.tigerbank.service.AccountService;
import com.tigerbank.service.OperationService;
import java.util.List;
import java.util.UUID;

public class DeleteAccountCommand extends AbstractCommand {
    private final AccountService accountService;
    private final OperationService operationService;
    private final UUID accountId;
    private BankAccount deletedAccount;
    private List<Operation> deletedOperations;

    public DeleteAccountCommand(AccountService accountService,
            OperationService operationService,
            UUID accountId) {
        this.accountService = accountService;
        this.operationService = operationService;
        this.accountId = accountId;
    }

    @Override
    public void execute() {
        deletedAccount = accountService.getAccount(accountId).orElse(null);
        if (deletedAccount != null) {
            deletedOperations = operationService.getOperationsByAccount(accountId);
            for (Operation op : deletedOperations) {
                operationService.deleteOperation(op.getId());
            }
            accountService.deleteAccount(accountId);
        }
    }

    @Override
    public void undo() {
        if (deletedAccount != null) {
            accountService.createAccount(deletedAccount.getName());
        }
    }

    @Override
    public String getName() {
        return String.format("Удаление счета: %s",
                deletedAccount != null ? deletedAccount.getName() : "Unknown");
    }
}