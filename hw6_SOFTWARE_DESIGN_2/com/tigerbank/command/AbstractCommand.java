package com.tigerbank.command;

public abstract class AbstractCommand implements Command {
    protected long executionTime;

    @Override
    public long getExecutionTime() {
        return executionTime;
    }
}