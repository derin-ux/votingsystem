package org.example.onlinevoting.controller;

import org.example.onlinevoting.dao.CandidateDAO;
import org.example.onlinevoting.model.Candidate;
import org.example.onlinevoting.util.DBConnection;
import org.example.onlinevoting.util.ElectionService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class CandidateController {

    private final ElectionService electionService = new ElectionService();

    public boolean registerCandidate(String nin, String firstName, String lastName,
                                     String phone, String email, int positionId) throws SQLException {
        if (electionService.isElectionActive()) {
            throw new IllegalStateException("Cannot register a candidate while an election is active.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            CandidateDAO candidateDAO = new CandidateDAO(conn);
            if (candidateDAO.existsByNinExcludingId(nin, -1)) {
                throw new IllegalArgumentException("A candidate with this NIN already exists.");
            }

            Candidate candidate = new Candidate(0, nin, firstName, lastName, phone, email, positionId);
            return candidateDAO.create(candidate);
        }
    }

    public boolean editCandidate(int candidateId, String nin, String firstName, String lastName,
                                 String phone, String email, int positionId) throws SQLException {
        if (electionService.isElectionActive()) {
            throw new IllegalStateException("Cannot edit a candidate while an election is active.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            CandidateDAO candidateDAO = new CandidateDAO(conn);
            if (candidateDAO.existsByNinExcludingId(nin, candidateId)) {
                throw new IllegalArgumentException("Another candidate already has this NIN.");
            }

            Candidate candidate = new Candidate(candidateId, nin, firstName, lastName, phone, email, positionId);
            return candidateDAO.update(candidate);
        }
    }

    public boolean deleteCandidate(int candidateId) throws SQLException {
        if (electionService.isElectionActive()) {
            throw new IllegalStateException("Cannot delete a candidate while an election is active.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            CandidateDAO candidateDAO = new CandidateDAO(conn);
            return candidateDAO.delete(candidateId);
        }
    }

    public List<Candidate> getAllCandidates() throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            CandidateDAO candidateDAO = new CandidateDAO(conn);
            return candidateDAO.findAll();
        }
    }
}