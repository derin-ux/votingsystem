package org.example.onlinevoting.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Election {
    private int electionId;
    private String title;
    private String description;
    private LocalDate electionDate;
    private LocalTime startTime;
    private String status; // e.g. "scheduled", "active", or "ended"

    public Election() {}

    public Election(int electionId, String title, String description,
                    LocalDate electionDate, LocalTime startTime, String status) {
        this.electionId = electionId;
        this.title = title;
        this.description = description;
        this.electionDate = electionDate;
        this.startTime = startTime;
        this.status = status;
    }

    public int getElectionId() { return electionId; }
    public void setElectionId(int electionId) { this.electionId = electionId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getElectionDate() { return electionDate; }
    public void setElectionDate(LocalDate electionDate) { this.electionDate = electionDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}