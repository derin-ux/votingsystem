package org.example.onlinevoting.model;

public class Voter {

    private int userId;
    private String nin;
    private String firstName;
    private String lastName;
    private String passwordHash;
    private boolean hasVoted;

    // Needed by VoterRegistrationController (new Voter() + setters)
    public Voter() {
    }

    // Needed by VoterDAO.mapRow()
    public Voter(int userId, String nin, String firstName, String lastName,
                 String passwordHash, boolean hasVoted) {
        this.userId = userId;
        this.nin = nin;
        this.firstName = firstName;
        this.lastName = lastName;
        this.passwordHash = passwordHash;
        this.hasVoted = hasVoted;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getNin() { return nin; }
    public void setNin(String nin) { this.nin = nin; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    // Named isHasVoted() because VoterDAO.create() calls v.isHasVoted()
    public boolean isHasVoted() { return hasVoted; }
    public void setHasVoted(boolean hasVoted) { this.hasVoted = hasVoted; }
}