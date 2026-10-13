package org.example.onlinevoting.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.example.onlinevoting.dao.VoteDAO;
import org.example.onlinevoting.model.Candidate;
import org.example.onlinevoting.model.Election;
import org.example.onlinevoting.model.Position;
import org.example.onlinevoting.util.DBConnection;
import org.example.onlinevoting.util.SceneNavigator;
import org.example.onlinevoting.util.SessionManager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminDashboardController {

    private static final Logger LOGGER = Logger.getLogger(AdminDashboardController.class.getName());

    private final CandidateController candidateController = new CandidateController();
    private final PositionController positionController = new PositionController();
    private final ElectionController electionController = new ElectionController();

    private final Map<Integer, String> positionTitles = new HashMap<>();

    private int adminId;
    private String username;

    // ---------- Header ----------
    @FXML private Label adminNameLabel;

    // ---------- Candidates tab ----------
    @FXML private TextField candFirstNameField, candLastNameField, candNinField,
            candPhoneField, candEmailField;
    @FXML private ComboBox<Position> candPositionCombo;
    @FXML private Label candidateMessageLabel;
    @FXML private TableView<Candidate> candidatesTable;
    @FXML private TableColumn<Candidate, String> candFirstNameCol, candLastNameCol,
            candNinCol, candPhoneCol, candEmailCol, candPositionCol;

    // ---------- Positions tab ----------
    @FXML private TextField positionTitleField;
    @FXML private Label positionMessageLabel;
    @FXML private TableView<Position> positionsTable;
    @FXML private TableColumn<Position, String> positionTitleCol;

    // ---------- Elections tab ----------
    @FXML private TextField electionFormTitleField, electionTimeField;
    @FXML private TextArea electionDescriptionArea;
    @FXML private DatePicker electionDatePicker;
    @FXML private Label electionMessageLabel;
    @FXML private TableView<Election> electionsTable;
    @FXML private TableColumn<Election, String> electionTitleCol, electionDateCol,
            electionTimeCol, electionStatusCol;

    // ---------- Results tab ----------
    @FXML private Label resultsElectionLabel;
    @FXML private VBox resultsBox;

    /** Runs automatically after the FXML loads. */
    @FXML
    private void initialize() {
        // Tell each table column which value to show
        candFirstNameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFirstName()));
        candLastNameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLastName()));
        candNinCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNin()));
        candPhoneCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPhone()));
        candEmailCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        candPositionCol.setCellValueFactory(d -> new SimpleStringProperty(
                positionTitles.getOrDefault(d.getValue().getPositionId(), "")));

        positionTitleCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));

        electionTitleCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));
        electionDateCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getElectionDate())));
        electionTimeCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getStartTime())));
        electionStatusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));

        // Show the position title (not the object) in the combo box
        candPositionCombo.setConverter(new StringConverter<>() {
            @Override public String toString(Position p) { return p == null ? "" : p.getTitle(); }
            @Override public Position fromString(String s) { return null; }
        });

        // Clicking a row fills its form so Update/Delete know which record you mean
        candidatesTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, c) -> { if (c != null) fillCandidateForm(c); });
        positionsTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, p) -> { if (p != null) positionTitleField.setText(p.getTitle()); });
        electionsTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, e) -> { if (e != null) fillElectionForm(e); });

        refreshPositions();
        refreshCandidates();
        refreshElections();
        refreshResults();
    }

    /** Called by AdminLoginController right after loading this screen. */
    public void setAdmin(int adminId, String username) {
        this.adminId = adminId;
        this.username = username;
        if (adminNameLabel != null) adminNameLabel.setText("Signed in as " + username);
    }

    // =====================================================
    //  Logout
    // =====================================================
    @FXML
    private void handleLogout() {
        SessionManager.clear();
        try {
            SceneNavigator.switchTo("/org/example/onlinevoting/fxml/admin_login.fxml", "Voting System — Admin");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not return to admin login", e);
        }
    }

    // =====================================================
    //  Candidates
    // =====================================================
    @FXML
    private void handleRegisterCandidate() {
        String error = validateCandidateForm();
        if (error != null) { showMessage(candidateMessageLabel, error, true); return; }

        boolean ok = perform(candidateMessageLabel, "Candidate registered.", "Candidate could not be registered.",
                () -> candidateController.registerCandidate(
                        candNinField.getText().trim(), candFirstNameField.getText().trim(),
                        candLastNameField.getText().trim(), candPhoneField.getText().trim(),
                        candEmailField.getText().trim(), candPositionCombo.getValue().getPositionId()));
        if (ok) { clearCandidateForm(); refreshCandidates(); }
    }

    @FXML
    private void handleUpdateCandidate() {
        Candidate selected = candidatesTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showMessage(candidateMessageLabel, "Select a candidate in the table first.", true); return; }
        String error = validateCandidateForm();
        if (error != null) { showMessage(candidateMessageLabel, error, true); return; }

        boolean ok = perform(candidateMessageLabel, "Candidate updated.", "Candidate could not be updated.",
                () -> candidateController.editCandidate(
                        selected.getCandidateId(), candNinField.getText().trim(),
                        candFirstNameField.getText().trim(), candLastNameField.getText().trim(),
                        candPhoneField.getText().trim(), candEmailField.getText().trim(),
                        candPositionCombo.getValue().getPositionId()));
        if (ok) { clearCandidateForm(); refreshCandidates(); }
    }

    @FXML
    private void handleDeleteCandidate() {
        Candidate selected = candidatesTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showMessage(candidateMessageLabel, "Select a candidate in the table first.", true); return; }
        if (!confirm("Delete " + selected.getFirstName() + " " + selected.getLastName() + "?")) return;

        boolean ok = perform(candidateMessageLabel, "Candidate deleted.", "Candidate could not be deleted.",
                () -> candidateController.deleteCandidate(selected.getCandidateId()));
        if (ok) { clearCandidateForm(); refreshCandidates(); }
    }

    @FXML
    private void handleClearCandidateForm() {
        clearCandidateForm();
        candidateMessageLabel.setVisible(false);
    }

    // =====================================================
    //  Positions
    // =====================================================
    @FXML
    private void handleAddPosition() {
        String title = positionTitleField.getText().trim();
        if (title.isEmpty()) { showMessage(positionMessageLabel, "Enter a position title.", true); return; }

        boolean ok = perform(positionMessageLabel, "Position added.", "Position could not be added.",
                () -> positionController.addPosition(title));
        if (ok) { positionTitleField.clear(); refreshPositions(); refreshCandidates(); }
    }

    @FXML
    private void handleUpdatePosition() {
        Position selected = positionsTable.getSelectionModel().getSelectedItem();
        String title = positionTitleField.getText().trim();
        if (selected == null) { showMessage(positionMessageLabel, "Select a position in the table first.", true); return; }
        if (title.isEmpty()) { showMessage(positionMessageLabel, "Enter a position title.", true); return; }

        boolean ok = perform(positionMessageLabel, "Position updated.", "Position could not be updated.",
                () -> positionController.editPosition(selected.getPositionId(), title));
        if (ok) { positionTitleField.clear(); refreshPositions(); refreshCandidates(); }
    }

    @FXML
    private void handleDeletePosition() {
        Position selected = positionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showMessage(positionMessageLabel, "Select a position in the table first.", true); return; }
        if (!confirm("Delete the position \"" + selected.getTitle() + "\"?")) return;

        boolean ok = perform(positionMessageLabel, "Position deleted.", "Position could not be deleted.",
                () -> positionController.deletePosition(selected.getPositionId()));
        if (ok) { positionTitleField.clear(); refreshPositions(); }
    }

    // =====================================================
    //  Elections
    // =====================================================
    @FXML
    private void handleCreateElection() {
        try {
            String title = requireText(electionFormTitleField.getText(), "Enter an election title.");
            LocalDate date = requireDate();
            Time time = parseTime(electionTimeField.getText());
            String description = electionDescriptionArea.getText().trim();

            boolean ok = perform(electionMessageLabel, "Election created.", "Election could not be created.",
                    () -> electionController.createElection(title, description, date, time));
            if (ok) { clearElectionForm(); refreshElections(); }
        } catch (IllegalArgumentException e) {
            showMessage(electionMessageLabel, e.getMessage(), true);
        }
    }

    @FXML
    private void handleUpdateElection() {
        Election selected = electionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showMessage(electionMessageLabel, "Select an election in the table first.", true); return; }
        try {
            String title = requireText(electionFormTitleField.getText(), "Enter an election title.");
            LocalDate date = requireDate();
            Time time = parseTime(electionTimeField.getText());
            String description = electionDescriptionArea.getText().trim();

            boolean ok = perform(electionMessageLabel, "Election updated.",
                    "Only elections that haven't started yet can be edited.",
                    () -> electionController.editElection(selected.getElectionId(), title, description, date, time));
            if (ok) { clearElectionForm(); refreshElections(); }
        } catch (IllegalArgumentException e) {
            showMessage(electionMessageLabel, e.getMessage(), true);
        }
    }

    @FXML
    private void handleStartElection() {
        Election selected = electionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showMessage(electionMessageLabel, "Select an election in the table first.", true); return; }
        if (!confirm("Start \"" + selected.getTitle() + "\" now? Voting opens immediately and every voter can vote again.")) return;

        boolean ok = perform(electionMessageLabel, "Election started.",
                "Only a scheduled election can be started.",
                () -> electionController.startElection(selected.getElectionId()));
        if (ok) { refreshElections(); refreshResults(); }
    }

    @FXML
    private void handleEndElection() {
        Election selected = electionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showMessage(electionMessageLabel, "Select an election in the table first.", true); return; }
        if (!confirm("End \"" + selected.getTitle() + "\"? Voting closes immediately.")) return;

        boolean ok = perform(electionMessageLabel, "Election ended.",
                "Only an active election can be ended.",
                () -> electionController.endElection(selected.getElectionId()));
        if (ok) { refreshElections(); refreshResults(); }
    }

    // =====================================================
    //  Results
    // =====================================================
    @FXML
    private void handleRefreshResults() {
        refreshResults();
    }

    private void refreshResults() {
        resultsBox.getChildren().clear();
        try {
            Election election = latestStartedElection();
            if (election == null) {
                resultsElectionLabel.setText("No election results yet");
                return;
            }
            resultsElectionLabel.setText(election.getTitle() + " (" + election.getStatus() + ")");

            Map<Integer, Integer> tally;
            try (Connection conn = DBConnection.getConnection()) {
                tally = new VoteDAO(conn).tallyByElection(election.getElectionId());
            }

            List<Candidate> candidates = candidateController.getAllCandidates();
            for (Position position : positionController.getAllPositions()) {
                VBox section = new VBox(6);
                Label heading = new Label(position.getTitle());
                heading.getStyleClass().add("panel-title");
                section.getChildren().add(heading);

                for (Candidate c : candidates) {
                    if (c.getPositionId() != position.getPositionId()) continue;
                    int votes = tally.getOrDefault(c.getCandidateId(), 0);
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    section.getChildren().add(new HBox(8,
                            new Label(c.getFirstName() + " " + c.getLastName()),
                            spacer,
                            new Label(votes + (votes == 1 ? " vote" : " votes"))));
                }
                resultsBox.getChildren().add(section);
            }
        } catch (SQLException e) {
            resultsElectionLabel.setText("Could not load results");
            LOGGER.log(Level.SEVERE, "refreshResults failed", e);
        }
    }

    // The list comes back newest first, so the first one that isn't "scheduled" is the latest
    private Election latestStartedElection() throws SQLException {
        for (Election e : electionController.getAllElections()) {
            if (!"scheduled".equals(e.getStatus())) return e;
        }
        return null;
    }

    // =====================================================
    //  Refresh helpers
    // =====================================================
    private void refreshPositions() {
        try {
            List<Position> positions = positionController.getAllPositions();
            positionTitles.clear();
            for (Position p : positions) positionTitles.put(p.getPositionId(), p.getTitle());
            positionsTable.getItems().setAll(positions);
            candPositionCombo.getItems().setAll(positions);
        } catch (SQLException e) {
            showMessage(positionMessageLabel, "Could not load positions.", true);
            LOGGER.log(Level.SEVERE, "refreshPositions failed", e);
        }
    }

    private void refreshCandidates() {
        try {
            candidatesTable.getItems().setAll(candidateController.getAllCandidates());
            candidatesTable.refresh(); // so renamed position titles show up
        } catch (SQLException e) {
            showMessage(candidateMessageLabel, "Could not load candidates.", true);
            LOGGER.log(Level.SEVERE, "refreshCandidates failed", e);
        }
    }

    private void refreshElections() {
        try {
            electionsTable.getItems().setAll(electionController.getAllElections());
        } catch (SQLException e) {
            showMessage(electionMessageLabel, "Could not load elections.", true);
            LOGGER.log(Level.SEVERE, "refreshElections failed", e);
        }
    }

    // =====================================================
    //  Form helpers
    // =====================================================
    private void fillCandidateForm(Candidate c) {
        candFirstNameField.setText(c.getFirstName());
        candLastNameField.setText(c.getLastName());
        candNinField.setText(c.getNin());
        candPhoneField.setText(c.getPhone());
        candEmailField.setText(c.getEmail());
        for (Position p : candPositionCombo.getItems()) {
            if (p.getPositionId() == c.getPositionId()) { candPositionCombo.setValue(p); break; }
        }
    }

    private void fillElectionForm(Election e) {
        electionFormTitleField.setText(e.getTitle());
        electionDescriptionArea.setText(e.getDescription());
        electionDatePicker.setValue(e.getElectionDate());
        electionTimeField.setText(String.valueOf(e.getStartTime()));
    }

    private void clearCandidateForm() {
        candFirstNameField.clear();
        candLastNameField.clear();
        candNinField.clear();
        candPhoneField.clear();
        candEmailField.clear();
        candPositionCombo.setValue(null);
        candidatesTable.getSelectionModel().clearSelection();
    }

    private void clearElectionForm() {
        electionFormTitleField.clear();
        electionDescriptionArea.clear();
        electionDatePicker.setValue(null);
        electionTimeField.clear();
        electionsTable.getSelectionModel().clearSelection();
    }

    private String validateCandidateForm() {
        if (candFirstNameField.getText().isEmpty() || candLastNameField.getText().isEmpty()
                || candNinField.getText().isEmpty() || candPhoneField.getText().isEmpty()
                || candEmailField.getText().isEmpty()) {
            return "Please fill in every field.";
        }
        if (!candNinField.getText().trim().matches("\\d{11}")) {
            return "NIN must be exactly 11 digits.";
        }
        if (candPositionCombo.getValue() == null) {
            return "Please choose a position.";
        }
        return null;
    }

    private String requireText(String text, String message) {
        if (text == null || text.isEmpty()) throw new IllegalArgumentException(message);
        return text.trim();
    }

    private LocalDate requireDate() {
        LocalDate date = electionDatePicker.getValue();
        if (date == null) throw new IllegalArgumentException("Please choose a date.");
        return date;
    }

    private Time parseTime(String text) {
        if (text == null || text.isEmpty()) throw new IllegalArgumentException("Enter a start time, e.g. 08:00.");
        try {
            return Time.valueOf(LocalTime.parse(text.trim()));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Start time must look like 08:00 (24-hour format).");
        }
    }

    // =====================================================
    //  Shared plumbing
    // =====================================================
    @FunctionalInterface
    private interface DbAction {
        boolean run() throws SQLException;
    }

    /** Runs a database action once and turns every kind of failure into a message on the given label. */
    private boolean perform(Label label, String successMsg, String failMsg, DbAction action) {
        try {
            boolean ok = action.run();
            showMessage(label, ok ? successMsg : failMsg, !ok);
            return ok;
        } catch (IllegalArgumentException | IllegalStateException e) {
            showMessage(label, e.getMessage(), true);
        } catch (SQLException e) {
            showMessage(label, "A database error occurred. Please try again.", true);
            LOGGER.log(Level.SEVERE, "Database action failed", e);
        }
        return false;
    }

    private void showMessage(Label label, String message, boolean isError) {
        label.setText(message);
        label.setStyle(isError ? "" : "-fx-text-fill: #4ade80;");
        label.setVisible(true);
    }

    private boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        alert.setHeaderText(null);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}