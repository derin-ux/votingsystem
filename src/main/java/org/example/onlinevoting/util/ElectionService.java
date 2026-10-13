package org.example.onlinevoting.util;

import org.example.onlinevoting.dao.ElectionDAO;
import org.example.onlinevoting.model.Election;
import java.sql.*;

// Shared read-only election queries — no FXML, no admin/voter actions.
// Called by CandidateController, ElectionController, and BallotController alike.
public class ElectionService {

    public boolean isElectionActive() throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            ElectionDAO electionDAO = new ElectionDAO(conn);
            return electionDAO.findActive() != null;
        }
    }

    public Election getActiveElection() throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            ElectionDAO electionDAO = new ElectionDAO(conn);
            return electionDAO.findActive();
        }
    }
}