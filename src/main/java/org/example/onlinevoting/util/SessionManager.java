package org.example.onlinevoting.util;

public class SessionManager {

    private static final int NO_USER = -1;

    // Only one of these is ever set at a time, depending on which app is running
    private static int loggedInVoterId = NO_USER;
    private static int loggedInAdminId = NO_USER;

    private SessionManager() {
        // Utility class, not meant to be instantiated
    }

    // ---- Voter ----

    public static void setLoggedInVoter(int userId) {
        loggedInVoterId = userId;
    }

    public static int getLoggedInVoterId() {
        return loggedInVoterId;
    }

    public static boolean isVoterLoggedIn() {
        return loggedInVoterId != NO_USER;
    }

    // ---- Admin ----

    public static void setLoggedInAdmin(int adminId) {
        loggedInAdminId = adminId;
    }

    public static int getLoggedInAdminId() {
        return loggedInAdminId;
    }

    public static boolean isAdminLoggedIn() {
        return loggedInAdminId != NO_USER;
    }

    // ---- Logout ----

    public static void clear() {
        loggedInVoterId = NO_USER;
        loggedInAdminId = NO_USER;
    }
}