package com.tigerbank.domain;

import com.tigerbank.enums.OperationType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Category {
    private UUID id;
    private OperationType type;
    private String name;
    private List<Operation> operations = new ArrayList<>();

    public Category() {
        this.id = UUID.randomUUID();
    }

    public Category(OperationType type, String name) {
        this();
        this.type = type;
        this.name = name;
    }

    public Category(UUID id, OperationType type, String name) {
        this.id = id;
        this.type = type;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        return String.format("%s (%s)", name, type.getDescription());
    }

    public String toCsv() {
        return String.join(",",
                id.toString(),
                type.name(),
                name.replace(",", " "));
    }

    public String toJson() {
        return String.format(
                "{\"id\":\"%s\",\"type\":\"%s\",\"name\":\"%s\"}",
                id.toString(),
                type.name(),
                name.replace("\"", "\\\""));
    }
}