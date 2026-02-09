package com.tigerbank.domain;

import com.tigerbank.enums.OperationType;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.UUID;

public class CategoryTest {

    @Test
    public void testCreateCategory() {
        Category category = new Category(OperationType.INCOME, "Salary");
        assertNotNull(category.getId());
        assertEquals(OperationType.INCOME, category.getType());
        assertEquals("Salary", category.getName());
    }

    @Test
    public void testCategoryWithId() {
        UUID id = UUID.randomUUID();
        Category category = new Category(id, OperationType.EXPENSE, "Food");

        assertEquals(id, category.getId());
        assertEquals(OperationType.EXPENSE, category.getType());
        assertEquals("Food", category.getName());
    }

    @Test
    public void testToString() {
        Category category = new Category(OperationType.INCOME, "Salary");
        assertEquals("Salary (Доход)", category.toString());

        Category expenseCategory = new Category(OperationType.EXPENSE, "Food");
        assertEquals("Food (Расход)", expenseCategory.toString());
    }

    @Test
    public void testToCsv() {
        Category category = new Category(OperationType.INCOME, "Salary,Bonus");
        String csv = category.toCsv();
        assertTrue(csv.contains("Salary Bonus")); // Запятая должна быть заменена
        assertTrue(csv.contains("INCOME"));
    }

    @Test
    public void testToJson() {
        Category category = new Category(OperationType.EXPENSE, "Food\"Groceries");
        String json = category.toJson();
        assertTrue(json.contains("Food\\\"Groceries")); // Кавычки должны быть экранированы
        assertTrue(json.contains("EXPENSE"));
    }
}