package com.library.service;

import com.library.dao.ReaderDAO;
import com.library.model.Reader;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

public class ReaderService {
    private final ReaderDAO readerDAO;
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    
    public ReaderService() {
        this.readerDAO = new ReaderDAO();
    }
    
    public void registerReader(Reader reader) throws SQLException {
        validateReader(reader);
        
        // Check if email already exists
        Reader existingReader = readerDAO.getReaderByEmail(reader.getEmail());
        if (existingReader != null) {
            throw new IllegalArgumentException("Читатель с email " + reader.getEmail() + " уже существует");
        }
        
        readerDAO.addReader(reader);
        System.out.println("Читатель успешно зарегистрирован с ID: " + reader.getId());
    }
    
    public void listAllReaders() throws SQLException {
        List<Reader> readers = readerDAO.getAllReaders();
        if (readers.isEmpty()) {
            System.out.println("Нет зарегистрированных читателей");
        } else {
            System.out.println("\n=== Список всех читателей ===");
            readers.forEach(System.out::println);
        }
    }
    
    public Reader getReaderById(int id) throws SQLException {
        Reader reader = readerDAO.getReaderById(id);
        if (reader == null) {
            throw new IllegalArgumentException("Читатель с ID " + id + " не найден");
        }
        return reader;
    }
    
    private void validateReader(Reader reader) {
        if (reader.getName() == null || reader.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Имя не может быть пустым");
        }
        if (reader.getEmail() == null || reader.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email не может быть пустым");
        }
        if (!EMAIL_PATTERN.matcher(reader.getEmail()).matches()) {
            throw new IllegalArgumentException("Неверный формат email");
        }
    }
}