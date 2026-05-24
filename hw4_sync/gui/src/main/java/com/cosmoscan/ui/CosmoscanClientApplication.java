package com.cosmoscan.ui;

import com.cosmoscan.ui.ui.MainFrame;
import com.formdev.flatlaf.FlatDarculaLaf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;
import java.awt.*;

@SpringBootApplication
@Slf4j
public class CosmoscanClientApplication {
    
    public static void main(String[] args) {
        // Установка Look & Feel
        try {
            FlatDarculaLaf.setup();
            
            // Настройка шрифтов
            Font defaultFont = new Font("Segoe UI", Font.PLAIN, 14);
            UIManager.put("defaultFont", defaultFont);
            UIManager.put("Label.font", defaultFont);
            UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 14));
            UIManager.put("TextField.font", defaultFont);
            UIManager.put("TextArea.font", defaultFont);
            UIManager.put("Table.font", defaultFont);
            UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 14));
            UIManager.put("TitledBorder.font", new Font("Segoe UI", Font.BOLD, 14));
            UIManager.put("TabbedPane.font", new Font("Segoe UI", Font.BOLD, 14));
            
        } catch (Exception e) {
            log.warn("Не удалось установить тему FlatLaf: {}", e.getMessage());
        }
        
        // Запуск Spring контекста
        ConfigurableApplicationContext context = new SpringApplicationBuilder(
                CosmoscanClientApplication.class)
                .headless(false)
                .run(args);
        
        // Запуск GUI
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame mainFrame = context.getBean(MainFrame.class);
                mainFrame.setVisible(true);
                log.info("CosmoScan UI успешно запущен");
            } catch (Exception e) {
                log.error("Ошибка при запуске UI: {}", e.getMessage(), e);
                JOptionPane.showMessageDialog(
                        null,
                        "Ошибка при запуске приложения:\n" + e.getMessage(),
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            }
        });
        
        // Хук для корректного завершения
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("CosmoScan UI завершает работу");
            context.close();
        }));
    }
}