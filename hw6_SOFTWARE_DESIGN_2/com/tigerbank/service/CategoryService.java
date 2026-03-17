package com.tigerbank.service;

import com.tigerbank.domain.Category;
import com.tigerbank.domain.factory.DomainFactory;
import com.tigerbank.domain.factory.DomainFactoryImpl;
import com.tigerbank.enums.OperationType;
import com.tigerbank.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final DomainFactory domainFactory;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
        this.domainFactory = new DomainFactoryImpl();
    }

    public Category createCategory(OperationType type, String name) {
        Category category = domainFactory.createCategory(type, name);
        return categoryRepository.save(category);
    }

    public Category createCategory(UUID id, OperationType type, String name) {
        Category category = domainFactory.createCategory(id, type, name);
        return categoryRepository.save(category);
    }

    public Optional<Category> getCategory(UUID id) {
        return categoryRepository.findById(id);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Category> getCategoriesByType(OperationType type) {
        return categoryRepository.findByType(type);
    }

    public boolean deleteCategory(UUID id) {
        return categoryRepository.delete(id);
    }

    public void loadDefaultCategories() {
        String[] incomeCategories = {
                "Зарплата", "Фриланс", "Инвестиции", "Подарки", "Возврат долга",
                "Кэшбэк", "Проценты по вкладу", "Стипендия", "Пенсия", "Сдача в аренду",
                "Дивиденды", "Бонусы", "Продажа", "Выигрыш", "Наследство"
        };

        String[] expenseCategories = {
                "Продукты", "Транспорт", "Коммунальные услуги", "Развлечения", "Одежда",
                "Здоровье", "Образование", "Рестораны/Кафе", "Подарки", "Связь/Интернет",
                "Кредиты/Ипотека", "Страхование", "Дом/Ремонт", "Красота/Уход", "Досуг/Хобби",
                "Питомцы", "Дети", "Автомобиль", "Налоги", "Благотворительность",
                "Спорт", "Путешествия", "Книги", "Техника", "Подписки"
        };

        for (String name : incomeCategories) {
            if (getAllCategories().stream()
                    .noneMatch(c -> c.getName().equalsIgnoreCase(name)
                            && c.getType() == OperationType.INCOME)) {
                createCategory(OperationType.INCOME, name);
            }
        }

        for (String name : expenseCategories) {
            if (getAllCategories().stream()
                    .noneMatch(c -> c.getName().equalsIgnoreCase(name)
                            && c.getType() == OperationType.EXPENSE)) {
                createCategory(OperationType.EXPENSE, name);
            }
        }
    }
}