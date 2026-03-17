package com.tigerbank.domain;

import com.tigerbank.enums.OperationType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Operation {
    private UUID id;
    private OperationType type;
    private UUID bankAccountId;
    private BankAccount bankAccount;
    private BigDecimal amount;
    private LocalDateTime date;
    private String description;
    private UUID categoryId;
    private Category category;

    public Operation() {
        this.id = UUID.randomUUID();
        this.date = LocalDateTime.now();
    }

    public Operation(OperationType type, UUID bankAccountId,
            BigDecimal amount, UUID categoryId) {
        this();
        this.type = type;
        this.bankAccountId = bankAccountId;
        this.amount = amount;
        this.categoryId = categoryId;
    }

    public Operation(UUID id, OperationType type, UUID bankAccountId,
            BigDecimal amount, LocalDateTime date, String description, UUID categoryId) {
        this.id = id;
        this.type = type;
        this.bankAccountId = bankAccountId;
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.categoryId = categoryId;
    }

    // ГЕТТЕРЫ И СЕТТЕРЫ
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public OperationType getType() {
        return type;
    }

    public void setType(OperationType type) {
        this.type = type;
    }

    public UUID getBankAccountId() {
        return bankAccountId;
    }

    public void setBankAccountId(UUID bankAccountId) {
        this.bankAccountId = bankAccountId;
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
        this.bankAccountId = bankAccount != null ? bankAccount.getId() : null;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
        this.categoryId = category != null ? category.getId() : null;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return String.format("%s: %.2f - %s",
                type.getDescription(), amount,
                description != null ? description : "без описания");
    }

    public String toCsv() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.join(",",
                id.toString(),
                type.name(),
                bankAccountId != null ? bankAccountId.toString() : "",
                amount.toString(),
                date.format(formatter),
                description != null ? description.replace(",", " ") : "",
                categoryId != null ? categoryId.toString() : "");
    }

    public String toJson() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format(
                "{\"id\":\"%s\",\"type\":\"%s\",\"bankAccountId\":\"%s\",\"amount\":%s,\"date\":\"%s\",\"description\":\"%s\",\"categoryId\":\"%s\"}",
                id.toString(),
                type.name(),
                bankAccountId != null ? bankAccountId.toString() : "",
                amount,
                date.format(formatter),
                description != null ? description.replace("\"", "\\\"") : "",
                categoryId != null ? categoryId.toString() : "");
    }
}