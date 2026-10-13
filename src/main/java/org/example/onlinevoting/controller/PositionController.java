package org.example.onlinevoting.controller;

import org.example.onlinevoting.dao.PositionDAO;
import org.example.onlinevoting.model.Position;
import org.example.onlinevoting.util.DBConnection;
import org.example.onlinevoting.util.ElectionService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class PositionController {

    private final ElectionService electionService = new ElectionService();

    public boolean addPosition(String title) throws SQLException {
        if (electionService.isElectionActive()) {
            throw new IllegalStateException("Cannot add a position while an election is active.");
        }
        String cleanTitle = requireTitle(title);

        try (Connection conn = DBConnection.getConnection()) {
            PositionDAO positionDAO = new PositionDAO(conn);
            Position position = new Position();
            position.setTitle(cleanTitle);
            return positionDAO.create(position);
        }
    }

    public boolean editPosition(int positionId, String newTitle) throws SQLException {
        if (electionService.isElectionActive()) {
            throw new IllegalStateException("Cannot edit a position while an election is active.");
        }
        String cleanTitle = requireTitle(newTitle);

        try (Connection conn = DBConnection.getConnection()) {
            PositionDAO positionDAO = new PositionDAO(conn);
            Position position = positionDAO.findById(positionId);
            if (position == null) {
                throw new IllegalArgumentException("Position not found.");
            }
            position.setTitle(cleanTitle);
            return positionDAO.update(position);
        }
    }

    public boolean deletePosition(int positionId) throws SQLException {
        if (electionService.isElectionActive()) {
            throw new IllegalStateException("Cannot delete a position while an election is active.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            PositionDAO positionDAO = new PositionDAO(conn);
            if (positionDAO.hasCandidatesAssigned(positionId)) {
                throw new IllegalStateException("Cannot delete a position that already has candidates assigned.");
            }
            return positionDAO.delete(positionId);
        }
    }

    public List<Position> getAllPositions() throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            PositionDAO positionDAO = new PositionDAO(conn);
            return positionDAO.findAll();
        }
    }

    // Trims first, then checks, so a title of only spaces is rejected too
    private String requireTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Position title cannot be empty.");
        }
        return title.trim();
    }
}