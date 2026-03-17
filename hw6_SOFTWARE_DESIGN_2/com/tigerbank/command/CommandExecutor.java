package com.tigerbank.command;

import com.tigerbank.di.Singleton;
import java.util.Stack;

@Singleton
public class CommandExecutor {
    private final Stack<Command> history = new Stack<>();
    private final Stack<Command> redoStack = new Stack<>();

    public void executeCommand(Command command) {
        long startTime = System.nanoTime();
        command.execute();
        long endTime = System.nanoTime();

        try {
            java.lang.reflect.Field field = command.getClass().getSuperclass()
                    .getDeclaredField("executionTime");
            field.setAccessible(true);
            field.set(command, endTime - startTime);
        } catch (Exception e) {
        }

        history.push(command);
        redoStack.clear();
    }

    public void undo() {
        if (!history.isEmpty()) {
            Command command = history.pop();
            command.undo();
            redoStack.push(command);
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            Command command = redoStack.pop();
            executeCommand(command);
        }
    }

    public Stack<Command> getHistory() {
        return history;
    }
}