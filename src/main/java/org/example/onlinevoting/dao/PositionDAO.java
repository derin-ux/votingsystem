package org.example.onlinevoting.dao;

import org.example.onlinevoting.model.Position;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PositionDAO {

    private final Connection conn;

    public PositionDAO(Connection conn) {
        this.conn = conn;
    }

    public List<Position> findAll() throws SQLException {
        List<Position> positions = new ArrayList<>();
        String sql = "SELECT * FROM positions ORDER BY position_id";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) positions.add(mapRow(rs));
        }
        return positions;
    }

    public Position findById(int positionId) throws SQLException {
        String sql = "SELECT * FROM positions WHERE position_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, positionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
                return null;
            }
        }
    }

    public boolean create(Position p) throws SQLException {
        String sql = "INSERT INTO positions (title) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getTitle());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(Position p) throws SQLException {
        String sql = "UPDATE positions SET title = ? WHERE position_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getTitle());
            ps.setInt(2, p.getPositionId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int positionId) throws SQLException {
        String sql = "DELETE FROM positions WHERE position_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, positionId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean hasCandidatesAssigned(int positionId) throws SQLException {
        String sql = "SELECT 1 FROM candidates WHERE position_id = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, positionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Position mapRow(ResultSet rs) throws SQLException {
        return new Position(
                rs.getInt("position_id"),
                rs.getString("title")
        );
    }
}