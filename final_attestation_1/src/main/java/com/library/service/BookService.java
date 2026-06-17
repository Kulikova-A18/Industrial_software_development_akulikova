package com.library.service;

import com.library.dao.BookDAO;
import com.library.model.Book;

import java.sql.SQLException;
import java.util.List;

public class BookService {
    private final BookDAO bookDAO;
    
    public BookService() {
        this.bookDAO = new BookDAO();
    }
    
    public void addBook(Book book) throws SQLException {
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Название книги не может быть пустым");
        }
        if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
            throw new IllegalArgumentException("Автор не может быть пустым");
        }
        if (book.getQuantity() < 0) {
            throw new IllegalArgumentException("Количество не может быть отрицательным");
        }
        bookDAO.addBook(book);
        System.out.println("Книга успешно добавлена с ID: " + book.getId());
    }
    
    public void listAllBooks() throws SQLException {
        List<Book> books = bookDAO.getAllBooks();
        if (books.isEmpty()) {
            System.out.println("В библиотеке нет книг");
        } else {
            System.out.println("\n=== Список всех книг ===");
            books.forEach(System.out::println);
        }
    }
    
    public void searchBooks(String title) throws SQLException {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Введите название для поиска");
        }
        
        List<Book> books = bookDAO.searchBooksByTitle(title);
        if (books.isEmpty()) {
            System.out.println("Книги с названием '" + title + "' не найдены");
        } else {
            System.out.println("\n=== Результаты поиска ===");
            books.forEach(System.out::println);
        }
    }
    
    public Book getBookById(int id) throws SQLException {
        Book book = bookDAO.getBookById(id);
        if (book == null) {
            throw new IllegalArgumentException("Книга с ID " + id + " не найдена");
        }
        return book;
    }
}