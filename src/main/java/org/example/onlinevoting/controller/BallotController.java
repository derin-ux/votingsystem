package org.example.onlinevoting.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.example.onlinevoting.model.Candidate;
import org.example.onlinevoting.model.Election;
import org.example.onlinevoting.util.DBConnection;
import org.example.onlinevoting.util.ElectionService;
import org.example.onlinevoting.util.SceneNavigator;
import org.example.onlinevoting.util.SessionManager;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class BallotController {

    @FXML private VBox positionsBox;
    @FXML private Label progressLabel;
    @FXML private Label errorLabel;
    @FXML private Label electionTitleLabel;
    @FXML private Button submitButton;

    private final Map<Integer, ToggleGroup> groupsByPosition = new HashMap<>();

    private final ElectionService electionService = new ElectionService();

    private int voterId;          // set via initData() when this screen loads
    private int electionId;       // the currently active election

    // positionId -> ToggleGroup, so only one candidate can be picked per position
//    private final Map<Integer, ToggleGroup> positionGroups = new HashMap<>();
    // RadioButton -> candidateId, to read back the selection on submit
//    private final Map<RadioButton, Integer> candidateButtons = new HashMap<>();


    public void initData(int voterId) {
        this.voterId = voterId;

        if (!isElectionActive()) {
            showAlert(Alert.AlertType.ERROR, "No Active Election",
                    "There is no election open for voting right now.");
            submitButton.setDisable(true);
            return;
        }

        if (hasAlreadyVoted()) {
            showAlert(Alert.AlertType.INFORMATION, "Already Voted",
                    "Our records show you've already voted in this election.");
            submitButton.setDisable(true);
            return;
        }

        loadBallot();
    }

    /** Goes through the shared ElectionService so "is an election active" has one source of truth. */
    private boolean isElectionActive() {
        try {
            Election active = electionService.getActiveElection();
            if (active != null) {
                this.electionId = active.getElectionId();
                electionTitleLabel.setText(active.getTitle());
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not check election status.");
            return false;
        }
    }

    private boolean hasAlreadyVoted() {
        String sql = "SELECT has_voted FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, voterId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getBoolean("has_voted");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not verify voting status.");
            return true; // fail safe: block voting if we can't confirm status
        }
    }

    /** Loads every candidate grouped by position and builds the ballot dynamically. */
    private void loadBallot() {
        String sql = "SELECT c.candidate_id, c.first_name, c.last_name, p.position_id, p.title AS position_title " +
                "FROM candidates c " +
                "JOIN positions p ON c.position_id = p.position_id " +
                "ORDER BY p.position_id, c.last_name";

        Map<Integer, String> positionTitles = new LinkedHashMap<>();
        Map<Integer, List<Candidate>> byPosition = new LinkedHashMap<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int posId = rs.getInt("position_id");
                positionTitles.putIfAbsent(posId, rs.getString("position_title"));
                byPosition.computeIfAbsent(posId, k -> new ArrayList<>())
                        .add(new Candidate(
                                rs.getInt("candidate_id"),
                                rs.getString("first_name"),
                                rs.getString("last_name")
                        ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not load candidates.");
            return;
        }

        for (Map.Entry<Integer, String> entry : positionTitles.entrySet()) {
            int posId = entry.getKey();
            positionsBox.getChildren().add(
                    buildPositionSection(posId, entry.getValue(), byPosition.get(posId)));
        }
        updateProgress();
    }


    private VBox buildPositionSection(int positionId, String positionTitle, List<Candidate> candidates) {
        Label title = new Label(positionTitle);
        title.getStyleClass().add("position-title");

        Label hint = new Label("Choose one candidate.");
        hint.getStyleClass().add("subtitle");

        ToggleGroup group = new ToggleGroup();
        groupsByPosition.put(positionId, group);


        FlowPane cards = new FlowPane(10, 10);
        for (Candidate c : candidates) {
            String initials = c.getFirstName().substring(0, 1) + c.getLastName().substring(0, 1);

            Label avatar = new Label(initials.toUpperCase());
            avatar.getStyleClass().add("candidate-avatar");

            Label name = new Label(c.getFirstName() + " " + c.getLastName());
            name.getStyleClass().add("candidate-name");

            VBox content = new VBox(8, avatar, name);
            content.setAlignment(Pos.CENTER);

            ToggleButton card = new ToggleButton();
            card.setGraphic(content);
            card.getStyleClass().add("candidate-card");
            card.setToggleGroup(group);
            card.setUserData(c.getCandidateId());
            card.setOnAction(e -> updateProgress());
            cards.getChildren().add(card);
        }

       return new VBox (4, title, hint, cards);

    }


    private void updateProgress() {
        int total = groupsByPosition.size();
        int selected = 0;

        for (ToggleGroup group : groupsByPosition.values()) {
            if (group.getSelectedToggle() != null) {
                selected++;
            }
        }

        progressLabel.setText(selected + " of " + total + " positions selected");
        errorLabel.setVisible(false);
    }



    @FXML
    private void handleSubmit() {
        // Re-check has_voted right before writing, to close the double-submit window.
        if (hasAlreadyVoted()) {
            showAlert(Alert.AlertType.WARNING, "Already Voted", "Your vote was already recorded.");
            submitButton.setDisable(true);
            return;
        }

        // Every position must have exactly one selection.
        Map<Integer, Integer> selections = new LinkedHashMap<>(); // positionId -> candidateId
        for (Map.Entry<Integer, ToggleGroup> entry : groupsByPosition.entrySet()) {
            Toggle selected = entry.getValue().getSelectedToggle();
            if (selected == null) {
                errorLabel.setText("Select a candidate for every position.");
                errorLabel.setVisible(true);
                return;
            }
            int candidateId = (Integer) selected.getUserData();
            selections.put(entry.getKey(), candidateId);
        }

        submitVotes(selections);
    }

    @FXML
    private void handleLogout() {
        SessionManager.clear();
        try {
            SceneNavigator.switchTo("/org/example/onlinevoting/fxml/voter_login.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not return to the login screen.");
        }
    }

    /** Writes all votes and flips has_voted in ONE transaction; any failure rolls everything back. */
    private void submitVotes(Map<Integer, Integer> selections) {
        String insertVoteSql =
                "INSERT INTO votes (user_id, candidate_id, position_id, election_id) VALUES (?, ?, ?, ?)";
        String markVotedSql = "UPDATE users SET has_voted = TRUE WHERE user_id = ?";

        Connection conn = null;
        boolean success = false;
        boolean alreadyVoted = false;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement insertStmt = conn.prepareStatement(insertVoteSql)) {
                for (Map.Entry<Integer, Integer> sel : selections.entrySet()) {
                    insertStmt.setInt(1, voterId);
                    insertStmt.setInt(2, sel.getValue()); // candidateId
                    insertStmt.setInt(3, sel.getKey());   // positionId
                    insertStmt.setInt(4, electionId);
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
            }

            try (PreparedStatement markStmt = conn.prepareStatement(markVotedSql)) {
                markStmt.setInt(1, voterId);
                if (markStmt.executeUpdate() == 0 ){
                    conn.rollback();
                    alreadyVoted = true;
                }
            }

            if (!alreadyVoted) {
                conn.commit();
                success = true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            showAlert(Alert.AlertType.ERROR, "Submission Failed",
                    "Something went wrong recording your vote. Nothing was saved — please try again.");
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }

        if (alreadyVoted) {
            submitButton.setDisable(true);
            showAlert(Alert.AlertType.INFORMATION, "Already Voted", "Your vote was already recorded.");
        } else if (success) {
            submitButton.setDisable(true);
            showAlert(Alert.AlertType.INFORMATION, "Vote recorded", "Thank you for participating in the election");
            handleLogout(); // clears session so new voter starts fresh
        }

    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}