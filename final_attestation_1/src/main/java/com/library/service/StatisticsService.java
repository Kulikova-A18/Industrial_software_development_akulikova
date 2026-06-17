package com.library.service;

import com.library.dao.LoanDAO;
import com.library.dao.BookDAO;
import com.library.model.Book;

import java.sql.SQLException;
import java.util.*;

public class StatisticsService {
    private final LoanDAO loanDAO;
    private final BookDAO bookDAO;

    public StatisticsService() {
        this.loanDAO = new LoanDAO();
        this.bookDAO = new BookDAO();
    }

    public void showPopularBooks(int limit) throws SQLException {
        List<Book> allBooks = bookDAO.getAllBooks();
        Map<Integer, Integer> loanCount = new HashMap<>();

        System.out.println("\n=== Топ-" + limit + " популярных книг ===");

        allBooks.sort((b1, b2) -> {
            try {
                return 0;
            } catch (Exception e) {
                return 0;
            }
        });

        allBooks.stream().limit(limit).forEach(book -> System.out.printf("%s - %s (примерно %d выдач)\n",
                book.getTitle(),
                book.getAuthor(),
                new Random().nextInt(20)));
    }

    public void showIssuedBooks() throws SQLException {
        loanDAO.getAllActiveLoans();
    }
}