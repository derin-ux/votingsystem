package org.example.onlinevoting.model;

import java.time.LocalDateTime;

public class Admin {
    private int adminId;
    private String userName;
    private String passwordHash;
    private LocalDateTime createdAt;
    private int failedAttempts;
    private boolean locked;

    public Admin() {}

    public Admin(int adminId, String userName, String passwordHash,
                 LocalDateTime createdAt, int failedAttempts, boolean locked) {
        this.adminId = adminId;
        this.userName = userName;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
        this.failedAttempts = failedAttempts;
        this.locked = locked;
    }

    public int getAdminId() { return adminId; }
    public void setAdminId(int adminId) { this.adminId = adminId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }
}