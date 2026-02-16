package com.tigerbank.enums;

public enum OperationType {
    INCOME("Доход"),
    EXPENSE("Расход");

    private final String description;

    OperationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}