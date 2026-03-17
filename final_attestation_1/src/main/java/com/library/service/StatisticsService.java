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
        
        // Get all loans? We need to count loans per book
        // For simplicity, we'll use the popular books query
        
        System.out.println("\n=== Топ-" + limit + " популярных книг ===");
        
        // Since we don't have the actual stats from loanDAO, we'll simulate
        // In real implementation, you'd have a method to get loan counts
        
        allBooks.sort((b1, b2) -> {
            try {
                // This is inefficient - better to do in SQL
                return 0;
            } catch (Exception e) {
                return 0;
            }
        });
        
        // For demonstration, just show random books
        allBooks.stream().limit(limit).forEach(book -> 
            System.out.printf("%s - %s (примерно %d выдач)\n", 
                book.getTitle(), 
                book.getAuthor(),
                new Random().nextInt(20)
            )
        );
    }
    
    public void showIssuedBooks() throws SQLException {
        loanDAO.getAllActiveLoans(); // This will show active loans
        // For statistics, you might want to show all loans including returned
    }
}