import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.function.Function;

// ========== ENUMS ==========
enum OperationType {
    INCOME("Доход"),
    EXPENSE("Расход");

    private final String description;

    OperationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}

// ========== DOMAIN CLASSES ==========
class BankAccount {
    private UUID id;
    private String name;
    private BigDecimal balance;
    private List<Operation> operations = new ArrayList<>();

    public BankAccount() {
        this.id = UUID.randomUUID();
        this.balance = BigDecimal.ZERO;
    }

    public BankAccount(String name) {
        this();
        this.name = name;
    }

    public BankAccount(UUID id, String name, BigDecimal balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
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
        return String.format("%s (Баланс: %.2f)", name, balance);
    }

    public String toCsv() {
        return String.join(",",
                id.toString(),
                name.replace(",", " "),
                balance.toString());
    }

    public String toJson() {
        return String.format(
                "{\"id\":\"%s\",\"name\":\"%s\",\"balance\":%s}",
                id.toString(),
                name.replace("\"", "\\\""),
                balance);
    }
}

class Category {
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

class Operation {
    private UUID id;
    private OperationType type;
    private UUID bankAccountId;
    private BankAccount bankAccount;
    private BigDecimal amount;
    private LocalDateTime date;
    private String description;
    private UUID categoryId;
    private Category category;

    public Operation() {
        this.id = UUID.randomUUID();
        this.date = LocalDateTime.now();
    }

    public Operation(OperationType type, UUID bankAccountId,
            BigDecimal amount, UUID categoryId) {
        this();
        this.type = type;
        this.bankAccountId = bankAccountId;
        this.amount = amount;
        this.categoryId = categoryId;
    }

    public Operation(UUID id, OperationType type, UUID bankAccountId,
            BigDecimal amount, LocalDateTime date, String description, UUID categoryId) {
        this.id = id;
        this.type = type;
        this.bankAccountId = bankAccountId;
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.categoryId = categoryId;
    }

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

    public UUID getBankAccountId() {
        return bankAccountId;
    }

    public void setBankAccountId(UUID bankAccountId) {
        this.bankAccountId = bankAccountId;
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
        this.bankAccountId = bankAccount != null ? bankAccount.getId() : null;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
        this.categoryId = category != null ? category.getId() : null;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return String.format("%s: %.2f - %s",
                type.getDescription(), amount,
                description != null ? description : "без описания");
    }

    public String toCsv() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.join(",",
                id.toString(),
                type.name(),
                bankAccountId != null ? bankAccountId.toString() : "",
                amount.toString(),
                date.format(formatter),
                description != null ? description.replace(",", " ") : "",
                categoryId != null ? categoryId.toString() : "");
    }

    public String toJson() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format(
                "{\"id\":\"%s\",\"type\":\"%s\",\"bankAccountId\":\"%s\",\"amount\":%s,\"date\":\"%s\",\"description\":\"%s\",\"categoryId\":\"%s\"}",
                id.toString(),
                type.name(),
                bankAccountId != null ? bankAccountId.toString() : "",
                amount,
                date.format(formatter),
                description != null ? description.replace("\"", "\\\"") : "",
                categoryId != null ? categoryId.toString() : "");
    }
}

// ========== REPOSITORIES WITH FULL RELATIONS ==========
interface Repository<T> {
    T save(T entity);

    Optional<T> findById(UUID id);

    List<T> findAll();

    boolean delete(UUID id);
}

class BankAccountRepository implements Repository<BankAccount> {
    private final Map<UUID, BankAccount> accounts = new HashMap<>();
    private final OperationRepository operationRepository;

    public BankAccountRepository(OperationRepository operationRepository) {
        this.operationRepository = operationRepository;
    }

    @Override
    public BankAccount save(BankAccount account) {
        accounts.put(account.getId(), account);
        return account;
    }

    @Override
    public Optional<BankAccount> findById(UUID id) {
        BankAccount account = accounts.get(id);
        if (account != null) {
            // Загружаем связанные операции
            account.getOperations().clear();
            account.getOperations().addAll(operationRepository.findByBankAccountId(id));
        }
        return Optional.ofNullable(account);
    }

    @Override
    public List<BankAccount> findAll() {
        List<BankAccount> allAccounts = new ArrayList<>(accounts.values());
        // Загружаем операции для всех счетов
        for (BankAccount account : allAccounts) {
            account.getOperations().clear();
            account.getOperations().addAll(operationRepository.findByBankAccountId(account.getId()));
        }
        return allAccounts;
    }

    @Override
    public boolean delete(UUID id) {
        // Проверяем, есть ли операции у счета
        List<Operation> accountOperations = operationRepository.findByBankAccountId(id);
        if (!accountOperations.isEmpty()) {
            return false; // Нельзя удалить счет с операциями
        }
        return accounts.remove(id) != null;
    }
}

class CategoryRepository implements Repository<Category> {
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
            // Загружаем связанные операции
            category.getOperations().clear();
            category.getOperations().addAll(operationRepository.findByCategoryId(id));
        }
        return Optional.ofNullable(category);
    }

    @Override
    public List<Category> findAll() {
        List<Category> allCategories = new ArrayList<>(categories.values());
        // Загружаем операции для всех категорий
        for (Category category : allCategories) {
            category.getOperations().clear();
            category.getOperations().addAll(operationRepository.findByCategoryId(category.getId()));
        }
        return allCategories;
    }

    @Override
    public boolean delete(UUID id) {
        // Проверяем, есть ли операции у категории
        List<Operation> categoryOperations = operationRepository.findByCategoryId(id);
        if (!categoryOperations.isEmpty()) {
            return false; // Нельзя удалить категорию с операциями
        }
        return categories.remove(id) != null;
    }

    public List<Category> findByType(OperationType type) {
        return findAll().stream()
                .filter(c -> c.getType() == type)
                .collect(Collectors.toList());
    }
}

class OperationRepository implements Repository<Operation> {
    private final Map<UUID, Operation> operations = new HashMap<>();
    private final BankAccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    public OperationRepository(BankAccountRepository accountRepository,
            CategoryRepository categoryRepository) {
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Operation save(Operation operation) {
        // Устанавливаем связи
        if (operation.getBankAccountId() != null) {
            accountRepository.findById(operation.getBankAccountId()).ifPresent(operation::setBankAccount);
        }
        if (operation.getCategoryId() != null) {
            categoryRepository.findById(operation.getCategoryId()).ifPresent(operation::setCategory);
        }

        operations.put(operation.getId(), operation);
        return operation;
    }

    @Override
    public Optional<Operation> findById(UUID id) {
        return Optional.ofNullable(operations.get(id));
    }

    @Override
    public List<Operation> findAll() {
        return new ArrayList<>(operations.values());
    }

    @Override
    public boolean delete(UUID id) {
        return operations.remove(id) != null;
    }

    public List<Operation> findByBankAccountId(UUID accountId) {
        return operations.values().stream()
                .filter(op -> accountId.equals(op.getBankAccountId()))
                .collect(Collectors.toList());
    }

    public List<Operation> findByCategoryId(UUID categoryId) {
        return operations.values().stream()
                .filter(op -> categoryId.equals(op.getCategoryId()))
                .collect(Collectors.toList());
    }

    public List<Operation> findByDateBetween(LocalDateTime start, LocalDateTime end) {
        return operations.values().stream()
                .filter(op -> !op.getDate().isBefore(start) && !op.getDate().isAfter(end))
                .collect(Collectors.toList());
    }

    public List<Operation> findByType(OperationType type) {
        return operations.values().stream()
                .filter(op -> op.getType() == type)
                .collect(Collectors.toList());
    }
}

// ========== SERVICES ==========
class AccountService {
    private final BankAccountRepository accountRepository;
    private final OperationRepository operationRepository;

    public AccountService(BankAccountRepository accountRepository,
            OperationRepository operationRepository) {
        this.accountRepository = accountRepository;
        this.operationRepository = operationRepository;
    }

    public BankAccount createAccount(String name) {
        BankAccount account = new BankAccount(name);
        return accountRepository.save(account);
    }

    public Optional<BankAccount> getAccount(UUID id) {
        return accountRepository.findById(id);
    }

    public List<BankAccount> getAllAccounts() {
        return accountRepository.findAll();
    }

    public boolean deleteAccount(UUID id) {
        return accountRepository.delete(id);
    }

    public void updateAccountBalance(UUID accountId, BigDecimal amount, OperationType type) {
        Optional<BankAccount> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isPresent()) {
            BankAccount account = accountOpt.get();
            BigDecimal newBalance = type == OperationType.INCOME ? account.getBalance().add(amount)
                    : account.getBalance().subtract(amount);
            account.setBalance(newBalance);
            accountRepository.save(account);
        }
    }

    public BigDecimal calculateTotalBalance() {
        return getAllAccounts().stream()
                .map(BankAccount::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category createCategory(OperationType type, String name) {
        Category category = new Category(type, name);
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
        // Предустановленные категории доходов
        String[] incomeCategories = {
                "Зарплата", "Фриланс", "Инвестиции", "Подарки", "Возврат долга",
                "Кэшбэк", "Проценты по вкладу", "Стипендия", "Пенсия", "Сдача в аренду"
        };

        // Предустановленные категории расходов
        String[] expenseCategories = {
                "Продукты", "Транспорт", "Коммунальные услуги", "Развлечения", "Одежда",
                "Здоровье", "Образование", "Рестораны/Кафе", "Подарки", "Связь/Интернет",
                "Кредиты/Ипотека", "Страхование", "Дом/Ремонт", "Красота/Уход", "Досуг/Хобби",
                "Питомцы", "Дети", "Автомобиль", "Налоги", "Благотворительность"
        };

        // Добавляем только если они еще не существуют
        for (String name : incomeCategories) {
            if (getAllCategories().stream()
                    .noneMatch(c -> c.getName().equals(name) && c.getType() == OperationType.INCOME)) {
                createCategory(OperationType.INCOME, name);
            }
        }

        for (String name : expenseCategories) {
            if (getAllCategories().stream()
                    .noneMatch(c -> c.getName().equals(name) && c.getType() == OperationType.EXPENSE)) {
                createCategory(OperationType.EXPENSE, name);
            }
        }
    }
}

class OperationService {
    private final OperationRepository operationRepository;
    private final AccountService accountService;
    private final CategoryService categoryService;

    public OperationService(OperationRepository operationRepository,
            AccountService accountService,
            CategoryService categoryService) {
        this.operationRepository = operationRepository;
        this.accountService = accountService;
        this.categoryService = categoryService;
    }

    public Operation createOperation(OperationType type, UUID accountId,
            BigDecimal amount, UUID categoryId, String description) {
        if (accountService.getAccount(accountId).isEmpty()) {
            throw new IllegalArgumentException("Счет не найден");
        }
        if (categoryService.getCategory(categoryId).isEmpty()) {
            throw new IllegalArgumentException("Категория не найдена");
        }

        Operation operation = new Operation(type, accountId, amount, categoryId);
        operation.setDescription(description);

        // Обновляем баланс счета
        accountService.updateAccountBalance(accountId, amount, type);

        return operationRepository.save(operation);
    }

    public Optional<Operation> getOperation(UUID id) {
        return operationRepository.findById(id);
    }

    public List<Operation> getOperationsByAccount(UUID accountId) {
        return operationRepository.findByBankAccountId(accountId);
    }

    public List<Operation> getAllOperations() {
        return operationRepository.findAll();
    }

    public boolean deleteOperation(UUID id) {
        Optional<Operation> operationOpt = operationRepository.findById(id);
        if (operationOpt.isPresent()) {
            Operation operation = operationOpt.get();

            // Отменяем влияние операции на баланс
            OperationType reverseType = operation.getType() == OperationType.INCOME ? OperationType.EXPENSE
                    : OperationType.INCOME;
            accountService.updateAccountBalance(
                    operation.getBankAccountId(),
                    operation.getAmount(),
                    reverseType);

            return operationRepository.delete(id);
        }
        return false;
    }

    public List<Operation> getOperationsByCategory(UUID categoryId) {
        return operationRepository.findByCategoryId(categoryId);
    }

    public List<Operation> getOperationsByType(OperationType type) {
        return operationRepository.findByType(type);
    }

    public List<Operation> getOperationsByDateRange(LocalDateTime start, LocalDateTime end) {
        return operationRepository.findByDateBetween(start, end);
    }

    public BigDecimal getTotalIncome(LocalDateTime start, LocalDateTime end) {
        return getOperationsByDateRange(start, end).stream()
                .filter(op -> op.getType() == OperationType.INCOME)
                .map(Operation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalExpense(LocalDateTime start, LocalDateTime end) {
        return getOperationsByDateRange(start, end).stream()
                .filter(op -> op.getType() == OperationType.EXPENSE)
                .map(Operation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getBalanceForPeriod(LocalDateTime start, LocalDateTime end) {
        return getTotalIncome(start, end).subtract(getTotalExpense(start, end));
    }
}

class AnalyticsService {
    private final OperationService operationService;
    private final CategoryService categoryService;
    private final AccountService accountService;

    public AnalyticsService(OperationService operationService,
            CategoryService categoryService,
            AccountService accountService) {
        this.operationService = operationService;
        this.categoryService = categoryService;
        this.accountService = accountService;
    }

    public Map<String, Object> getFullAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> analytics = new LinkedHashMap<>();

        // Базовая статистика
        analytics.put("period", startDate.toLocalDate() + " - " + endDate.toLocalDate());
        analytics.put("total_income", operationService.getTotalIncome(startDate, endDate));
        analytics.put("total_expense", operationService.getTotalExpense(startDate, endDate));
        analytics.put("balance", operationService.getBalanceForPeriod(startDate, endDate));

        // Статистика по категориям доходов
        Map<String, BigDecimal> incomeByCategory = new HashMap<>();
        for (Category category : categoryService.getCategoriesByType(OperationType.INCOME)) {
            BigDecimal total = operationService.getOperationsByCategory(category.getId()).stream()
                    .filter(op -> !op.getDate().isBefore(startDate) && !op.getDate().isAfter(endDate))
                    .map(Operation::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                incomeByCategory.put(category.getName(), total);
            }
        }
        analytics.put("income_by_category", incomeByCategory);

        // Статистика по категориям расходов
        Map<String, BigDecimal> expenseByCategory = new HashMap<>();
        for (Category category : categoryService.getCategoriesByType(OperationType.EXPENSE)) {
            BigDecimal total = operationService.getOperationsByCategory(category.getId()).stream()
                    .filter(op -> !op.getDate().isBefore(startDate) && !op.getDate().isAfter(endDate))
                    .map(Operation::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                expenseByCategory.put(category.getName(), total);
            }
        }
        analytics.put("expense_by_category", expenseByCategory);

        // Статистика по счетам
        Map<String, BigDecimal> accountBalances = new HashMap<>();
        for (BankAccount account : accountService.getAllAccounts()) {
            BigDecimal income = operationService.getOperationsByAccount(account.getId()).stream()
                    .filter(op -> op.getType() == OperationType.INCOME)
                    .filter(op -> !op.getDate().isBefore(startDate) && !op.getDate().isAfter(endDate))
                    .map(Operation::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal expense = operationService.getOperationsByAccount(account.getId()).stream()
                    .filter(op -> op.getType() == OperationType.EXPENSE)
                    .filter(op -> !op.getDate().isBefore(startDate) && !op.getDate().isAfter(endDate))
                    .map(Operation::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal balance = income.subtract(expense);
            if (balance.compareTo(BigDecimal.ZERO) != 0) {
                accountBalances.put(account.getName(), balance);
            }
        }
        analytics.put("account_balances", accountBalances);

        // Самые крупные операции
        List<Operation> allOperations = operationService.getAllOperations().stream()
                .filter(op -> !op.getDate().isBefore(startDate) && !op.getDate().isAfter(endDate))
                .sorted((o1, o2) -> o2.getAmount().compareTo(o1.getAmount()))
                .limit(10)
                .collect(Collectors.toList());
        analytics.put("top_operations", allOperations);

        return analytics;
    }

    public Map<String, BigDecimal> getCategoryStatistics(OperationType type) {
        Map<UUID, BigDecimal> categoryTotals = new HashMap<>();

        for (Operation op : operationService.getAllOperations()) {
            if (op.getType() == type) {
                categoryTotals.merge(op.getCategoryId(), op.getAmount(), BigDecimal::add);
            }
        }

        Map<String, BigDecimal> result = new HashMap<>();
        for (Map.Entry<UUID, BigDecimal> entry : categoryTotals.entrySet()) {
            Optional<Category> categoryOpt = categoryService.getCategory(entry.getKey());
            result.put(categoryOpt.map(Category::getName).orElse("Неизвестная"), entry.getValue());
        }

        return result;
    }

    public String generateAnalyticsReport(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> analytics = getFullAnalytics(startDate, endDate);

        StringBuilder report = new StringBuilder();
        report.append("=== ФИНАНСОВЫЙ ОТЧЕТ ===\n");
        report.append("Период: ").append(analytics.get("period")).append("\n\n");

        report.append("ОБЩАЯ СТАТИСТИКА:\n");
        report.append(String.format("Общий доход: %.2f\n", analytics.get("total_income")));
        report.append(String.format("Общий расход: %.2f\n", analytics.get("total_expense")));
        report.append(String.format("Баланс за период: %.2f\n\n", analytics.get("balance")));

        report.append("ДОХОДЫ ПО КАТЕГОРИЯМ:\n");
        Map<String, BigDecimal> incomeByCategory = (Map<String, BigDecimal>) analytics.get("income_by_category");
        if (incomeByCategory.isEmpty()) {
            report.append("  Нет данных\n");
        } else {
            incomeByCategory.forEach((cat, amt) -> report.append(String.format("  %s: %.2f\n", cat, amt)));
        }

        report.append("\nРАСХОДЫ ПО КАТЕГОРИЯМ:\n");
        Map<String, BigDecimal> expenseByCategory = (Map<String, BigDecimal>) analytics.get("expense_by_category");
        if (expenseByCategory.isEmpty()) {
            report.append("  Нет данных\n");
        } else {
            expenseByCategory.forEach((cat, amt) -> report.append(String.format("  %s: %.2f\n", cat, amt)));
        }

        report.append("\nБАЛАНС ПО СЧЕТАМ:\n");
        Map<String, BigDecimal> accountBalances = (Map<String, BigDecimal>) analytics.get("account_balances");
        if (accountBalances.isEmpty()) {
            report.append("  Нет данных\n");
        } else {
            accountBalances.forEach((acc, bal) -> report.append(String.format("  %s: %.2f\n", acc, bal)));
        }

        report.append("\nСАМЫЕ КРУПНЫЕ ОПЕРАЦИИ:\n");
        List<Operation> topOperations = (List<Operation>) analytics.get("top_operations");
        if (topOperations.isEmpty()) {
            report.append("  Нет данных\n");
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            for (Operation op : topOperations) {
                String categoryName = categoryService.getCategory(op.getCategoryId())
                        .map(Category::getName)
                        .orElse("Неизвестно");
                report.append(String.format("  %s %.2f (%s) - %s\n",
                        op.getType().getDescription(), op.getAmount(),
                        op.getDate().format(formatter), categoryName));
            }
        }

        return report.toString();
    }
}

class FileExporter {

    public void exportAllToCsv(String baseFileName,
            List<BankAccount> accounts,
            List<Category> categories,
            List<Operation> operations) {

        exportToCsv(accounts, baseFileName + "_accounts.csv",
                new String[] { "ID", "Name", "Balance" },
                acc -> new String[] { acc.getId().toString(), acc.getName(), acc.getBalance().toString() });

        exportToCsv(categories, baseFileName + "_categories.csv",
                new String[] { "ID", "Type", "Name" },
                cat -> new String[] { cat.getId().toString(), cat.getType().name(), cat.getName() });

        exportToCsv(operations, baseFileName + "_operations.csv",
                new String[] { "ID", "Type", "AccountID", "Amount", "Date", "Description", "CategoryID" },
                op -> {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    return new String[] {
                            op.getId().toString(),
                            op.getType().name(),
                            op.getBankAccountId() != null ? op.getBankAccountId().toString() : "",
                            op.getAmount().toString(),
                            op.getDate().format(formatter),
                            op.getDescription() != null ? op.getDescription() : "",
                            op.getCategoryId() != null ? op.getCategoryId().toString() : ""
                    };
                });
    }

    public void exportAllToJson(String baseFileName,
            List<BankAccount> accounts,
            List<Category> categories,
            List<Operation> operations) {

        exportToJson(accounts, baseFileName + "_accounts.json");
        exportToJson(categories, baseFileName + "_categories.json");
        exportToJson(operations, baseFileName + "_operations.json");
    }

    private <T> void exportToCsv(List<T> items, String fileName, String[] headers,
            Function<T, String[]> converter) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println(String.join(",", headers));
            for (T item : items) {
                writer.println(String.join(",", converter.apply(item)));
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка экспорта в CSV: " + e.getMessage(), e);
        }
    }

    private void exportToJson(List<?> items, String fileName) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println("[");
            for (int i = 0; i < items.size(); i++) {
                Object item = items.get(i);
                String json;
                if (item instanceof BankAccount) {
                    json = ((BankAccount) item).toJson();
                } else if (item instanceof Category) {
                    json = ((Category) item).toJson();
                } else if (item instanceof Operation) {
                    json = ((Operation) item).toJson();
                } else {
                    continue;
                }

                writer.print("  " + json);
                if (i < items.size() - 1) {
                    writer.println(",");
                } else {
                    writer.println();
                }
            }
            writer.println("]");
        } catch (IOException e) {
            throw new RuntimeException("Ошибка экспорта в JSON: " + e.getMessage(), e);
        }
    }

    public void importFromCsv(String baseFileName) {
        // Реализация импорта может быть добавлена позже
        System.out.println("Импорт из CSV файлов: " + baseFileName);
    }
}

// ========== GUI APPLICATION ==========
public class Main extends JFrame {
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final OperationService operationService;
    private final AnalyticsService analyticsService;
    private final FileExporter fileExporter;

    private JTabbedPane tabbedPane;
    private JTable accountsTable;
    private JTable categoriesTable;
    private JTable operationsTable;
    private DefaultTableModel accountsModel;
    private DefaultTableModel categoriesModel;
    private DefaultTableModel operationsModel;

    private JLabel totalBalanceLabel;
    private JLabel totalIncomeLabel;
    private JLabel totalExpenseLabel;
    private JTextArea analyticsArea;

    // Хранилище для связи ID с объектами
    private Map<UUID, BankAccount> accountMap = new HashMap<>();
    private Map<UUID, Category> categoryMap = new HashMap<>();
    private Map<UUID, Operation> operationMap = new HashMap<>();

    public Main() {
        // Инициализация репозиториев
        OperationRepository operationRepo = new OperationRepository(null, null);
        BankAccountRepository accountRepo = new BankAccountRepository(operationRepo);
        CategoryRepository categoryRepo = new CategoryRepository(operationRepo);

        // Устанавливаем связи
        operationRepo = new OperationRepository(accountRepo, categoryRepo);

        // Инициализация сервисов
        accountService = new AccountService(accountRepo, operationRepo);
        categoryService = new CategoryService(categoryRepo);
        operationService = new OperationService(operationRepo, accountService, categoryService);
        analyticsService = new AnalyticsService(operationService, categoryService, accountService);
        fileExporter = new FileExporter();

        initializeUI();
        loadInitialData();
        updateAnalytics();
    }

    private void initializeUI() {
        setTitle("ТигрБанк - Учет финансов");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        // Создаем вкладки
        tabbedPane = new JTabbedPane();

        // Вкладка счетов
        tabbedPane.addTab("Счета", createAccountsPanel());

        // Вкладка категорий
        tabbedPane.addTab("Категории", createCategoriesPanel());

        // Вкладка операций
        tabbedPane.addTab("Операции", createOperationsPanel());

        // Вкладка аналитики
        tabbedPane.addTab("Аналитика", createAnalyticsPanel());

        add(tabbedPane);

        // Меню
        createMenuBar();
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Меню "Файл"
        JMenu fileMenu = new JMenu("Файл");

        // Импорт
        JMenu importMenu = new JMenu("Импорт");
        JMenuItem importCsvItem = new JMenuItem("Импорт из CSV");
        importCsvItem.addActionListener(e -> importDataFromCsv());
        importMenu.add(importCsvItem);

        JMenuItem importJsonItem = new JMenuItem("Импорт из JSON");
        importJsonItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Функция импорта из JSON будет реализована в следующей версии",
                "Информация", JOptionPane.INFORMATION_MESSAGE));
        importMenu.add(importJsonItem);

        // Экспорт
        JMenu exportMenu = new JMenu("Экспорт");
        JMenuItem exportCsvItem = new JMenuItem("Экспорт в CSV");
        exportCsvItem.addActionListener(e -> exportDataToCsv());
        exportMenu.add(exportCsvItem);

        JMenuItem exportJsonItem = new JMenuItem("Экспорт в JSON");
        exportJsonItem.addActionListener(e -> exportDataToJson());
        exportMenu.add(exportJsonItem);

        JMenuItem exportReportItem = new JMenuItem("Экспорт отчета");
        exportReportItem.addActionListener(e -> exportAnalyticsReport());
        exportMenu.add(exportReportItem);

        fileMenu.add(importMenu);
        fileMenu.add(exportMenu);
        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Выход");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        // Меню "Настройки"
        JMenu settingsMenu = new JMenu("Настройки");

        JMenuItem manageCategoriesItem = new JMenuItem("Управление категориями");
        manageCategoriesItem.addActionListener(e -> showCategoryManagerDialog());
        settingsMenu.add(manageCategoriesItem);

        JMenuItem resetDataItem = new JMenuItem("Сбросить все данные");
        resetDataItem.addActionListener(e -> resetAllData());
        settingsMenu.add(resetDataItem);

        JMenuItem loadDefaultsItem = new JMenuItem("Загрузить стандартные категории");
        loadDefaultsItem.addActionListener(e -> loadDefaultCategories());
        settingsMenu.add(loadDefaultsItem);

        // Меню "Помощь"
        JMenu helpMenu = new JMenu("Помощь");
        JMenuItem aboutItem = new JMenuItem("О программе");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(settingsMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    private JPanel createAccountsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Таблица счетов
        String[] columns = { "ID", "Название", "Баланс", "Кол-во операций" };
        accountsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        accountsTable = new JTable(accountsModel);
        accountsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(accountsTable);

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton addButton = new JButton("Добавить счет");
        addButton.addActionListener(e -> showAddAccountDialog());

        JButton editButton = new JButton("Редактировать");
        editButton.addActionListener(e -> showEditAccountDialog());

        JButton deleteButton = new JButton("Удалить");
        deleteButton.addActionListener(e -> deleteSelectedAccount());

        JButton operationsButton = new JButton("Операции по счету");
        operationsButton.addActionListener(e -> showAccountOperationsDialog());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(operationsButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCategoriesPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Таблица категорий
        String[] columns = { "ID", "Тип", "Название", "Кол-во операций" };
        categoriesModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        categoriesTable = new JTable(categoriesModel);
        categoriesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(categoriesTable);

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton addButton = new JButton("Добавить категорию");
        addButton.addActionListener(e -> showAddCategoryDialog());

        JButton deleteButton = new JButton("Удалить");
        deleteButton.addActionListener(e -> deleteSelectedCategory());

        JButton operationsButton = new JButton("Операции по категории");
        operationsButton.addActionListener(e -> showCategoryOperationsDialog());

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(operationsButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createOperationsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Таблица операций
        String[] columns = { "ID", "Тип", "Счет", "Категория", "Сумма", "Дата", "Описание" };
        operationsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        operationsTable = new JTable(operationsModel);
        operationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Добавляем сортировку
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(operationsModel);
        operationsTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(operationsTable);

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton addButton = new JButton("Добавить операцию");
        addButton.addActionListener(e -> showAddOperationDialog());

        JButton deleteButton = new JButton("Удалить");
        deleteButton.addActionListener(e -> deleteSelectedOperation());

        JButton filterButton = new JButton("Фильтр по дате");
        filterButton.addActionListener(e -> showFilterDialog());

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(filterButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createAnalyticsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Панель общей статистики
        JPanel statsPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Общая статистика"));

        totalBalanceLabel = new JLabel("0.00", SwingConstants.RIGHT);
        totalIncomeLabel = new JLabel("0.00", SwingConstants.RIGHT);
        totalExpenseLabel = new JLabel("0.00", SwingConstants.RIGHT);

        JLabel accountsCountLabel = new JLabel("0", SwingConstants.RIGHT);

        statsPanel.add(new JLabel("Общий баланс:"));
        statsPanel.add(totalBalanceLabel);
        statsPanel.add(new JLabel("Общий доход:"));
        statsPanel.add(totalIncomeLabel);
        statsPanel.add(new JLabel("Общий расход:"));
        statsPanel.add(totalExpenseLabel);
        statsPanel.add(new JLabel("Количество счетов:"));
        statsPanel.add(accountsCountLabel);

        // Панель аналитики
        analyticsArea = new JTextArea(15, 40);
        analyticsArea.setEditable(false);
        JScrollPane analyticsScroll = new JScrollPane(analyticsArea);
        analyticsScroll.setBorder(BorderFactory.createTitledBorder("Детальная аналитика"));

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton refreshButton = new JButton("Обновить аналитику");
        refreshButton.addActionListener(e -> updateAnalytics());

        JButton periodButton = new JButton("Анализ за период");
        periodButton.addActionListener(e -> showPeriodAnalyticsDialog());

        JButton fullReportButton = new JButton("Полный отчет");
        fullReportButton.addActionListener(e -> showFullAnalyticsReport());

        JButton categoryStatsButton = new JButton("Статистика по категориям");
        categoryStatsButton.addActionListener(e -> showCategoryStatisticsDialog());

        buttonPanel.add(refreshButton);
        buttonPanel.add(periodButton);
        buttonPanel.add(fullReportButton);
        buttonPanel.add(categoryStatsButton);

        panel.add(statsPanel, BorderLayout.NORTH);
        panel.add(analyticsScroll, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadInitialData() {
        // Загружаем стандартные категории
        categoryService.loadDefaultCategories();

        // Создаем начальные счета
        accountService.createAccount("Основной счет");
        accountService.createAccount("Накопительный счет");
        accountService.createAccount("Кредитная карта");

        refreshAllTables();
        updateMaps();
    }

    private void updateMaps() {
        accountMap.clear();
        categoryMap.clear();
        operationMap.clear();

        for (BankAccount account : accountService.getAllAccounts()) {
            accountMap.put(account.getId(), account);
        }

        for (Category category : categoryService.getAllCategories()) {
            categoryMap.put(category.getId(), category);
        }

        for (Operation operation : operationService.getAllOperations()) {
            operationMap.put(operation.getId(), operation);
        }
    }

    private void refreshAllTables() {
        refreshAccountsTable();
        refreshCategoriesTable();
        refreshOperationsTable();
        updateAnalytics();
        updateMaps();
    }

    private void refreshAccountsTable() {
        accountsModel.setRowCount(0);
        for (BankAccount account : accountService.getAllAccounts()) {
            int operationCount = operationService.getOperationsByAccount(account.getId()).size();
            accountsModel.addRow(new Object[] {
                    account.getId().toString().substring(0, 8) + "...",
                    account.getName(),
                    String.format("%.2f", account.getBalance()),
                    operationCount
            });
        }
    }

    private void refreshCategoriesTable() {
        categoriesModel.setRowCount(0);
        for (Category category : categoryService.getAllCategories()) {
            int operationCount = operationService.getOperationsByCategory(category.getId()).size();
            categoriesModel.addRow(new Object[] {
                    category.getId().toString().substring(0, 8) + "...",
                    category.getType().getDescription(),
                    category.getName(),
                    operationCount
            });
        }
    }

    private void refreshOperationsTable() {
        operationsModel.setRowCount(0);
        for (Operation operation : operationService.getAllOperations()) {
            // Получаем название счета
            String accountName = accountService.getAccount(operation.getBankAccountId())
                    .map(BankAccount::getName)
                    .orElse("Неизвестный счет");

            // Получаем название категории
            String categoryName = categoryService.getCategory(operation.getCategoryId())
                    .map(Category::getName)
                    .orElse("Неизвестная категория");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

            operationsModel.addRow(new Object[] {
                    operation.getId().toString().substring(0, 8) + "...",
                    operation.getType().getDescription(),
                    accountName,
                    categoryName,
                    String.format("%.2f", operation.getAmount()),
                    operation.getDate().format(formatter),
                    operation.getDescription()
            });
        }
    }

    // ДИАЛОГОВЫЕ ОКНА

    private void showAddAccountDialog() {
        JTextField nameField = new JTextField(20);

        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.add(new JLabel("Название счета:"));
        panel.add(nameField);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Добавление счета", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                accountService.createAccount(name);
                refreshAllTables();
                JOptionPane.showMessageDialog(this, "Счет добавлен успешно!");
            } else {
                JOptionPane.showMessageDialog(this, "Введите название счета",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditAccountDialog() {
        int selectedRow = accountsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите счет для редактирования",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String accountIdStr = (String) accountsModel.getValueAt(selectedRow, 0);
        BankAccount account = findAccountByShortId(accountIdStr);

        if (account != null) {
            JTextField nameField = new JTextField(account.getName(), 20);

            JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
            panel.add(new JLabel("Название счета:"));
            panel.add(nameField);
            panel.add(new JLabel("Текущий баланс:"));
            panel.add(new JLabel(String.format("%.2f", account.getBalance())));

            int result = JOptionPane.showConfirmDialog(this, panel,
                    "Редактирование счета", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                String newName = nameField.getText().trim();
                if (!newName.isEmpty()) {
                    account.setName(newName);
                    refreshAllTables();
                    JOptionPane.showMessageDialog(this, "Счет обновлен успешно!");
                }
            }
        }
    }

    private void showAccountOperationsDialog() {
        int selectedRow = accountsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите счет",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String accountIdStr = (String) accountsModel.getValueAt(selectedRow, 0);
        BankAccount account = findAccountByShortId(accountIdStr);

        if (account != null) {
            List<Operation> operations = operationService.getOperationsByAccount(account.getId());

            StringBuilder sb = new StringBuilder();
            sb.append("Операции по счету: ").append(account.getName()).append("\n");
            sb.append("Баланс: ").append(String.format("%.2f", account.getBalance())).append("\n\n");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

            if (operations.isEmpty()) {
                sb.append("Нет операций");
            } else {
                BigDecimal totalIncome = BigDecimal.ZERO;
                BigDecimal totalExpense = BigDecimal.ZERO;

                for (Operation op : operations) {
                    if (op.getType() == OperationType.INCOME) {
                        totalIncome = totalIncome.add(op.getAmount());
                    } else {
                        totalExpense = totalExpense.add(op.getAmount());
                    }

                    String categoryName = categoryService.getCategory(op.getCategoryId())
                            .map(Category::getName)
                            .orElse("Неизвестно");

                    sb.append(String.format("%s %s: %.2f - %s (%s)\n",
                            op.getDate().format(formatter),
                            op.getType().getDescription(),
                            op.getAmount(),
                            op.getDescription() != null ? op.getDescription() : "без описания",
                            categoryName));
                }

                sb.append("\nИтого:\n");
                sb.append(String.format("Доходы: %.2f\n", totalIncome));
                sb.append(String.format("Расходы: %.2f\n", totalExpense));
                sb.append(String.format("Баланс: %.2f\n", totalIncome.subtract(totalExpense)));
            }

            JTextArea textArea = new JTextArea(sb.toString(), 20, 50);
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);

            JOptionPane.showMessageDialog(this, scrollPane,
                    "Операции по счету", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteSelectedAccount() {
        int selectedRow = accountsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите счет для удаления",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String accountIdStr = (String) accountsModel.getValueAt(selectedRow, 0);
        BankAccount account = findAccountByShortId(accountIdStr);

        if (account != null) {
            int operationCount = operationService.getOperationsByAccount(account.getId()).size();

            if (operationCount > 0) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "У счета есть " + operationCount + " операций. Удалить счет вместе с операциями?",
                        "Подтверждение удаления", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    // Удаляем все операции счета
                    for (Operation op : operationService.getOperationsByAccount(account.getId())) {
                        operationService.deleteOperation(op.getId());
                    }
                    accountService.deleteAccount(account.getId());
                    refreshAllTables();
                    JOptionPane.showMessageDialog(this, "Счет и операции удалены!");
                }
            } else {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Удалить счет \"" + account.getName() + "\"?",
                        "Подтверждение удаления", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    if (accountService.deleteAccount(account.getId())) {
                        refreshAllTables();
                        JOptionPane.showMessageDialog(this, "Счет удален!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Не удалось удалить счет",
                                "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    private void showAddCategoryDialog() {
        JComboBox<OperationType> typeCombo = new JComboBox<>(OperationType.values());
        JTextField nameField = new JTextField(20);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.add(new JLabel("Тип категории:"));
        panel.add(typeCombo);
        panel.add(new JLabel("Название категории:"));
        panel.add(nameField);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Добавление категории", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            OperationType type = (OperationType) typeCombo.getSelectedItem();
            String name = nameField.getText().trim();

            if (!name.isEmpty()) {
                // Проверяем, не существует ли уже такая категория
                boolean exists = categoryService.getAllCategories().stream()
                        .anyMatch(c -> c.getType() == type && c.getName().equalsIgnoreCase(name));

                if (exists) {
                    JOptionPane.showMessageDialog(this, "Такая категория уже существует",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                } else {
                    categoryService.createCategory(type, name);
                    refreshAllTables();
                    JOptionPane.showMessageDialog(this, "Категория добавлена успешно!");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Введите название категории",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showCategoryManagerDialog() {
        JDialog dialog = new JDialog(this, "Управление категориями", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Таблица категорий
        String[] columns = { "Тип", "Название", "Операций", "Действия" };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Только колонка с действиями редактируемая
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 3 ? JButton.class : String.class;
            }
        };

        JTable table = new JTable(model);

        // Заполняем таблицу
        for (Category category : categoryService.getAllCategories()) {
            int operationCount = operationService.getOperationsByCategory(category.getId()).size();
            JButton deleteButton = new JButton("Удалить");
            deleteButton.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row != -1) {
                    String categoryName = (String) model.getValueAt(row, 1);
                    Category cat = categoryService.getAllCategories().stream()
                            .filter(c -> c.getName().equals(categoryName))
                            .findFirst()
                            .orElse(null);

                    if (cat != null) {
                        if (operationCount > 0) {
                            int confirm = JOptionPane.showConfirmDialog(dialog,
                                    "У категории есть " + operationCount + " операций. Удалить категорию?",
                                    "Подтверждение", JOptionPane.YES_NO_OPTION);

                            if (confirm == JOptionPane.YES_OPTION) {
                                categoryService.deleteCategory(cat.getId());
                                refreshAllTables();
                                dialog.dispose();
                                showCategoryManagerDialog();
                            }
                        } else {
                            categoryService.deleteCategory(cat.getId());
                            refreshAllTables();
                            dialog.dispose();
                            showCategoryManagerDialog();
                        }
                    }
                }
            });

            model.addRow(new Object[] {
                    category.getType().getDescription(),
                    category.getName(),
                    String.valueOf(operationCount),
                    deleteButton
            });
        }

        JScrollPane scrollPane = new JScrollPane(table);

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton addButton = new JButton("Добавить категорию");
        addButton.addActionListener(e -> {
            dialog.dispose();
            showAddCategoryDialog();
            showCategoryManagerDialog();
        });

        JButton closeButton = new JButton("Закрыть");
        closeButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(addButton);
        buttonPanel.add(closeButton);

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private void showCategoryOperationsDialog() {
        int selectedRow = categoriesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите категорию",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String categoryIdStr = (String) categoriesModel.getValueAt(selectedRow, 0);
        Category category = findCategoryByShortId(categoryIdStr);

        if (category != null) {
            List<Operation> operations = operationService.getOperationsByCategory(category.getId());

            StringBuilder sb = new StringBuilder();
            sb.append("Операции по категории: ").append(category.getName()).append("\n");
            sb.append("Тип: ").append(category.getType().getDescription()).append("\n\n");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            BigDecimal total = BigDecimal.ZERO;

            if (operations.isEmpty()) {
                sb.append("Нет операций");
            } else {
                for (Operation op : operations) {
                    total = total.add(op.getAmount());
                    String accountName = accountService.getAccount(op.getBankAccountId())
                            .map(BankAccount::getName)
                            .orElse("Неизвестно");

                    sb.append(String.format("%s: %.2f - %s (%s)\n",
                            op.getDate().format(formatter),
                            op.getAmount(),
                            op.getDescription() != null ? op.getDescription() : "без описания",
                            accountName));
                }

                sb.append("\nИтого: ").append(String.format("%.2f", total));
            }

            JTextArea textArea = new JTextArea(sb.toString(), 15, 50);
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);

            JOptionPane.showMessageDialog(this, scrollPane,
                    "Операции по категории", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteSelectedCategory() {
        int selectedRow = categoriesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите категорию для удаления",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String categoryIdStr = (String) categoriesModel.getValueAt(selectedRow, 0);
        Category category = findCategoryByShortId(categoryIdStr);

        if (category != null) {
            int operationCount = operationService.getOperationsByCategory(category.getId()).size();

            if (operationCount > 0) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "У категории есть " + operationCount + " операций. Удалить категорию?\n" +
                                "Операции останутся без категории.",
                        "Подтверждение удаления", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    categoryService.deleteCategory(category.getId());
                    refreshAllTables();
                    JOptionPane.showMessageDialog(this, "Категория удалена!");
                }
            } else {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Удалить категорию \"" + category.getName() + "\"?",
                        "Подтверждение удаления", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    categoryService.deleteCategory(category.getId());
                    refreshAllTables();
                    JOptionPane.showMessageDialog(this, "Категория удалена!");
                }
            }
        }
    }

    private void showAddOperationDialog() {
        // Получаем списки счетов и категорий
        List<BankAccount> accounts = accountService.getAllAccounts();
        List<Category> incomeCategories = categoryService.getCategoriesByType(OperationType.INCOME);
        List<Category> expenseCategories = categoryService.getCategoriesByType(OperationType.EXPENSE);

        if (accounts.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Сначала создайте хотя бы один счет",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JComboBox<BankAccount> accountCombo = new JComboBox<>(accounts.toArray(new BankAccount[0]));
        JComboBox<OperationType> typeCombo = new JComboBox<>(OperationType.values());
        JComboBox<Category> categoryCombo = new JComboBox<>();
        JTextField amountField = new JTextField(15);
        JTextArea descriptionArea = new JTextArea(3, 20);
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);

        // Обновляем категории при изменении типа операции
        typeCombo.addActionListener(e -> {
            categoryCombo.removeAllItems();
            OperationType selectedType = (OperationType) typeCombo.getSelectedItem();
            List<Category> categories = selectedType == OperationType.INCOME ? incomeCategories : expenseCategories;

            for (Category cat : categories) {
                categoryCombo.addItem(cat);
            }
        });

        // Инициализируем начальные значения
        typeCombo.setSelectedItem(OperationType.INCOME);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Счет:"), gbc);
        gbc.gridx = 1;
        panel.add(accountCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Тип операции:"), gbc);
        gbc.gridx = 1;
        panel.add(typeCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Категория:"), gbc);
        gbc.gridx = 1;
        panel.add(categoryCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Сумма:"), gbc);
        gbc.gridx = 1;
        panel.add(amountField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Описание:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        panel.add(descriptionScroll, gbc);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Добавление операции", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                BankAccount selectedAccount = (BankAccount) accountCombo.getSelectedItem();
                OperationType selectedType = (OperationType) typeCombo.getSelectedItem();
                Category selectedCategory = (Category) categoryCombo.getSelectedItem();
                BigDecimal amount = new BigDecimal(amountField.getText().trim());
                String description = descriptionArea.getText().trim();

                if (selectedCategory == null) {
                    JOptionPane.showMessageDialog(this, "Выберите категорию",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    JOptionPane.showMessageDialog(this, "Сумма должна быть положительной",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                operationService.createOperation(selectedType, selectedAccount.getId(),
                        amount, selectedCategory.getId(), description);

                refreshAllTables();

                JOptionPane.showMessageDialog(this,
                        String.format("Операция добавлена успешно!\nНовый баланс счета: %.2f",
                                selectedAccount.getBalance()),
                        "Успех", JOptionPane.INFORMATION_MESSAGE);

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Введите корректную сумму",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Ошибка: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showFilterDialog() {
        JSpinner startDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner endDateSpinner = new JSpinner(new SpinnerDateModel());
        JComboBox<String> typeFilter = new JComboBox<>(new String[] { "Все", "Доходы", "Расходы" });
        JComboBox<BankAccount> accountFilter = new JComboBox<>();
        accountFilter.addItem(null); // Все счета
        for (BankAccount account : accountService.getAllAccounts()) {
            accountFilter.addItem(account);
        }

        startDateSpinner.setEditor(new JSpinner.DateEditor(startDateSpinner, "dd.MM.yyyy"));
        endDateSpinner.setEditor(new JSpinner.DateEditor(endDateSpinner, "dd.MM.yyyy"));

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.add(new JLabel("Начальная дата:"));
        panel.add(startDateSpinner);
        panel.add(new JLabel("Конечная дата:"));
        panel.add(endDateSpinner);
        panel.add(new JLabel("Тип операции:"));
        panel.add(typeFilter);
        panel.add(new JLabel("Счет:"));
        panel.add(accountFilter);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Фильтр операций", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            Date startDate = (Date) startDateSpinner.getValue();
            Date endDate = (Date) endDateSpinner.getValue();
            BankAccount selectedAccount = (BankAccount) accountFilter.getSelectedItem();
            String type = (String) typeFilter.getSelectedItem();

            LocalDateTime start = LocalDateTime.ofInstant(startDate.toInstant(),
                    java.time.ZoneId.systemDefault());
            LocalDateTime end = LocalDateTime.ofInstant(endDate.toInstant(),
                    java.time.ZoneId.systemDefault());

            List<Operation> filteredOperations = operationService.getOperationsByDateRange(start, end);

            if (selectedAccount != null) {
                filteredOperations = filteredOperations.stream()
                        .filter(op -> op.getBankAccountId().equals(selectedAccount.getId()))
                        .collect(Collectors.toList());
            }

            if ("Доходы".equals(type)) {
                filteredOperations = filteredOperations.stream()
                        .filter(op -> op.getType() == OperationType.INCOME)
                        .collect(Collectors.toList());
            } else if ("Расходы".equals(type)) {
                filteredOperations = filteredOperations.stream()
                        .filter(op -> op.getType() == OperationType.EXPENSE)
                        .collect(Collectors.toList());
            }

            // Обновляем таблицу
            operationsModel.setRowCount(0);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

            for (Operation operation : filteredOperations) {
                String accountName = accountService.getAccount(operation.getBankAccountId())
                        .map(BankAccount::getName)
                        .orElse("Неизвестный счет");

                String categoryName = categoryService.getCategory(operation.getCategoryId())
                        .map(Category::getName)
                        .orElse("Неизвестная категория");

                operationsModel.addRow(new Object[] {
                        operation.getId().toString().substring(0, 8) + "...",
                        operation.getType().getDescription(),
                        accountName,
                        categoryName,
                        String.format("%.2f", operation.getAmount()),
                        operation.getDate().format(formatter),
                        operation.getDescription()
                });
            }

            JOptionPane.showMessageDialog(this,
                    "Найдено операций: " + filteredOperations.size(),
                    "Результаты фильтрации", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteSelectedOperation() {
        int selectedRow = operationsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите операцию для удаления",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String operationIdStr = (String) operationsModel.getValueAt(selectedRow, 0);
        Operation operation = findOperationByShortId(operationIdStr);

        if (operation != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    String.format("Удалить операцию?\n%s: %.2f - %s",
                            operation.getType().getDescription(),
                            operation.getAmount(),
                            operation.getDescription() != null ? operation.getDescription() : "без описания"),
                    "Подтверждение удаления", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (operationService.deleteOperation(operation.getId())) {
                    refreshAllTables();
                    JOptionPane.showMessageDialog(this, "Операция удалена!");
                } else {
                    JOptionPane.showMessageDialog(this, "Не удалось удалить операцию",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void updateAnalytics() {
        // Общая статистика
        BigDecimal totalBalance = accountService.calculateTotalBalance();
        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
        BigDecimal totalIncome = operationService.getTotalIncome(oneYearAgo, LocalDateTime.now());
        BigDecimal totalExpense = operationService.getTotalExpense(oneYearAgo, LocalDateTime.now());

        totalBalanceLabel.setText(String.format("%.2f", totalBalance));
        totalIncomeLabel.setText(String.format("%.2f", totalIncome));
        totalExpenseLabel.setText(String.format("%.2f", totalExpense));

        // Базовая аналитика
        LocalDateTime startDate = LocalDateTime.now().minusMonths(1);
        LocalDateTime endDate = LocalDateTime.now();

        String analyticsText = analyticsService.generateAnalyticsReport(startDate, endDate);
        analyticsArea.setText(analyticsText);
    }

    private void showPeriodAnalyticsDialog() {
        JSpinner startDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner endDateSpinner = new JSpinner(new SpinnerDateModel());

        startDateSpinner.setEditor(new JSpinner.DateEditor(startDateSpinner, "dd.MM.yyyy"));
        endDateSpinner.setEditor(new JSpinner.DateEditor(endDateSpinner, "dd.MM.yyyy"));

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.add(new JLabel("Начальная дата:"));
        panel.add(startDateSpinner);
        panel.add(new JLabel("Конечная дата:"));
        panel.add(endDateSpinner);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Анализ за период", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            Date startDate = (Date) startDateSpinner.getValue();
            Date endDate = (Date) endDateSpinner.getValue();

            LocalDateTime start = LocalDateTime.ofInstant(startDate.toInstant(),
                    java.time.ZoneId.systemDefault());
            LocalDateTime end = LocalDateTime.ofInstant(endDate.toInstant(),
                    java.time.ZoneId.systemDefault());

            String analyticsText = analyticsService.generateAnalyticsReport(start, end);

            JTextArea textArea = new JTextArea(analyticsText, 20, 60);
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);

            JOptionPane.showMessageDialog(this, scrollPane,
                    "Анализ за период", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showFullAnalyticsReport() {
        LocalDateTime startDate = LocalDateTime.now().minusYears(1);
        LocalDateTime endDate = LocalDateTime.now();

        String analyticsText = analyticsService.generateAnalyticsReport(startDate, endDate);

        JTextArea textArea = new JTextArea(analyticsText, 25, 70);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        JOptionPane.showMessageDialog(this, scrollPane,
                "Полный финансовый отчет", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showCategoryStatisticsDialog() {
        JDialog dialog = new JDialog(this, "Статистика по категориям", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Доходы
        Map<String, BigDecimal> incomeStats = analyticsService.getCategoryStatistics(OperationType.INCOME);
        JPanel incomePanel = createCategoryStatsPanel(incomeStats, "Доходы");
        tabbedPane.addTab("Доходы", incomePanel);

        // Расходы
        Map<String, BigDecimal> expenseStats = analyticsService.getCategoryStatistics(OperationType.EXPENSE);
        JPanel expensePanel = createCategoryStatsPanel(expenseStats, "Расходы");
        tabbedPane.addTab("Расходы", expensePanel);

        dialog.add(tabbedPane);
        dialog.setVisible(true);
    }

    private JPanel createCategoryStatsPanel(Map<String, BigDecimal> stats, String title) {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = { "Категория", "Сумма", "Процент" };
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);

        BigDecimal total = stats.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        for (Map.Entry<String, BigDecimal> entry : stats.entrySet()) {
            BigDecimal amount = entry.getValue();
            BigDecimal percentage = total.compareTo(BigDecimal.ZERO) > 0
                    ? amount.multiply(new BigDecimal(100)).divide(total, 2, BigDecimal.ROUND_HALF_UP)
                    : BigDecimal.ZERO;

            model.addRow(new Object[] {
                    entry.getKey(),
                    String.format("%.2f", amount),
                    String.format("%.2f%%", percentage)
            });
        }

        // Итого
        model.addRow(new Object[] { "ИТОГО:", String.format("%.2f", total), "100.00%" });

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // ФУНКЦИОНАЛ МЕНЮ "ФАЙЛ"

    private void importDataFromCsv() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выберите файл для импорта");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                // В реальном приложении здесь была бы реализация парсинга CSV
                JOptionPane.showMessageDialog(this,
                        "Импорт данных будет реализован в следующей версии\n" +
                                "Выбран файл: " + file.getName(),
                        "Информация", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Ошибка импорта: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportDataToCsv() {
        String fileName = JOptionPane.showInputDialog(this,
                "Введите базовое имя для файлов (без расширения):",
                "tigerbank_finances_" + System.currentTimeMillis());

        if (fileName != null && !fileName.trim().isEmpty()) {
            try {
                fileExporter.exportAllToCsv(fileName.trim(),
                        accountService.getAllAccounts(),
                        categoryService.getAllCategories(),
                        operationService.getAllOperations());

                JOptionPane.showMessageDialog(this,
                        "Данные успешно экспортированы в CSV файлы:\n" +
                                "- " + fileName + "_accounts.csv\n" +
                                "- " + fileName + "_categories.csv\n" +
                                "- " + fileName + "_operations.csv",
                        "Экспорт завершен", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Ошибка экспорта: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportDataToJson() {
        String fileName = JOptionPane.showInputDialog(this,
                "Введите базовое имя для файлов (без расширения):",
                "tigerbank_finances_" + System.currentTimeMillis());

        if (fileName != null && !fileName.trim().isEmpty()) {
            try {
                fileExporter.exportAllToJson(fileName.trim(),
                        accountService.getAllAccounts(),
                        categoryService.getAllCategories(),
                        operationService.getAllOperations());

                JOptionPane.showMessageDialog(this,
                        "Данные успешно экспортированы в JSON файлы:\n" +
                                "- " + fileName + "_accounts.json\n" +
                                "- " + fileName + "_categories.json\n" +
                                "- " + fileName + "_operations.json",
                        "Экспорт завершен", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Ошибка экспорта: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportAnalyticsReport() {
        LocalDateTime startDate = LocalDateTime.now().minusYears(1);
        LocalDateTime endDate = LocalDateTime.now();

        String report = analyticsService.generateAnalyticsReport(startDate, endDate);

        String fileName = JOptionPane.showInputDialog(this,
                "Введите имя файла для отчета:",
                "financial_report_" + System.currentTimeMillis() + ".txt");

        if (fileName != null && !fileName.trim().isEmpty()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
                writer.println(report);
                JOptionPane.showMessageDialog(this,
                        "Отчет успешно экспортирован в файл: " + fileName,
                        "Экспорт завершен", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Ошибка экспорта: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ФУНКЦИОНАЛ МЕНЮ "НАСТРОЙКИ"

    private void loadDefaultCategories() {
        int result = JOptionPane.showConfirmDialog(this,
                "Загрузить стандартные категории?\n" +
                        "Существующие категории не будут удалены.",
                "Загрузка стандартных категорий",
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            categoryService.loadDefaultCategories();
            refreshAllTables();
            JOptionPane.showMessageDialog(this,
                    "Стандартные категории загружены успешно!",
                    "Успех", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void resetAllData() {
        int result = JOptionPane.showConfirmDialog(this,
                "ВНИМАНИЕ! Вы собираетесь удалить ВСЕ данные:\n" +
                        "- Все счета\n" +
                        "- Все категории\n" +
                        "- Все операции\n\n" +
                        "Это действие нельзя отменить. Продолжить?",
                "Сброс всех данных",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            // В реальном приложении здесь бы очищались все репозитории
            JOptionPane.showMessageDialog(this,
                    "Функция сброса данных будет реализована в следующей версии",
                    "Информация", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ

    private BankAccount findAccountByShortId(String shortId) {
        for (BankAccount account : accountService.getAllAccounts()) {
            if (account.getId().toString().startsWith(shortId.replace("...", ""))) {
                return account;
            }
        }
        return null;
    }

    private Category findCategoryByShortId(String shortId) {
        for (Category category : categoryService.getAllCategories()) {
            if (category.getId().toString().startsWith(shortId.replace("...", ""))) {
                return category;
            }
        }
        return null;
    }

    private Operation findOperationByShortId(String shortId) {
        for (Operation operation : operationService.getAllOperations()) {
            if (operation.getId().toString().startsWith(shortId.replace("...", ""))) {
                return operation;
            }
        }
        return null;
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
                "ТигрБанк - Учет финансов\n" +
                        "Версия 2.0\n\n" +
                        "Функции:\n" +
                        "1. Управление счетами с полной связью операций\n" +
                        "2. Предустановленные и пользовательские категории\n" +
                        "3. Учет доходов и расходов\n" +
                        "4. Полная финансовая аналитика\n" +
                        "5. Импорт/экспорт данных\n\n" +
                        "Все данные связаны через ID для целостности системы.",
                "О программе", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            Main app = new Main();
            app.setVisible(true);
        });
    }
}