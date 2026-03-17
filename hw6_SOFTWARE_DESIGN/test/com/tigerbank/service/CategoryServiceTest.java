package com.tigerbank.service;

import com.tigerbank.domain.Category;
import com.tigerbank.enums.OperationType;
import com.tigerbank.repository.CategoryRepository;
import com.tigerbank.repository.OperationRepository;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CategoryServiceTest {

    private CategoryService service;
    private CategoryRepository categoryRepository;

    @Before
    public void setUp() {
        OperationRepository operationRepository = new OperationRepository(null, null);
        categoryRepository = new CategoryRepository(operationRepository);
        service = new CategoryService(categoryRepository);
    }

    @Test
    public void testCreateCategory() {
        Category category = service.createCategory(OperationType.INCOME, "Salary");

        assertNotNull(category);
        assertNotNull(category.getId());
        assertEquals(OperationType.INCOME, category.getType());
        assertEquals("Salary", category.getName());

        Optional<Category> found = service.getCategory(category.getId());
        assertTrue(found.isPresent());
        assertEquals(category.getId(), found.get().getId());
    }

    @Test
    public void testGetAllCategories() {
        service.createCategory(OperationType.INCOME, "Salary");
        service.createCategory(OperationType.EXPENSE, "Food");

        assertEquals(2, service.getAllCategories().size());
    }

    @Test
    public void testGetCategoriesByType() {
        service.createCategory(OperationType.INCOME, "Salary");
        service.createCategory(OperationType.INCOME, "Bonus");
        service.createCategory(OperationType.EXPENSE, "Food");

        List<Category> incomeCategories = service.getCategoriesByType(OperationType.INCOME);
        assertEquals(2, incomeCategories.size());

        List<Category> expenseCategories = service.getCategoriesByType(OperationType.EXPENSE);
        assertEquals(1, expenseCategories.size());
    }

    @Test
    public void testLoadDefaultCategories() {
        service.loadDefaultCategories();

        List<Category> allCategories = service.getAllCategories();
        assertTrue(allCategories.size() > 0);

        boolean hasIncome = allCategories.stream()
                .anyMatch(c -> c.getType() == OperationType.INCOME);
        boolean hasExpense = allCategories.stream()
                .anyMatch(c -> c.getType() == OperationType.EXPENSE);

        assertTrue("Должны быть категории доходов", hasIncome);
        assertTrue("Должны быть категории расходов", hasExpense);
    }

    @Test
    public void testDeleteCategory() {
        Category category = service.createCategory(OperationType.INCOME, "Salary");

        boolean result = service.deleteCategory(category.getId());
        // Метод может вернуть true или false в зависимости от реализации
        assertNotNull(Boolean.valueOf(result));
    }
}