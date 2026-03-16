package com.tigerbank.repository;

import com.tigerbank.domain.Category;
import com.tigerbank.domain.Operation;
import com.tigerbank.enums.OperationType;
import java.util.*;
import java.util.stream.Collectors;

public class CategoryRepository implements Repository<Category> {
    private final Map<UUID, Category> categories = new HashMap<>();
    private final OperationRepository operationRepository;

    public CategoryRepository(OperationRepository operationRepository) {
        this.operationRepository = operationRepository;
    }

    @Override
    public Category save(Category category) {
        categories.put(category.getId(), category);
        return category;
    }

    @Override
    public Optional<Category> findById(UUID id) {
        Category category = categories.get(id);
        if (category != null) {
            List<Operation> categoryOperations = operationRepository.findByCategoryId(id);
            category.getOperations().clear();
            category.getOperations().addAll(categoryOperations);
        }
        return Optional.ofNullable(category);
    }

    @Override
    public List<Category> findAll() {
        List<Category> allCategories = new ArrayList<>(categories.values());
        for (Category category : allCategories) {
            List<Operation> categoryOperations = operationRepository.findByCategoryId(category.getId());
            category.getOperations().clear();
            category.getOperations().addAll(categoryOperations);
        }
        return allCategories;
    }

    @Override
    public boolean delete(UUID id) {
        List<Operation> categoryOperations = operationRepository.findByCategoryId(id);
        for (Operation op : categoryOperations) {
            op.setCategoryId(null);
            op.setCategory(null);
            operationRepository.save(op);
        }
        return categories.remove(id) != null;
    }

    public List<Category> findByType(OperationType type) {
        return findAll().stream()
                .filter(c -> c.getType() == type)
                .collect(Collectors.toList());
    }

    @Override
    public void clear() {
        categories.clear();
    }
}