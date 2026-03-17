package com.tigerbank.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BankAccount {
    private UUID id;
    private String name;
    private BigDecimal balance;
    private List<Operation> operations = new ArrayList<>();

    public BankAccount() {
        this.id = UUID.randomUUID();
        this.balance = BigDecimal.ZERO;
    }

    public BankAccount(String name) {
        this();
        this.name = name;
    }

    public BankAccount(UUID id, String name, BigDecimal balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
    }

    // ГЕТТЕРЫ И СЕТТЕРЫ
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public List<Operation> getOperations() {
        return operations;
    }

    public void addOperation(Operation operation) {
        operations.add(operation);
    }

    public void removeOperation(Operation operation) {
        operations.remove(operation);
    }

    @Override
    public String toString() {
        return String.format("%s (Баланс: %.2f)", name, balance);
    }

    public String toCsv() {
        return String.join(",",
                id.toString(),
                name.replace(",", " "),
                balance.toString());
    }

    public String toJson() {
        return String.format(
                "{\"id\":\"%s\",\"name\":\"%s\",\"balance\":%s}",
                id.toString(),
                name.replace("\"", "\\\""),
                balance);
    }
}