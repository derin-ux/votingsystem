package org.example.onlinevoting.dao;

import org.example.onlinevoting.model.Election;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ElectionDAO {

    private final Connection conn;

    public ElectionDAO(Connection conn) {
        this.conn = conn;
    }

    public boolean existsOnDate(LocalDate date) throws SQLException {
        String sql = "SELECT 1 FROM elections WHERE election_date = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean existsOnDateExcludingId(LocalDate date, int excludeElectionId) throws SQLException {
        String sql = "SELECT 1 FROM elections WHERE election_date = ? AND election_id != ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ps.setInt(2, excludeElectionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean create(Election e) throws SQLException {
        String sql = "INSERT INTO elections (title, description, election_date, start_time, status) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getTitle());
            ps.setString(2, e.getDescription());
            ps.setDate(3, Date.valueOf(e.getElectionDate()));
            ps.setTime(4, Time.valueOf(e.getStartTime()));
            ps.setString(5, e.getStatus());
            return ps.executeUpdate() > 0;
        }
    }

    public Election findActive() throws SQLException {
        String sql = "SELECT * FROM elections WHERE status = 'active' LIMIT 1";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return mapRow(rs);
            return null;
        }
    }

    public void updateStatus(int electionId, String status) throws SQLException {
        String sql = "UPDATE elections SET status = ? WHERE election_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, electionId);
            ps.executeUpdate();
        }
    }

    // Only flips to 'active' if currently 'scheduled' — mirrors the guard
    // ElectionController.startElection() needs
    public boolean activate(int electionId) throws SQLException {
        String sql = "UPDATE elections SET status = 'active' WHERE election_id = ? AND status = 'scheduled'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, electionId);
            return ps.executeUpdate() > 0;
        }
    }

    // Only flips to 'ended' if currently 'active' — mirrors the guard
    // ElectionController.endElection() needs
    public boolean close(int electionId) throws SQLException {
        String sql = "UPDATE elections SET status = 'ended' WHERE election_id = ? AND status = 'active'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, electionId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Election> findAll() throws SQLException {
        List<Election> elections = new ArrayList<>();
        String sql = "SELECT * FROM elections ORDER BY election_date DESC";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) elections.add(mapRow(rs));
        }
        return elections;
    }


    public boolean update(int electionId, String title, String description,
                          LocalDate electionDate, Time startTime) throws SQLException {
        String sql = "UPDATE elections SET title = ?, description = ?, election_date = ?, start_time = ? " +
                "WHERE election_id = ? AND status = 'scheduled'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setDate(3, Date.valueOf(electionDate));
            ps.setTime(4, startTime);
            ps.setInt(5, electionId);
            return ps.executeUpdate() > 0;
        }
    }

    private Election mapRow(ResultSet rs) throws SQLException {
        return new Election(
                rs.getInt("election_id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getDate("election_date").toLocalDate(),
                rs.getTime("start_time").toLocalTime(),
                rs.getString("status")
        );
    }
}