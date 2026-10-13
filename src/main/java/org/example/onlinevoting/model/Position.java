package org.example.onlinevoting.model;

public class Position {
    private int positionId;
    private String title;

    public Position() {}

    public Position(int positionId, String title) {
        this.positionId = positionId;
        this.title = title;
    }

    public int getPositionId() { return positionId; }
    public void setPositionId(int positionId) { this.positionId = positionId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    @Override
    public String toString() { return title; }
}