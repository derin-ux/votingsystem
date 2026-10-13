package org.example.onlinevoting.dao;

import org.example.onlinevoting.model.Admin;
import java.sql.*;
import java.time.LocalDateTime;

public class AdminDAO {

    public static final int MAX_FAILED_ATTEMPTS = 4;

    private final Connection conn;

    public AdminDAO(Connection conn) {
        this.conn = conn;
    }

    public Admin findByUsername(String userName) throws SQLException {
        String sql = "SELECT admin_id, user_name, password_hash, created_at, failed_attempts, locked " +
                "FROM admins WHERE user_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
                return null;
            }
        }
    }

    public void incrementFailedAttempts(int adminId) throws SQLException {
        String sql = "UPDATE admins SET " +
                "locked = CASE WHEN failed_attempts + 1 >= " + MAX_FAILED_ATTEMPTS + " THEN TRUE ELSE locked END, " +
                "failed_attempts = failed_attempts + 1 " +
                "WHERE admin_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            ps.executeUpdate();
        }
    }

//    To unlock myself for more testing runs

//    UPDATE admins SET locked = 0, failed_attempts = 0 WHERE user_name = 'your_admin_username';

    public void resetFailedAttempts(int adminId) throws SQLException {
        String sql = "UPDATE admins SET failed_attempts = 0 WHERE admin_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            ps.executeUpdate();
        }
    }

    public void unlock(int adminId) throws SQLException {
        String sql = "UPDATE admins SET locked = FALSE, failed_attempts = 0 WHERE admin_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            ps.executeUpdate();
        }
    }

    private Admin mapRow(ResultSet rs) throws SQLException {
        return new Admin(
                rs.getInt("admin_id"),
                rs.getString("user_name"),
                rs.getString("password_hash"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getInt("failed_attempts"),
                rs.getBoolean("locked")
        );
    }
}