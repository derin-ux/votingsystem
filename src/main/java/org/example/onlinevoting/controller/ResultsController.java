package org.example.onlinevoting.controller;

import org.example.onlinevoting.dao.CandidateDAO;
import org.example.onlinevoting.dao.PositionDAO;
import org.example.onlinevoting.dao.VoteDAO;
import org.example.onlinevoting.model.Candidate;
import org.example.onlinevoting.model.Position;
import org.example.onlinevoting.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class ResultsController {

    public Map<Position, List<CandidateResult>> getResultsForElection(int electionId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            PositionDAO positionDAO = new PositionDAO(conn);
            CandidateDAO candidateDAO = new CandidateDAO(conn);
            VoteDAO voteDAO = new VoteDAO(conn);

            Map<Integer, Integer> tally = voteDAO.tallyByElection(electionId);

            List<Candidate> candidates = candidateDAO.findAll();
            Map<Integer, List<Candidate>> candidatesByPosition = new HashMap<>();
            for (Candidate c : candidates) {
                candidatesByPosition
                        .computeIfAbsent(c.getPositionId(), k -> new ArrayList<>())
                        .add(c);
            }

            Map<Position, List<CandidateResult>> results = new LinkedHashMap<>();
            List<Position> positions = positionDAO.findAll();

            for (Position position : positions) {
                List<Candidate> positionCandidates =
                        candidatesByPosition.getOrDefault(position.getPositionId(), List.of());

                List<CandidateResult> candidateResults = new ArrayList<>();
                for (Candidate c : positionCandidates) {
                    int votes = tally.getOrDefault(c.getCandidateId(), 0);
                    candidateResults.add(new CandidateResult(c, votes));
                }

                candidateResults.sort((a, b) -> Integer.compare(b.getVoteCount(), a.getVoteCount()));
                results.put(position, candidateResults);
            }

            return results;
        }
    }

    public int getTotalVotesCast(int electionId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            VoteDAO voteDAO = new VoteDAO(conn);
            Map<Integer, Integer> tally = voteDAO.tallyByElection(electionId);
            return tally.values().stream().mapToInt(Integer::intValue).sum();
        }
    }

    public static class CandidateResult {
        private final Candidate candidate;
        private final int voteCount;

        public CandidateResult(Candidate candidate, int voteCount) {
            this.candidate = candidate;
            this.voteCount = voteCount;
        }

        public Candidate getCandidate() { return candidate; }
        public int getVoteCount() { return voteCount; }
    }
}