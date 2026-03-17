package com.tigerbank.command;

public interface Command {
    void execute();

    void undo();

    String getName();

    long getExecutionTime();
}