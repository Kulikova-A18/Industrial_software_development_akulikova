# Домашнее задание №2. Модуль 5. Практика: консольный калькулятор, работа с массивами и классами

Нужно написать консольное приложение, которое моделирует работу банка: создание счетов, операции, отчёты и начисления.

## Запуск

**Требования**:

- Java JDK 8 или выше
- Командная строка/терминал

Имеется возможность как самостоятельно запустить:

```bash
javac BankApplication.java
java BankApplication
```

Так и запустить скрипт:

```bash
./run.sh
```

## Меню приложения

После запуска, пользователя встречает меню приложения:

```java
BANKING SYSTEM MENU
1. Create New Customer
2. Open Debit Account
3. Open Credit Account
4. Deposit Money
5. Withdraw Money
6. Transfer Money
7. Show Customer Accounts
8. Show Transaction History
9. Generate Bank Report
10. Exit
```

Пользователь может взаимодействовать с меню выбирая нужный пункт

```java
Enter your choice (1-10):
```

## Архитектура проекта


### Схема иерархии классов

```mermaid
classDiagram
    class BankApplication {
        -Bank bank
        -Scanner scanner
        +main(String[] args)
        +createCustomer()
        +openDebitAccount()
        +openCreditAccount()
        +deposit()
        +withdraw()
        +transfer()
    }
    
    class Bank {
        -List~Customer~ customers
        -List~Account~ accounts
        -List~Transaction~ transactions
        +createCustomer(String fullName) Customer
        +openDebitAccount(Customer owner) Account
        +openCreditAccount(Customer owner, double creditLimit) Account
        +deposit(String accountNumber, double amount) boolean
        +withdraw(String accountNumber, double amount) boolean
        +transfer(String from, String to, double amount) boolean
        +printReport()
    }
    
    class Customer {
        -int id
        -String fullName
        +Customer(String fullName)
        +getId() int
        +getFullName() String
    }
    
    class Account {
        <<abstract>>
        -String accountNumber
        -double balance
        -Customer owner
        +deposit(double amount) boolean
        +withdraw(double amount) boolean
        +transfer(Account to, double amount) boolean
        +getBalance() double
        +getAccountNumber() String
        +getOwner() Customer
    }
    
    class DebitAccount {
        +DebitAccount(Customer owner)
    }
    
    class CreditAccount {
        -double creditLimit
        +CreditAccount(Customer owner, double creditLimit)
        +withdraw(double amount) boolean
        +getCreditLimit() double
    }
    
    class Transaction {
        -TransactionType type
        -double amount
        -String fromAccountNumber
        -String toAccountNumber
        -LocalDateTime timestamp
        -boolean success
        -String message
        +Transaction(...)
        +getType() TransactionType
        +getAmount() double
        +isSuccess() boolean
    }
    
    class TransactionType {
        <<enumeration>>
        DEPOSIT
        WITHDRAW
        TRANSFER
    }
    
    BankApplication --> Bank
    Bank --> Customer : manages
    Bank --> Account : manages
    Bank --> Transaction : logs
    Account <|-- DebitAccount : extends
    Account <|-- CreditAccount : extends
    Account --> Customer : belongs to
    Transaction --> TransactionType : has type
```

### Схема потока данных

```mermaid
flowchart TD
    A[Пользователь] --> B[Консольное меню]
    B --> C{Выбор операции}
    
    C --> D[Создать клиента]
    C --> E[Открыть счет]
    C --> F[Операции со счетом]
    C --> G[Отчеты]
    
    D --> H[Bank.createCustomer]
    H --> I[Customer объект]
    I --> J[Добавлен в список клиентов]
    
    E --> K{Тип счета}
    K --> L[Дебетовый]
    K --> M[Кредитный]
    L --> N[Bank.openDebitAccount]
    M --> O[Bank.openCreditAccount]
    N --> P[DebitAccount создан]
    O --> Q[CreditAccount создан]
    
    F --> R{Тип операции}
    R --> S[Пополнение]
    R --> T[Снятие]
    R --> U[Перевод]
    S --> V[Bank.deposit]
    T --> W[Bank.withdraw]
    U --> X[Bank.transfer]
    
    V --> Y[Transaction запись]
    W --> Y
    X --> Y
    
    G --> Z{Тип отчета}
    Z --> AA[Счета клиента]
    Z --> BB[История операций]
    Z --> CC[Общий отчет]
    AA --> DD[Bank.printCustomerAccounts]
    BB --> EE[Bank.printTransactions]
    CC --> FF[Bank.printReport]
```

### Таблица классов и зависимостей

| Класс | Назначение | Зависит от | Связан с |
|-------|------------|------------|----------|
| **BankApplication** | Точка входа, меню | Bank, Scanner | Bank |
| **Bank** | Центральный контроллер | Customer, Account, Transaction | Все основные классы |
| **Customer** | Данные клиента | - | Account, Bank |
| **Account** (абстрактный) | Базовый счет | Customer | Bank, транзакции |
| **DebitAccount** | Дебетовый счет | Account | Bank |
| **CreditAccount** | Кредитный счет | Account | Bank |
| **Transaction** | Запись операции | TransactionType | Bank |
| **TransactionType** | Типы операций | - | Transaction |

### Таблица основных методов

| Метод | Класс | Параметры | Возвращает | Описание |
|-------|-------|-----------|------------|----------|
| createCustomer | Bank | String fullName | Customer | Создает нового клиента |
| openDebitAccount | Bank | Customer owner | Account | Открывает дебетовый счет |
| openCreditAccount | Bank | Customer owner, double limit | Account | Открывает кредитный счет |
| deposit | Bank | String accNum, double amount | boolean | Пополнение счета |
| withdraw | Bank | String accNum, double amount | boolean | Снятие со счета |
| transfer | Bank | String from, String to, double amount | boolean | Перевод между счетами |
| deposit | Account | double amount | boolean | Изменение баланса |
| withdraw | Account | double amount | boolean | Списание средств |
| transfer | Account | Account to, double amount | boolean | Перевод на другой счет |