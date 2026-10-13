package org.example.onlinevoting.model;

public class Vote {
    private int voteId;
    private int userId;
    private int candidateId;
    private int positionId;
    private int electionId;

    public Vote() {}

    public Vote(int voteId, int userId, int candidateId, int positionId, int electionId) {
        this.voteId = voteId;
        this.userId = userId;
        this.candidateId = candidateId;
        this.positionId = positionId;
        this.electionId = electionId;
    }

    public int getVoteId() { return voteId; }
    public void setVoteId(int voteId) { this.voteId = voteId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getCandidateId() { return candidateId; }
    public void setCandidateId(int candidateId) { this.candidateId = candidateId; }

    public int getPositionId() { return positionId; }
    public void setPositionId(int positionId) { this.positionId = positionId; }

    public int getElectionId() { return electionId; }
    public void setElectionId(int electionId) { this.electionId = electionId; }
}