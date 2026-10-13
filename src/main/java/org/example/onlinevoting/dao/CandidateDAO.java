package org.example.onlinevoting.dao;

import org.example.onlinevoting.model.Candidate;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CandidateDAO {

    private final Connection conn;

    public CandidateDAO(Connection conn) {
        this.conn = conn;
    }

    public boolean existsByNinExcludingId(String nin, int excludeCandidateId) throws SQLException {
        String sql = "SELECT 1 FROM candidates WHERE nin = ? AND candidate_id != ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nin);
            ps.setInt(2, excludeCandidateId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean create(Candidate c) throws SQLException {
        String sql = "INSERT INTO candidates (nin, first_name, last_name, phone, email, position_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getNin());
            ps.setString(2, c.getFirstName());
            ps.setString(3, c.getLastName());
            ps.setString(4, c.getPhone());
            ps.setString(5, c.getEmail());
            ps.setInt(6, c.getPositionId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(Candidate c) throws SQLException {
        String sql = "UPDATE candidates SET nin = ?, first_name = ?, last_name = ?, " +
                "phone = ?, email = ?, position_id = ? WHERE candidate_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getNin());
            ps.setString(2, c.getFirstName());
            ps.setString(3, c.getLastName());
            ps.setString(4, c.getPhone());
            ps.setString(5, c.getEmail());
            ps.setInt(6, c.getPositionId());
            ps.setInt(7, c.getCandidateId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int candidateId) throws SQLException {
        String sql = "DELETE FROM candidates WHERE candidate_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Candidate> findByPosition(int positionId) throws SQLException {
        List<Candidate> candidates = new ArrayList<>();
        String sql = "SELECT * FROM candidates WHERE position_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, positionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) candidates.add(mapRow(rs));
            }
        }
        return candidates;
    }

    public List<Candidate> findAll() throws SQLException {
        List<Candidate> candidates = new ArrayList<>();
        String sql = "SELECT * FROM candidates ORDER BY position_id, last_name";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) candidates.add(mapRow(rs));
        }
        return candidates;
    }

    private Candidate mapRow(ResultSet rs) throws SQLException {
        return new Candidate(
                rs.getInt("candidate_id"),
                rs.getString("nin"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getInt("position_id")
        );
    }
}