package com.library.dao;

import com.library.config.DatabaseConfig;
import com.library.model.Reader;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReaderDAO {
    
    public void addReader(Reader reader) throws SQLException {
        String sql = "INSERT INTO readers (name, email, phone) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, reader.getName());
            pstmt.setString(2, reader.getEmail());
            pstmt.setString(3, reader.getPhone());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reader.setId(generatedKeys.getInt(1));
                }
            }
        }
    }
    
    public List<Reader> getAllReaders() throws SQLException {
        List<Reader> readers = new ArrayList<>();
        String sql = "SELECT * FROM readers ORDER BY name";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                readers.add(mapRowToReader(rs));
            }
        }
        return readers;
    }
    
    public Reader getReaderById(int id) throws SQLException {
        String sql = "SELECT * FROM readers WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToReader(rs);
                }
            }
        }
        return null;
    }
    
    public Reader getReaderByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM readers WHERE email = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToReader(rs);
                }
            }
        }
        return null;
    }
    
    private Reader mapRowToReader(ResultSet rs) throws SQLException {
        Reader reader = new Reader();
        reader.setId(rs.getInt("id"));
        reader.setName(rs.getString("name"));
        reader.setEmail(rs.getString("email"));
        reader.setPhone(rs.getString("phone"));
        reader.setRegistrationDate(rs.getTimestamp("registration_date").toLocalDateTime());
        reader.setActive(rs.getBoolean("is_active"));
        return reader;
    }
}