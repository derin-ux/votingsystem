package org.example.onlinevoting.dao;

import org.example.onlinevoting.model.Vote;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class VoteDAO {

    private final Connection conn;

    public VoteDAO(Connection conn) {
        this.conn = conn;
    }

    public void castVote(Vote v) throws SQLException {
        String sql = "INSERT INTO votes (user_id, candidate_id, position_id, election_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, v.getUserId());
            ps.setInt(2, v.getCandidateId());
            ps.setInt(3, v.getPositionId());
            ps.setInt(4, v.getElectionId());
            ps.executeUpdate();
        }
    }

    public Map<Integer, Integer> tallyByElection(int electionId) throws SQLException {
        Map<Integer, Integer> tally = new HashMap<>();
        String sql = "SELECT candidate_id, COUNT(*) AS vote_count FROM votes " +
                "WHERE election_id = ? GROUP BY candidate_id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, electionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tally.put(rs.getInt("candidate_id"), rs.getInt("vote_count"));
                }
            }
        }
        return tally;
    }
}