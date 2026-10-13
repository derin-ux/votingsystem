package org.example.onlinevoting.controller;

import org.example.onlinevoting.dao.ElectionDAO;
import org.example.onlinevoting.dao.VoterDAO;
import org.example.onlinevoting.model.Election;
import org.example.onlinevoting.util.DBConnection;
import org.example.onlinevoting.util.ElectionService;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class ElectionController {

    private final ElectionService electionService = new ElectionService();

    public boolean createElection(String title, String description,
                                  LocalDate electionDate, Time startTime) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            ElectionDAO electionDAO = new ElectionDAO(conn);
            if (electionDAO.existsOnDate(electionDate)) {
                throw new IllegalArgumentException("An election is already scheduled for this date.");
            }

            Election election = new Election(0, title, description, electionDate,
                    startTime.toLocalTime(), "scheduled");
            return electionDAO.create(election);
        }
    }

    // Also resets every voter's has_voted flag to false, so voters who
    // already voted in an earlier election can vote again in this one.
    public boolean startElection(int electionId) throws SQLException {
        if (electionService.isElectionActive()) {
            throw new IllegalStateException("Another election is already active.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            ElectionDAO electionDAO = new ElectionDAO(conn);
            boolean started = electionDAO.activate(electionId);
            if (started) {
                VoterDAO voterDAO = new VoterDAO(conn);
                voterDAO.resetAllVotedStatus();
            }
            return started;
        }
    }

    public boolean endElection(int electionId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            ElectionDAO electionDAO = new ElectionDAO(conn);
            return electionDAO.close(electionId);
        }
    }

    public boolean editElection(int electionId, String title, String description,
                                LocalDate electionDate, Time startTime) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            ElectionDAO electionDAO = new ElectionDAO(conn);
            if (electionDAO.existsOnDateExcludingId(electionDate, electionId)) {
                throw new IllegalArgumentException("Another election is already scheduled for this date.");
            }

            String sql = "UPDATE elections SET title = ?, description = ?, election_date = ?, start_time = ? " +
                    "WHERE election_id = ? AND status = 'scheduled'";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, title);
                stmt.setString(2, description);
                stmt.setDate(3, Date.valueOf(electionDate));
                stmt.setTime(4, startTime);
                stmt.setInt(5, electionId);
                return stmt.executeUpdate() > 0;
            }
        }
    }

    public List<Election> getAllElections() throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            ElectionDAO electionDAO = new ElectionDAO(conn);
            return electionDAO.findAll();
        }
    }
}