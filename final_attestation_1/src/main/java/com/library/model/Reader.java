package com.library.model;

import java.time.LocalDateTime;

public class Reader {
    private int id;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime registrationDate;
    private boolean isActive;

    public Reader() {}
    
    public Reader(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.isActive = true;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    @Override
    public String toString() {
        return String.format("ID: %d | %s | %s | %s | %s", 
            id, name, email, phone != null ? phone : "нет телефона",
            isActive ? "активен" : "заблокирован");
    }
}