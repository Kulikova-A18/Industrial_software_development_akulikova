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
            category.getOperations().clear();
            category.getOperations().addAll(operationRepository.findByCategoryId(id));
        }
        return Optional.ofNullable(category);
    }

    @Override
    public List<Category> findAll() {
        List<Category> allCategories = new ArrayList<>(categories.values());
        for (Category category : allCategories) {
            category.getOperations().clear();
            category.getOperations().addAll(operationRepository.findByCategoryId(category.getId()));
        }
        return allCategories;
    }

    @Override
    public boolean delete(UUID id) {
        List<Operation> categoryOperations = operationRepository.findByCategoryId(id);
        if (!categoryOperations.isEmpty()) {
            return false;
        }
        return categories.remove(id) != null;
    }

    public List<Category> findByType(OperationType type) {
        return findAll().stream()
                .filter(c -> c.getType() == type)
                .collect(Collectors.toList());
    }
}