package com.tigerbank.repository;

import com.tigerbank.domain.Category;
import com.tigerbank.enums.OperationType;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Optional;
import java.util.UUID;

public class CategoryRepositoryTest {

    private CategoryRepository repository;
    private OperationRepository operationRepository;

    @Before
    public void setUp() {
        operationRepository = new OperationRepository(null, null);
        repository = new CategoryRepository(operationRepository);
    }

    @Test
    public void testSaveAndFindById() {
        Category category = new Category(OperationType.INCOME, "Salary");

        Category saved = repository.save(category);
        assertEquals(category, saved);

        Optional<Category> found = repository.findById(category.getId());
        assertTrue(found.isPresent());
        assertEquals(category.getId(), found.get().getId());
        assertEquals(category.getType(), found.get().getType());
        assertEquals(category.getName(), found.get().getName());
    }

    @Test
    public void testFindByType() {
        Category incomeCat1 = new Category(OperationType.INCOME, "Salary");
        Category incomeCat2 = new Category(OperationType.INCOME, "Bonus");
        Category expenseCat = new Category(OperationType.EXPENSE, "Food");

        repository.save(incomeCat1);
        repository.save(incomeCat2);
        repository.save(expenseCat);

        assertEquals(2, repository.findByType(OperationType.INCOME).size());
        assertEquals(1, repository.findByType(OperationType.EXPENSE).size());
    }

    @Test
    public void testDelete() {
        Category category = new Category(OperationType.INCOME, "Salary");
        repository.save(category);

        assertTrue(repository.delete(category.getId()));
        assertFalse(repository.findById(category.getId()).isPresent());
    }
}