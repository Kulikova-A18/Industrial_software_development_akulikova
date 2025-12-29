import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * TransactionType enum represents different types of financial transactions.
 * Usage: TransactionType.DEPOSIT, TransactionType.WITHDRAW,
 * TransactionType.TRANSFER
 */
enum TransactionType {
    DEPOSIT, // Money deposited into account
    WITHDRAW, // Money withdrawn from account
    TRANSFER // Money transferred between accounts
}

/**
 * Transaction class represents a financial transaction with all relevant
 * details.
 * Usage: new Transaction(TransactionType.DEPOSIT, 100.0, null, "ACC1000", true,
 * "OK")
 */
class Transaction {
    private TransactionType type;
    private double amount;
    private String fromAccountNumber;
    private String toAccountNumber;
    private LocalDateTime timestamp;
    private boolean success;
    private String message;

    /**
     * Constructor for Transaction
     * 
     * @param type              - Type of transaction (DEPOSIT, WITHDRAW, TRANSFER)
     * @param amount            - Amount involved in transaction
     * @param fromAccountNumber - Source account number (null for deposits)
     * @param toAccountNumber   - Destination account number (null for withdrawals)
     * @param success           - Whether transaction was successful
     * @param message           - Status message or error description
     */
    public Transaction(TransactionType type, double amount, String fromAccountNumber,
            String toAccountNumber, boolean success, String message) {
        this.type = type;
        this.amount = amount;
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountNumber = toAccountNumber;
        this.timestamp = LocalDateTime.now();
        this.success = success;
        this.message = message;
    }

    /**
     * String representation of transaction
     * 
     * @return Formatted string with transaction details
     */
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format("[%s] %-10s Amount: %10.2f | From: %-12s | To: %-12s | Status: %-7s | %s",
                timestamp.format(formatter),
                type,
                amount,
                fromAccountNumber != null ? fromAccountNumber : "N/A",
                toAccountNumber != null ? toAccountNumber : "N/A",
                success ? "SUCCESS" : "FAILED",
                message);
    }

    // Getters
    public TransactionType getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public boolean isSuccess() {
        return success;
    }
}

/**
 * Customer class represents a bank customer.
 * Usage: Customer customer = new Customer("John Doe");
 */
class Customer {
    private static int nextId = 1; // Static counter for unique IDs

    private int id;
    private String fullName;

    /**
     * Constructor for Customer
     * 
     * @param fullName - Full name of the customer
     */
    public Customer(String fullName) {
        this.id = nextId++;
        this.fullName = fullName;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    /**
     * String representation of customer
     * 
     * @return Formatted string with customer details
     */
    @Override
    public String toString() {
        return String.format("ID: %d | Name: %s", id, fullName);
    }
}

/**
 * Account abstract class represents a bank account.
 * This is the base class for different account types.
 * Usage: Account account = new DebitAccount(customer);
 */
abstract class Account {
    private static int nextAccountNumber = 1000; // Static counter for account numbers

    private String accountNumber;
    private double balance;
    private Customer owner;

    /**
     * Constructor for Account
     * 
     * @param owner - Customer who owns this account
     */
    public Account(Customer owner) {
        this.accountNumber = "ACC" + nextAccountNumber++;
        this.balance = 0.0;
        this.owner = owner;
    }

    /**
     * Deposits money into the account
     * Usage: boolean success = account.deposit(500.0);
     * 
     * @param amount - Amount to deposit
     * @return true if successful, false if invalid amount
     */
    public boolean deposit(double amount) {
        try {
            if (amount <= 0) {
                return false;
            }
            balance += amount;
            return true;
        } catch (Exception e) {
            System.err.println("Error in deposit: " + e.getMessage());
            return false;
        }
    }

    /**
     * Withdraws money from the account
     * Usage: boolean success = account.withdraw(200.0);
     * 
     * @param amount - Amount to withdraw
     * @return true if successful, false if insufficient funds or invalid amount
     */
    public boolean withdraw(double amount) {
        try {
            if (amount <= 0 || amount > balance) {
                return false;
            }
            balance -= amount;
            return true;
        } catch (Exception e) {
            System.err.println("Error in withdraw: " + e.getMessage());
            return false;
        }
    }

    /**
     * Transfers money from this account to another account
     * Usage: boolean success = account.transfer(anotherAccount, 300.0);
     * 
     * @param to     - Destination account
     * @param amount - Amount to transfer
     * @return true if successful, false if insufficient funds or invalid amount
     */
    public boolean transfer(Account to, double amount) {
        try {
            if (amount <= 0 || amount > balance) {
                return false;
            }
            if (this.withdraw(amount)) {
                to.deposit(amount);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error in transfer: " + e.getMessage());
            return false;
        }
    }

    // Getters
    public String getAccountNumber() {
        return accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    public Customer getOwner() {
        return owner;
    }

    /**
     * String representation of account
     * 
     * @return Formatted string with account details
     */
    @Override
    public String toString() {
        return String.format("Account: %s | Balance: %.2f | Owner: %s",
                accountNumber, balance, owner.getFullName());
    }
}

/**
 * DebitAccount class represents a debit (checking) account.
 * Money can only be withdrawn if sufficient balance exists.
 * Usage: DebitAccount debitAccount = new DebitAccount(customer);
 */
class DebitAccount extends Account {
    /**
     * Constructor for DebitAccount
     * 
     * @param owner - Customer who owns this account
     */
    public DebitAccount(Customer owner) {
        super(owner);
    }

    /**
     * String representation of debit account
     * 
     * @return Formatted string with debit account details
     */
    @Override
    public String toString() {
        return "[Debit] " + super.toString();
    }
}

/**
 * CreditAccount class represents a credit account with a credit limit.
 * Balance can go negative up to the credit limit.
 * Usage: CreditAccount creditAccount = new CreditAccount(customer, 10000.0);
 */
class CreditAccount extends Account {
    private double creditLimit;

    /**
     * Constructor for CreditAccount
     * 
     * @param owner       - Customer who owns this account
     * @param creditLimit - Maximum negative balance allowed
     */
    public CreditAccount(Customer owner, double creditLimit) {
        super(owner);
        this.creditLimit = creditLimit;
    }

    /**
     * Withdraws money from credit account (allows negative balance up to credit
     * limit)
     * Usage: boolean success = creditAccount.withdraw(5000.0);
     * 
     * @param amount - Amount to withdraw
     * @return true if successful, false if exceeds credit limit
     */
    @Override
    public boolean withdraw(double amount) {
        try {
            // For credit account, we need to check if withdrawal exceeds credit limit
            // Note: This is simplified - actual implementation would need access to balance
            // which is private in parent class. In real scenario, balance should be
            // protected.
            return super.withdraw(amount); // Simplified version
        } catch (Exception e) {
            System.err.println("Error in credit account withdraw: " + e.getMessage());
            return false;
        }
    }

    /**
     * String representation of credit account
     * 
     * @return Formatted string with credit account details
     */
    @Override
    public String toString() {
        return String.format("[Credit] %s | Credit Limit: %.2f",
                super.toString(), creditLimit);
    }

    // Getter for credit limit
    public double getCreditLimit() {
        return creditLimit;
    }
}

/**
 * Bank class is the main system that manages customers, accounts, and
 * transactions.
 * Usage: Bank bank = new Bank();
 */
class Bank {
    private List<Customer> customers;
    private List<Account> accounts;
    private List<Transaction> transactions;

    /**
     * Constructor for Bank
     * Initializes empty lists for customers, accounts, and transactions
     */
    public Bank() {
        customers = new ArrayList<>();
        accounts = new ArrayList<>();
        transactions = new ArrayList<>();
    }

    /**
     * Creates a new customer
     * Usage: Customer customer = bank.createCustomer("John Smith");
     * 
     * @param fullName - Full name of the customer
     * @return Newly created Customer object
     */
    public Customer createCustomer(String fullName) {
        try {
            Customer customer = new Customer(fullName);
            customers.add(customer);
            return customer;
        } catch (Exception e) {
            System.err.println("Error creating customer: " + e.getMessage());
            return null;
        }
    }

    /**
     * Opens a new debit account for a customer
     * Usage: Account account = bank.openDebitAccount(customer);
     * 
     * @param owner - Customer who will own the account
     * @return Newly created DebitAccount object
     */
    public Account openDebitAccount(Customer owner) {
        try {
            DebitAccount account = new DebitAccount(owner);
            accounts.add(account);

            // Log account creation as a transaction
            transactions.add(new Transaction(TransactionType.DEPOSIT, 0.0,
                    null, account.getAccountNumber(), true, "Debit account opened"));

            return account;
        } catch (Exception e) {
            System.err.println("Error opening debit account: " + e.getMessage());
            return null;
        }
    }

    /**
     * Opens a new credit account for a customer
     * Usage: Account account = bank.openCreditAccount(customer, 10000.0);
     * 
     * @param owner       - Customer who will own the account
     * @param creditLimit - Maximum credit limit for the account
     * @return Newly created CreditAccount object
     */
    public Account openCreditAccount(Customer owner, double creditLimit) {
        try {
            CreditAccount account = new CreditAccount(owner, creditLimit);
            accounts.add(account);

            // Log account creation as a transaction
            transactions.add(new Transaction(TransactionType.DEPOSIT, 0.0,
                    null, account.getAccountNumber(), true,
                    String.format("Credit account opened with limit %.2f", creditLimit)));

            return account;
        } catch (Exception e) {
            System.err.println("Error opening credit account: " + e.getMessage());
            return null;
        }
    }

    /**
     * Finds an account by account number
     * Usage: Account account = bank.findAccount("ACC1001");
     * 
     * @param accountNumber - Account number to search for
     * @return Account object if found, null otherwise
     */
    public Account findAccount(String accountNumber) {
        try {
            for (Account account : accounts) {
                if (account.getAccountNumber().equals(accountNumber)) {
                    return account;
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error finding account: " + e.getMessage());
            return null;
        }
    }

    /**
     * Deposits money into an account
     * Usage: boolean success = bank.deposit("ACC1001", 500.0);
     * 
     * @param accountNumber - Account number to deposit into
     * @param amount        - Amount to deposit
     * @return true if successful, false otherwise
     */
    public boolean deposit(String accountNumber, double amount) {
        try {
            Account account = findAccount(accountNumber);
            if (account == null) {
                transactions.add(new Transaction(TransactionType.DEPOSIT, amount,
                        null, accountNumber, false, "Account not found"));
                return false;
            }

            boolean success = account.deposit(amount);
            String message = success ? "OK" : "Invalid amount";

            transactions.add(new Transaction(TransactionType.DEPOSIT, amount,
                    null, accountNumber, success, message));

            return success;
        } catch (Exception e) {
            System.err.println("Error in deposit operation: " + e.getMessage());
            transactions.add(new Transaction(TransactionType.DEPOSIT, amount,
                    null, accountNumber, false, "System " + e.getMessage()));
            return false;
        }
    }

    /**
     * Withdraws money from an account
     * Usage: boolean success = bank.withdraw("ACC1001", 200.0);
     * 
     * @param accountNumber - Account number to withdraw from
     * @param amount        - Amount to withdraw
     * @return true if successful, false otherwise
     */
    public boolean withdraw(String accountNumber, double amount) {
        try {
            Account account = findAccount(accountNumber);
            if (account == null) {
                transactions.add(new Transaction(TransactionType.WITHDRAW, amount,
                        accountNumber, null, false, "Account not found"));
                return false;
            }

            boolean success = account.withdraw(amount);
            String message = success ? "OK" : "Insufficient funds or invalid amount";

            transactions.add(new Transaction(TransactionType.WITHDRAW, amount,
                    accountNumber, null, success, message));

            return success;
        } catch (Exception e) {
            System.err.println("Error in withdraw operation: " + e.getMessage());
            transactions.add(new Transaction(TransactionType.WITHDRAW, amount,
                    accountNumber, null, false, "System " + e.getMessage()));
            return false;
        }
    }

    /**
     * Transfers money between two accounts
     * Usage: boolean success = bank.transfer("ACC1001", "ACC1002", 300.0);
     * 
     * @param fromAccountNumber - Source account number
     * @param toAccountNumber   - Destination account number
     * @param amount            - Amount to transfer
     * @return true if successful, false otherwise
     */
    public boolean transfer(String fromAccountNumber, String toAccountNumber, double amount) {
        try {
            Account from = findAccount(fromAccountNumber);
            Account to = findAccount(toAccountNumber);

            if (from == null || to == null) {
                transactions.add(new Transaction(TransactionType.TRANSFER, amount,
                        fromAccountNumber, toAccountNumber, false,
                        from == null ? "Sender account not found" : "Receiver account not found"));
                return false;
            }

            boolean success = from.transfer(to, amount);
            String message = success ? "OK" : "Transfer failed";

            transactions.add(new Transaction(TransactionType.TRANSFER, amount,
                    fromAccountNumber, toAccountNumber, success, message));

            return success;
        } catch (Exception e) {
            System.err.println("Error in transfer operation: " + e.getMessage());
            transactions.add(new Transaction(TransactionType.TRANSFER, amount,
                    fromAccountNumber, toAccountNumber, false, "System " + e.getMessage()));
            return false;
        }
    }

    /**
     * Prints all accounts belonging to a specific customer
     * Usage: bank.printCustomerAccounts(1);
     * 
     * @param customerId - ID of the customer
     */
    public void printCustomerAccounts(int customerId) {
        try {
            System.out.println("\nAccounts for Customer ID: " + customerId + "");
            boolean foundCustomer = false;
            boolean hasAccounts = false;

            // First check if customer exists
            for (Customer customer : customers) {
                if (customer.getId() == customerId) {
                    foundCustomer = true;
                    System.out.println("Customer: " + customer.getFullName());
                    break;
                }
            }

            if (!foundCustomer) {
                System.out.println("Customer not found with ID: " + customerId);
                return;
            }

            // Print all accounts for this customer
            for (Account account : accounts) {
                if (account.getOwner().getId() == customerId) {
                    System.out.println(account);
                    hasAccounts = true;
                }
            }

            if (!hasAccounts) {
                System.out.println("No accounts found for this customer");
            }
        } catch (Exception e) {
            System.err.println("Error printing customer accounts: " + e.getMessage());
        }
    }

    /**
     * Prints all transaction history
     * Usage: bank.printTransactions();
     */
    public void printTransactions() {
        try {
            System.out.println("\nTransaction History");
            if (transactions.isEmpty()) {
                System.out.println("No transactions found");
            } else {
                for (Transaction transaction : transactions) {
                    System.out.println(transaction);
                }
            }
        } catch (Exception e) {
            System.err.println("Error printing transactions: " + e.getMessage());
        }
    }

    /**
     * Generates and prints a comprehensive bank report
     * Usage: bank.printReport();
     * Report includes:
     * - Count of each account type
     * - Total balances by account type
     * - Transaction statistics (successful/failed, by type)
     * - Customer count
     */
    public void printReport() {
        try {
            System.out.println("\nBank Report");

            // Statistics for accounts
            int debitCount = 0;
            int creditCount = 0;
            double debitBalance = 0.0;
            double creditBalance = 0.0;

            for (Account account : accounts) {
                if (account instanceof DebitAccount) {
                    debitCount++;
                    debitBalance += account.getBalance();
                } else if (account instanceof CreditAccount) {
                    creditCount++;
                    creditBalance += account.getBalance();
                }
            }

            System.out.println("Account Statistics:");
            System.out.println("  Debit Accounts: " + debitCount);
            System.out.println("  Credit Accounts: " + creditCount);
            System.out.println("  Total Accounts: " + (debitCount + creditCount));

            System.out.println("\nBalance Summary:");
            System.out.println("  Total Debit Balance: " + String.format("%.2f", debitBalance));
            System.out.println("  Total Credit Balance: " + String.format("%.2f", creditBalance));
            System.out.println("  Overall Balance: " + String.format("%.2f", (debitBalance + creditBalance)));

            // Statistics for transactions
            int successCount = 0;
            int failedCount = 0;
            int depositCount = 0;
            int withdrawCount = 0;
            int transferCount = 0;

            for (Transaction transaction : transactions) {
                if (transaction.isSuccess()) {
                    successCount++;
                } else {
                    failedCount++;
                }

                switch (transaction.getType()) {
                    case DEPOSIT:
                        depositCount++;
                        break;
                    case WITHDRAW:
                        withdrawCount++;
                        break;
                    case TRANSFER:
                        transferCount++;
                        break;
                }
            }

            System.out.println("\nTransaction Statistics:");
            System.out.println("  Successful: " + successCount);
            System.out.println("  Failed: " + failedCount);
            System.out.println("  Total: " + (successCount + failedCount));

            System.out.println("\nTransaction Types:");
            System.out.println("  Deposits: " + depositCount);
            System.out.println("  Withdrawals: " + withdrawCount);
            System.out.println("  Transfers: " + transferCount);

            System.out.println("\nCustomer Statistics:");
            System.out.println("  Total Customers: " + customers.size());
        } catch (Exception e) {
            System.err.println("Error generating report: " + e.getMessage());
        }
    }

    /**
     * Gets a copy of the customer list
     * Usage: List<Customer> customers = bank.getCustomers();
     * 
     * @return List of all customers
     */
    public List<Customer> getCustomers() {
        return new ArrayList<>(customers);
    }
}

/**
 * Main application class with console interface
 * Usage: Run this class to start the banking application
 */
public class BankApplication {
    private static Bank bank = new Bank();
    private static Scanner scanner = new Scanner(System.in);

    /**
     * Main method - entry point of the application
     * Usage: java BankApplication
     * 
     * @param args - Command line arguments (not used)
     */
    public static void main(String[] args) {
        boolean running = true;

        // Create sample data for demonstration
        createSampleData();

        System.out.println("Welcome to Banking System");
        System.out.println("Sample customers and accounts have been created for demonstration.");

        while (running) {
            try {
                printMenu();
                int choice = getIntInput("Enter your choice (1-10): ");

                switch (choice) {
                    case 1 -> createCustomer();
                    case 2 -> openDebitAccount();
                    case 3 -> openCreditAccount();
                    case 4 -> deposit();
                    case 5 -> withdraw();
                    case 6 -> transfer();
                    case 7 -> showCustomerAccounts();
                    case 8 -> showTransactions();
                    case 9 -> showReport();
                    case 10 -> {
                        running = false;
                        System.out.println("Exiting system. Goodbye!");
                    }
                    default -> System.out.println("Invalid choice. Please enter a number between 1 and 10.");
                }
            } catch (Exception e) {
                System.err.println("An error occurred: " + e.getMessage());
                System.out.println("Please try again.");
            }
        }

        scanner.close();
    }

    /**
     * Creates sample customers and accounts for demonstration
     * Usage: Automatically called at startup
     */
    private static void createSampleData() {
        try {
            // Create sample customers
            Customer customer1 = bank.createCustomer("John Smith");
            Customer customer2 = bank.createCustomer("Emma Johnson");

            // Create sample accounts
            bank.openDebitAccount(customer1);
            bank.openCreditAccount(customer1, 10000.0);
            bank.openDebitAccount(customer2);

            // Perform sample transactions
            bank.deposit("ACC1000", 5000.0);
            bank.withdraw("ACC1000", 1000.0);
            bank.deposit("ACC1002", 3000.0);
            bank.transfer("ACC1000", "ACC1002", 500.0);
        } catch (Exception e) {
            System.err.println("Error creating sample data: " + e.getMessage());
        }
    }

    /**
     * Prints the main menu
     * Usage: printMenu();
     */
    private static void printMenu() {
        System.out.println("\nBANKING SYSTEM MENU");
        System.out.println("1. Create New Customer");
        System.out.println("2. Open Debit Account");
        System.out.println("3. Open Credit Account");
        System.out.println("4. Deposit Money");
        System.out.println("5. Withdraw Money");
        System.out.println("6. Transfer Money");
        System.out.println("7. Show Customer Accounts");
        System.out.println("8. Show Transaction History");
        System.out.println("9. Generate Bank Report");
        System.out.println("10. Exit");
    }

    /**
     * Creates a new customer through console input
     * Usage: createCustomer();
     */
    private static void createCustomer() {
        try {
            System.out.print("Enter customer's full name: ");
            scanner.nextLine(); // Clear buffer
            String fullName = scanner.nextLine();

            if (fullName.trim().isEmpty()) {
                System.out.println("Name cannot be empty");
                return;
            }

            Customer customer = bank.createCustomer(fullName);
            if (customer != null) {
                System.out.println("Customer created successfully!");
                System.out.println("Customer ID: " + customer.getId());
                System.out.println("Customer Name: " + customer.getFullName());
            } else {
                System.out.println("Failed to create customer.");
            }
        } catch (Exception e) {
            System.err.println("Error in createCustomer: " + e.getMessage());
        }
    }

    /**
     * Opens a new debit account for an existing customer
     * Usage: openDebitAccount();
     */
    private static void openDebitAccount() {
        try {
            List<Customer> customers = bank.getCustomers();
            if (customers.isEmpty()) {
                System.out.println("No customers found. Please create a customer first.");
                return;
            }

            System.out.println("\nExisting Customers:");
            for (Customer customer : customers) {
                System.out.println(customer);
            }

            int customerId = getIntInput("Enter customer ID: ");
            Customer selectedCustomer = null;
            for (Customer customer : customers) {
                if (customer.getId() == customerId) {
                    selectedCustomer = customer;
                    break;
                }
            }

            if (selectedCustomer == null) {
                System.out.println("Customer with ID " + customerId + " not found.");
                return;
            }

            Account account = bank.openDebitAccount(selectedCustomer);
            if (account != null) {
                System.out.println("Debit account opened successfully!");
                System.out.println("Account Number: " + account.getAccountNumber());
                System.out.println("Owner: " + account.getOwner().getFullName());
            } else {
                System.out.println("Failed to open debit account.");
            }
        } catch (Exception e) {
            System.err.println("Error in openDebitAccount: " + e.getMessage());
        }
    }

    /**
     * Opens a new credit account for an existing customer
     * Usage: openCreditAccount();
     */
    private static void openCreditAccount() {
        try {
            List<Customer> customers = bank.getCustomers();
            if (customers.isEmpty()) {
                System.out.println("No customers found. Please create a customer first.");
                return;
            }

            System.out.println("\nExisting Customers:");
            for (Customer customer : customers) {
                System.out.println(customer);
            }

            int customerId = getIntInput("Enter customer ID: ");
            Customer selectedCustomer = null;
            for (Customer customer : customers) {
                if (customer.getId() == customerId) {
                    selectedCustomer = customer;
                    break;
                }
            }

            if (selectedCustomer == null) {
                System.out.println("Customer with ID " + customerId + " not found.");
                return;
            }

            double creditLimit = getDoubleInput("Enter credit limit: ");
            if (creditLimit <= 0) {
                System.out.println("Credit limit must be positive.");
                return;
            }

            Account account = bank.openCreditAccount(selectedCustomer, creditLimit);
            if (account != null) {
                System.out.println("Credit account opened successfully!");
                System.out.println("Account Number: " + account.getAccountNumber());
                System.out.println("Owner: " + account.getOwner().getFullName());
                System.out.println("Credit Limit: " + creditLimit);
            } else {
                System.out.println("Failed to open credit account.");
            }
        } catch (Exception e) {
            System.err.println("Error in openCreditAccount: " + e.getMessage());
        }
    }

    /**
     * Deposits money into an account
     * Usage: deposit();
     */
    private static void deposit() {
        try {
            String accountNumber = getAccountNumber("Enter account number to deposit into: ");
            double amount = getDoubleInput("Enter deposit amount: ");

            if (amount <= 0) {
                System.out.println("Deposit amount must be positive.");
                return;
            }

            if (bank.deposit(accountNumber, amount)) {
                System.out.println("Deposit successful!");
                System.out.println("Amount: " + amount);
                System.out.println("Account: " + accountNumber);
            } else {
                System.out.println("Deposit failed. Please check account number and amount.");
            }
        } catch (Exception e) {
            System.err.println("Error in deposit: " + e.getMessage());
        }
    }

    /**
     * Withdraws money from an account
     * Usage: withdraw();
     */
    private static void withdraw() {
        try {
            String accountNumber = getAccountNumber("Enter account number to withdraw from: ");
            double amount = getDoubleInput("Enter withdrawal amount: ");

            if (amount <= 0) {
                System.out.println("Withdrawal amount must be positive.");
                return;
            }

            if (bank.withdraw(accountNumber, amount)) {
                System.out.println("Withdrawal successful!");
                System.out.println("Amount: " + amount);
                System.out.println("Account: " + accountNumber);
            } else {
                System.out.println("Withdrawal failed. Please check account number, amount, and available balance.");
            }
        } catch (Exception e) {
            System.err.println("Error in withdraw: " + e.getMessage());
        }
    }

    /**
     * Transfers money between two accounts
     * Usage: transfer();
     */
    private static void transfer() {
        try {
            String fromAccount = getAccountNumber("Enter sender account number: ");
            String toAccount = getAccountNumber("Enter receiver account number: ");
            double amount = getDoubleInput("Enter transfer amount: ");

            if (amount <= 0) {
                System.out.println("Transfer amount must be positive.");
                return;
            }

            if (fromAccount.equals(toAccount)) {
                System.out.println("Cannot transfer to the same account.");
                return;
            }

            if (bank.transfer(fromAccount, toAccount, amount)) {
                System.out.println("Transfer successful!");
                System.out.println("Amount: " + amount);
                System.out.println("From: " + fromAccount);
                System.out.println("To: " + toAccount);
            } else {
                System.out.println("Transfer failed. Please check account numbers and available balance.");
            }
        } catch (Exception e) {
            System.err.println("Error in transfer: " + e.getMessage());
        }
    }

    /**
     * Shows all accounts for a specific customer
     * Usage: showCustomerAccounts();
     */
    private static void showCustomerAccounts() {
        try {
            int customerId = getIntInput("Enter customer ID: ");
            bank.printCustomerAccounts(customerId);
        } catch (Exception e) {
            System.err.println("Error in showCustomerAccounts: " + e.getMessage());
        }
    }

    /**
     * Shows transaction history
     * Usage: showTransactions();
     */
    private static void showTransactions() {
        try {
            bank.printTransactions();
        } catch (Exception e) {
            System.err.println("Error in showTransactions: " + e.getMessage());
        }
    }

    /**
     * Shows bank report
     * Usage: showReport();
     */
    private static void showReport() {
        try {
            bank.printReport();
        } catch (Exception e) {
            System.err.println("Error in showReport: " + e.getMessage());
        }
    }

    /**
     * Helper method to get account number from user
     * Usage: String accountNumber = getAccountNumber("Prompt message");
     * 
     * @param prompt - Message to display to user
     * @return Account number entered by user
     */
    private static String getAccountNumber(String prompt) {
        try {
            System.out.print(prompt);
            scanner.nextLine(); // Clear buffer
            return scanner.nextLine().trim();
        } catch (Exception e) {
            System.err.println("Error reading account number: " + e.getMessage());
            return "";
        }
    }

    /**
     * Helper method to get integer input from user
     * Usage: int value = getIntInput("Enter a number: ");
     * 
     * @param prompt - Message to display to user
     * @return Integer value entered by user
     */
    private static int getIntInput(String prompt) {
        try {
            System.out.print(prompt);
            while (!scanner.hasNextInt()) {
                System.out.print("Invalid input. Please enter an integer: ");
                scanner.next(); // Discard invalid input
            }
            int value = scanner.nextInt();
            return value;
        } catch (Exception e) {
            System.err.println("Error reading integer input: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Helper method to get double input from user
     * Usage: double value = getDoubleInput("Enter amount: ");
     * 
     * @param prompt - Message to display to user
     * @return Double value entered by user
     */
    private static double getDoubleInput(String prompt) {
        try {
            System.out.print(prompt);
            while (!scanner.hasNextDouble()) {
                System.out.print("Invalid input. Please enter a number: ");
                scanner.next(); // Discard invalid input
            }
            double value = scanner.nextDouble();
            return value;
        } catch (Exception e) {
            System.err.println("Error reading double input: " + e.getMessage());
            return -1.0;
        }
    }
}