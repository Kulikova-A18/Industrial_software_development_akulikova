package com.library.service;

import com.library.dao.LoanDAO;
import com.library.dao.BookDAO;
import com.library.model.Loan;
import com.library.model.Book;
import com.library.model.Reader;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class LoanService {
    private final LoanDAO loanDAO;
    private final BookDAO bookDAO;
    private final BookService bookService;
    private final ReaderService readerService;

    public LoanService() {
        this.loanDAO = new LoanDAO();
        this.bookDAO = new BookDAO();
        this.bookService = new BookService();
        this.readerService = new ReaderService();
    }

    public void issueBook(int bookId, int readerId, int loanDays) throws SQLException {
        Book book = bookService.getBookById(bookId);
        if (book.getQuantity() <= 0) {
            throw new IllegalStateException("Книга \"" + book.getTitle() + "\" недоступна для выдачи");
        }

        Reader reader = readerService.getReaderById(readerId);
        if (!reader.isActive()) {
            throw new IllegalStateException("Читатель \"" + reader.getName() + "\" заблокирован");
        }

        LocalDateTime dueDate = LocalDateTime.now().plusDays(loanDays);
        Loan loan = new Loan(bookId, readerId, dueDate);

        try {
            bookDAO.updateBookQuantity(bookId, -1);

            loanDAO.createLoan(loan);

            System.out.println("Книга \"" + book.getTitle() + "\" выдана читателю \"" +
                    reader.getName() + "\" до " + dueDate.toLocalDate());
        } catch (SQLException e) {
            throw new SQLException("Ошибка при выдаче книги: " + e.getMessage());
        }
    }

    public void returnBook(int loanId) throws SQLException {
        loanDAO.returnBook(loanId);

        System.out.println("Книга успешно возвращена");
    }

    public void showReaderBooks(int readerId) throws SQLException {
        Reader reader = readerService.getReaderById(readerId);
        List<Loan> activeLoans = loanDAO.getActiveLoansByReader(readerId);

        if (activeLoans.isEmpty()) {
            System.out.println("У читателя \"" + reader.getName() + "\" нет выданных книг");
        } else {
            System.out.println("\n=== Книги, выданные читателю \"" + reader.getName() + "\" ===");
            for (Loan loan : activeLoans) {
                Book book = bookService.getBookById(loan.getBookId());
                System.out.printf("ID выдачи: %d | Книга: %s | Дата выдачи: %s | Срок до: %s\n",
                        loan.getId(),
                        book.getTitle(),
                        loan.getLoanDate().toLocalDate(),
                        loan.getDueDate().toLocalDate());
            }
        }
    }

    public void showAllIssuedBooks() throws SQLException {
        List<Loan> activeLoans = loanDAO.getAllActiveLoans();

        if (activeLoans.isEmpty()) {
            System.out.println("Нет выданных книг");
        } else {
            System.out.println("\n=== Список всех выданных книг ===");
            for (Loan loan : activeLoans) {
                Book book = bookService.getBookById(loan.getBookId());
                Reader reader = readerService.getReaderById(loan.getReaderId());

                String status = loan.getDueDate().isBefore(LocalDateTime.now()) ? "ПРОСРОЧЕНА" : "Активна";

                System.out.printf("ID выдачи: %d | Книга: %s | Читатель: %s | Выдана: %s | Срок: %s | %s\n",
                        loan.getId(),
                        book.getTitle(),
                        reader.getName(),
                        loan.getLoanDate().toLocalDate(),
                        loan.getDueDate().toLocalDate(),
                        status);
            }
        }
    }
}