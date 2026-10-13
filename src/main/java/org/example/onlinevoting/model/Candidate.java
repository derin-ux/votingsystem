package org.example.onlinevoting.model;

public class Candidate {
    private int candidateId;
    private String nin;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private int positionId;

    public Candidate() {}

    public Candidate(int candidateId, String firstName, String lastName) {
        this.candidateId = candidateId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Candidate(int candidateId, String nin, String firstName, String lastName,
                     String phone, String email, int positionId) {
        this.candidateId = candidateId;
        this.nin = nin;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.positionId = positionId;
    }

    public int getCandidateId() { return candidateId; }
    public void setCandidateId(int candidateId) { this.candidateId = candidateId; }

    public String getNin() { return nin; }
    public void setNin(String nin) { this.nin = nin; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFullName() { return firstName + " " + lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getPositionId() { return positionId; }
    public void setPositionId(int positionId) { this.positionId = positionId; }
}