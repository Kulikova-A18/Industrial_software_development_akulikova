package com.library.ui;

import com.library.model.Book;
import com.library.model.Reader;
import com.library.service.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {
    private final Scanner scanner;
    private final BookService bookService;
    private final ReaderService readerService;
    private final LoanService loanService;
    private final StatisticsService statisticsService;

    public ConsoleUI() {
        this.scanner = new Scanner(System.in);
        this.bookService = new BookService();
        this.readerService = new ReaderService();
        this.loanService = new LoanService();
        this.statisticsService = new StatisticsService();
    }

    public void start() {
        while (true) {
            printMainMenu();
            String choice = scanner.nextLine();

            try {
                switch (choice) {
                    case "1":
                        bookMenu();
                        break;
                    case "2":
                        readerMenu();
                        break;
                    case "3":
                        loanMenu();
                        break;
                    case "4":
                        statisticsMenu();
                        break;
                    case "0":
                        System.out.println("До свидания!");
                        return;
                    default:
                        System.out.println("Неверный выбор. Пожалуйста, попробуйте снова.");
                }
            } catch (SQLException e) {
                System.out.println("Ошибка базы данных: " + e.getMessage());
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Ошибка: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Неожиданная ошибка: " + e.getMessage());
            }
        }
    }

    private void printMainMenu() {
        System.out.println("\n=== БИБЛИОТЕЧНАЯ СИСТЕМА ===");
        System.out.println("1. Работа с книгами");
        System.out.println("2. Работа с читателями");
        System.out.println("3. Операции выдачи");
        System.out.println("4. Статистика");
        System.out.println("0. Выход");
        System.out.print("Выберите пункт меню: ");
    }

    private void bookMenu() throws SQLException {
        while (true) {
            System.out.println("\n=== РАБОТА С КНИГАМИ ===");
            System.out.println("1. Добавить книгу");
            System.out.println("2. Посмотреть список книг");
            System.out.println("3. Найти книгу по названию");
            System.out.println("0. Назад");
            System.out.print("Выберите пункт меню: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    addBook();
                    break;
                case "2":
                    bookService.listAllBooks();
                    break;
                case "3":
                    searchBook();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Неверный выбор.");
            }
        }
    }

    private void addBook() {
        try {
            System.out.println("\n=== ДОБАВЛЕНИЕ КНИГИ ===");

            System.out.print("Название: ");
            String title = scanner.nextLine();

            System.out.print("Автор: ");
            String author = scanner.nextLine();

            System.out.print("ISBN (необязательно): ");
            String isbn = scanner.nextLine();
            if (isbn.trim().isEmpty())
                isbn = null;

            System.out.print("Год издания (необязательно): ");
            String yearStr = scanner.nextLine();
            Integer year = yearStr.trim().isEmpty() ? null : Integer.parseInt(yearStr);

            System.out.print("Количество экземпляров: ");
            int quantity = Integer.parseInt(scanner.nextLine());

            Book book = new Book(title, author, isbn, year, quantity);
            bookService.addBook(book);

        } catch (NumberFormatException e) {
            System.out.println("Ошибка: введите корректное число");
        } catch (SQLException e) {
            System.out.println("Ошибка при добавлении книги: " + e.getMessage());
        }
    }

    private void searchBook() throws SQLException {
        System.out.print("\nВведите название для поиска: ");
        String title = scanner.nextLine();
        bookService.searchBooks(title);
    }

    private void readerMenu() throws SQLException {
        while (true) {
            System.out.println("\n=== РАБОТА С ЧИТАТЕЛЯМИ ===");
            System.out.println("1. Зарегистрировать читателя");
            System.out.println("2. Посмотреть список читателей");
            System.out.println("0. Назад");
            System.out.print("Выберите пункт меню: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    registerReader();
                    break;
                case "2":
                    readerService.listAllReaders();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Неверный выбор.");
            }
        }
    }

    private void registerReader() {
        try {
            System.out.println("\n=== РЕГИСТРАЦИЯ ЧИТАТЕЛЯ ===");

            System.out.print("Имя: ");
            String name = scanner.nextLine();

            System.out.print("Email: ");
            String email = scanner.nextLine();

            System.out.print("Телефон (необязательно): ");
            String phone = scanner.nextLine();
            if (phone.trim().isEmpty())
                phone = null;

            Reader reader = new Reader(name, email, phone);
            readerService.registerReader(reader);

        } catch (SQLException e) {
            System.out.println("Ошибка при регистрации: " + e.getMessage());
        }
    }

    private void loanMenu() throws SQLException {
        while (true) {
            System.out.println("\n=== ОПЕРАЦИИ ВЫДАЧИ ===");
            System.out.println("1. Выдать книгу читателю");
            System.out.println("2. Вернуть книгу");
            System.out.println("3. Посмотреть книги, выданные читателю");
            System.out.println("0. Назад");
            System.out.print("Выберите пункт меню: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    issueBook();
                    break;
                case "2":
                    returnBook();
                    break;
                case "3":
                    showReaderBooks();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Неверный выбор.");
            }
        }
    }

    private void issueBook() {
        try {
            System.out.println("\n=== ВЫДАЧА КНИГИ ===");

            System.out.print("ID книги: ");
            int bookId = Integer.parseInt(scanner.nextLine());

            System.out.print("ID читателя: ");
            int readerId = Integer.parseInt(scanner.nextLine());

            System.out.print("Срок выдачи (дней): ");
            int days = Integer.parseInt(scanner.nextLine());

            loanService.issueBook(bookId, readerId, days);

        } catch (NumberFormatException e) {
            System.out.println("Ошибка: введите корректное число");
        } catch (SQLException e) {
            System.out.println("Ошибка при выдаче книги: " + e.getMessage());
        }
    }

    private void returnBook() {
        try {
            System.out.println("\n=== ВОЗВРАТ КНИГИ ===");

            System.out.print("ID выдачи: ");
            int loanId = Integer.parseInt(scanner.nextLine());

            loanService.returnBook(loanId);

        } catch (NumberFormatException e) {
            System.out.println("Ошибка: введите корректное число");
        } catch (SQLException e) {
            System.out.println("Ошибка при возврате книги: " + e.getMessage());
        }
    }

    private void showReaderBooks() {
        try {
            System.out.print("\nВведите ID читателя: ");
            int readerId = Integer.parseInt(scanner.nextLine());

            loanService.showReaderBooks(readerId);

        } catch (NumberFormatException e) {
            System.out.println("Ошибка: введите корректное число");
        } catch (SQLException e) {
            System.out.println("Ошибка при получении списка: " + e.getMessage());
        }
    }

    private void statisticsMenu() throws SQLException {
        while (true) {
            System.out.println("\n=== СТАТИСТИКА ===");
            System.out.println("1. Показать популярные книги");
            System.out.println("2. Показать список выданных книг");
            System.out.println("0. Назад");
            System.out.print("Выберите пункт меню: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("Сколько книг показать? ");
                    int limit = Integer.parseInt(scanner.nextLine());
                    statisticsService.showPopularBooks(limit);
                    break;
                case "2":
                    loanService.showAllIssuedBooks();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Неверный выбор.");
            }
        }
    }
}