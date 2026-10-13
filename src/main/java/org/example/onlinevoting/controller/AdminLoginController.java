package org.example.onlinevoting.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.onlinevoting.dao.AdminDAO;
import org.example.onlinevoting.model.Admin;
import org.example.onlinevoting.util.DBConnection;
import org.example.onlinevoting.util.SceneNavigator;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminLoginController {

    private static final Logger LOGGER = Logger.getLogger(AdminLoginController.class.getName());
    private static final int MAX_FAILED_ATTEMPTS = AdminDAO.MAX_FAILED_ATTEMPTS;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        try (Connection connection = DBConnection.getConnection()) {
            AdminDAO adminDAO = new AdminDAO(connection);
            Admin admin = adminDAO.findByUsername(username);

            if (admin == null) {
                showError("Invalid username or password.");
                return;
            }

            if (admin.isLocked()) {
                showError("This account is locked. Contact a system administrator to reset it.");
                return;
            }

            boolean passwordMatches = BCrypt.checkpw(password, admin.getPasswordHash());

            if (passwordMatches) {
                adminDAO.resetFailedAttempts(admin.getAdminId());
                openDashboard(username, admin.getAdminId());
            } else {
                adminDAO.incrementFailedAttempts(admin.getAdminId());
                int failedAttemptsNow = admin.getFailedAttempts() + 1;

                if (failedAttemptsNow >= MAX_FAILED_ATTEMPTS) {
                    showError("Too many failed attempts. This account is now locked.");
                } else {
                    int remaining = MAX_FAILED_ATTEMPTS - failedAttemptsNow;
                    showError("Invalid username or password. " + remaining + " attempt(s) left.");
                }
            }

        } catch (SQLException e) {
            showError("A database error occurred. Please try again.");
            LOGGER.log(Level.SEVERE, "Login query failed for user: " + username, e);
        }
    }

    private void openDashboard(String username, int adminId) {
        try {
            AdminDashboardController dashboardController =
                    SceneNavigator.switchTo("/org/example/onlinevoting/fxml/admin_dashboard.fxml", "Admin Dashboard");
            dashboardController.setAdmin(adminId, username);
        } catch (IOException e) {
            showError("Could not load the dashboard. Please try again.");
            LOGGER.log(Level.SEVERE, "Failed to load admin_dashboard.fxml", e);
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}