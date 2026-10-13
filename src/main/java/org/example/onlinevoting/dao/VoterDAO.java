package org.example.onlinevoting.dao;

import org.example.onlinevoting.model.Voter;
import java.sql.*;

public class VoterDAO {

    private final Connection conn;

    public VoterDAO(Connection conn) {
        this.conn = conn;
    }

    public Voter findByNin(String nin) throws SQLException {
        String sql = "SELECT * FROM users WHERE nin = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nin);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
                return null;
            }
        }
    }

    public boolean existsByNin(String nin) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE nin = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nin);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean create(Voter v) throws SQLException {
        String sql = "INSERT INTO users (nin, first_name, last_name, password_hash, has_voted) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, v.getNin());
            ps.setString(2, v.getFirstName());
            ps.setString(3, v.getLastName());
            ps.setString(4, v.getPasswordHash());
            ps.setBoolean(5, v.isHasVoted());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean markAsVoted(int userId) throws SQLException {
        String sql = "UPDATE users SET has_voted = TRUE WHERE user_id = ? AND has_voted = FALSE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public void resetAllVotedStatus() throws SQLException {
        String sql = "UPDATE users SET has_voted = FALSE";
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
        }
    }

    private Voter mapRow(ResultSet rs) throws SQLException {
        return new Voter(
                rs.getInt("user_id"),
                rs.getString("nin"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("password_hash"),
                rs.getBoolean("has_voted")
        );
    }
}