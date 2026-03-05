package app;

import ui.LoginFrame;
import config.AppConfig;
import db.DBConnection;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                // Initialize application configuration
                AppConfig.initialize();

                // Test database connection
                if (DBConnection.getInstance().testConnection()) {
                    System.out.println("Database connected successfully");
                } else {
                    System.err.println("Failed to connect to database");
                }

                // Launch login frame
                new LoginFrame().setVisible(true);

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Failed to start application: " + e.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}