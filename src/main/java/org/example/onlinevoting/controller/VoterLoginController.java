package org.example.onlinevoting.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.onlinevoting.dao.VoterDAO;
import org.example.onlinevoting.model.Voter;
import org.example.onlinevoting.util.DBConnection;
import org.example.onlinevoting.util.PasswordUtil;
import org.example.onlinevoting.util.SceneNavigator;
import org.example.onlinevoting.util.SessionManager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class VoterLoginController {

    @FXML private TextField ninField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;


    private static final String BALLOT_FXML = "/org/example/onlinevoting/fxml/ballot.fxml";
    private static final String REGISTER_FXML = "/org/example/onlinevoting/fxml/voter_registration.fxml";

    @FXML
    private void handleLogin() {
        errorLabel.setVisible(false); // clear any earlier message

        String nin = ninField.getText().trim();
        String password = passwordField.getText();

        if (nin.isEmpty() || password.isEmpty()) {
            showError("Enter both NIN and password");
            return;
        }

        Voter voter;
        try (Connection connection = DBConnection.getConnection()) {
            voter = new VoterDAO(connection).findByNin(nin);
        } catch (SQLException e) {
            showError("Database error. Try again.");
            e.printStackTrace();
            return;
        }

        // Same message for "no such NIN" and "wrong password" so nobody can
        // use this screen to find out which NINs are registered
        if (voter == null || !PasswordUtil.verify(password, voter.getPasswordHash())) {
            showError("Invalid NIN or password");
            return;
        }

        if (voter.isHasVoted()) {
            showError("You have already voted in this election.");
            return;
        }

        SessionManager.setLoggedInVoter(voter.getUserId());
        try {
            BallotController ballot = SceneNavigator.switchTo(BALLOT_FXML,"Voting System - Ballot");
            ballot.initData(voter.getUserId());
        } catch (IOException e) {
            showError("Could not load the ballot. Try again.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGoToRegister() {
        try {
            SceneNavigator.switchTo(REGISTER_FXML);
        } catch (IOException e) {
            showError("Could not open the registration page.");
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}