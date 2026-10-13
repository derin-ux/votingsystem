package org.example.onlinevoting.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.onlinevoting.dao.VoterDAO;
import org.example.onlinevoting.model.Voter;
import org.example.onlinevoting.util.DBConnection;
import org.example.onlinevoting.util.PasswordUtil;
import org.example.onlinevoting.util.SceneNavigator;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class VoterRegistrationController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField ninField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    private static final String LOGIN_FXML = "/org/example/onlinevoting/fxml/voter_login.fxml";
    private static final int MIN_PASSWORD_LENGTH = 6;

    @FXML
    public void initialize() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }

    @FXML
    private void handleRegister() {
        errorLabel.setVisible(false); // clear any earlier message

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String nin = ninField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (firstName.isEmpty() || lastName.isEmpty() || nin.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill in every field.");
            return;
        }

        if (!nin.matches("\\d{11}")) {
            showError("NIN must be exactly 11 digits.");
            return;
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            showError("Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            return;
        }

        try (Connection connection = DBConnection.getConnection()) {
            VoterDAO voterDAO = new VoterDAO(connection);

            if (voterDAO.existsByNin(nin)) {
                showError("This NIN is already registered.");
                return;
            }

            Voter voter = new Voter();
            voter.setFirstName(firstName);
            voter.setLastName(lastName);
            voter.setNin(nin);
            voter.setPasswordHash(PasswordUtil.hash(password));

            if (!voterDAO.create(voter)) {
                showError("Registration failed. Please try again.");
                return;
            }
        } catch (SQLException e) {
            showError("Database error. Try again.");
            e.printStackTrace();
            return;
        }

        // Connection is closed by now; safe to show the pop-up and move on
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Registration complete");
        alert.setHeaderText(null);
        alert.setContentText("Account created. Log in with your new details.");
        alert.showAndWait();

        goToLogin();
    }

    @FXML
    private void handleBackToLogin() {
        goToLogin();
    }

    private void goToLogin() {
        try {
            SceneNavigator.switchTo(LOGIN_FXML, "Voting System — Vote");
        } catch (IOException e) {
            showError("Could not load the login screen.");
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}