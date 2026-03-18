package com.library.dao;

import com.library.config.DatabaseConfig;
import com.library.model.Loan;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LoanDAO {

    public void createLoan(Loan loan) throws SQLException {
        String sql = "INSERT INTO loans (book_id, reader_id, due_date, status) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, loan.getBookId());
            pstmt.setInt(2, loan.getReaderId());
            pstmt.setTimestamp(3, Timestamp.valueOf(loan.getDueDate()));
            pstmt.setString(4, loan.getStatus());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    loan.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void returnBook(int loanId) throws SQLException {
        String sql = "UPDATE loans SET return_date = ?, status = 'RETURNED' " +
                "WHERE id = ? AND return_date IS NULL";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(2, loanId);
            pstmt.executeUpdate();
        }
    }

    public List<Loan> getActiveLoansByReader(int readerId) throws SQLException {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT l.*, b.title as book_title, r.name as reader_name " +
                "FROM loans l " +
                "JOIN books b ON l.book_id = b.id " +
                "JOIN readers r ON l.reader_id = r.id " +
                "WHERE l.reader_id = ? AND l.return_date IS NULL " +
                "ORDER BY l.loan_date DESC";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, readerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    loans.add(mapRowToLoan(rs));
                }
            }
        }
        return loans;
    }

    public List<Loan> getAllActiveLoans() throws SQLException {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT l.*, b.title as book_title, r.name as reader_name " +
                "FROM loans l " +
                "JOIN books b ON l.book_id = b.id " +
                "JOIN readers r ON l.reader_id = r.id " +
                "WHERE l.return_date IS NULL " +
                "ORDER BY l.loan_date DESC";

        try (Connection conn = DatabaseConfig.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                loans.add(mapRowToLoan(rs));
            }
        }
        return loans;
    }

    public List<Loan> getPopularBooks(int limit) throws SQLException {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT b.id, b.title, b.author, COUNT(l.id) as loan_count " +
                "FROM books b " +
                "LEFT JOIN loans l ON b.id = l.book_id " +
                "GROUP BY b.id, b.title, b.author " +
                "ORDER BY loan_count DESC " +
                "LIMIT ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Loan loan = new Loan();
                    loan.setBookId(rs.getInt("id"));
                    loans.add(loan);
                }
            }
        }
        return loans;
    }

    private Loan mapRowToLoan(ResultSet rs) throws SQLException {
        Loan loan = new Loan();
        loan.setId(rs.getInt("id"));
        loan.setBookId(rs.getInt("book_id"));
        loan.setReaderId(rs.getInt("reader_id"));
        loan.setLoanDate(rs.getTimestamp("loan_date").toLocalDateTime());
        loan.setDueDate(rs.getTimestamp("due_date").toLocalDateTime());
        Timestamp returnDate = rs.getTimestamp("return_date");
        if (returnDate != null) {
            loan.setReturnDate(returnDate.toLocalDateTime());
        }
        loan.setStatus(rs.getString("status"));
        loan.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return loan;
    }
}