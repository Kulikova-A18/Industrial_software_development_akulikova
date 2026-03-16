package com.tigerbank.command;

import com.tigerbank.domain.BankAccount;
import com.tigerbank.service.AccountService;

public class CreateAccountCommand extends AbstractCommand {
    private final AccountService accountService;
    private final String accountName;
    private BankAccount createdAccount;

    public CreateAccountCommand(AccountService accountService, String accountName) {
        this.accountService = accountService;
        this.accountName = accountName;
    }

    @Override
    public void execute() {
        this.createdAccount = accountService.createAccount(accountName);
    }

    @Override
    public void undo() {
        if (createdAccount != null) {
            accountService.deleteAccount(createdAccount.getId());
        }
    }

    @Override
    public String getName() {
        return String.format("Создание счета: %s", accountName);
    }
}