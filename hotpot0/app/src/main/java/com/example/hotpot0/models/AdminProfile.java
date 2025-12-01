package com.example.hotpot0.models;

import android.content.Context;
import androidx.annotation.NonNull;


/**
 * Represents an admin profile stored in Firestore.
 * <p>
 * Each {@code AdminProfile} instance contains a unique ID, username, and password
 * associated with an administrative user. For simplicity, the password is stored
 * in plain text, but in a production environment, it should be hashed and secured.
 * </p>
 */
public class AdminProfile {

    // Firestore document fields
    private Integer adminID;     // Firestore document ID
    private String deviceID;     // Device-specific identifier
    private String username;     // Admin username -- No longer used
    private String password;     // Raw password -- No longer used

    // Constructors

    /**
     * Required empty constructor for Firestore deserialization.
     * <p>
     * Firestore uses this constructor when reconstructing objects
     * from database documents.
     * </p>
     */
    public AdminProfile() {}

    /**
     * Creates a new {@code AdminProfile} instance with the specified username and password.
     * @param context  the application context (not stored; may be used for Firestore initialization)
     * @param username the admin’s username
     * @param password the admin’s password (in plain text)
     */
    public AdminProfile(Context context, String username, String password) {
        this.adminID = null;    // Firestore will assign this later
        this.username = username;
        this.password = password;
    }

    /**
     * Retrieves the Firestore-assigned admin ID.
     * @return the admin ID, or {@code null} if not yet assigned
     */
    public Integer getAdminID() { return adminID; }

    /**
     * Sets the admin’s Firestore ID.
     * @param adminID the unique ID assigned by Firestore
     */
    public void setAdminID(Integer adminID) { this.adminID = adminID; }

    /**
     * Retrieves the username of this admin.
     * @return the admin’s username
     */
    public String getUsername() { return username; }

    /**
     * Sets the username for this admin.
     * @param username the admin’s username
     */
    public void setUsername(String username) { this.username = username; }

    /**
     * Retrieves the admin’s password.
     * @return the admin’s password in plain text
     */
    public String getPassword() { return password; }

    /**
     * Sets the admin’s password.
     * @param password the new password for the admin
     */
    public void setPassword(String password) { this.password = password; }

    /**
     * Returns a string representation of this admin profile.
     * The password value is masked for display purposes.
     * @return a formatted string containing admin details
     */
    @NonNull
    @Override
    public String toString() {
        return "AdminProfile{" +
                "adminID='" + adminID + '\'' +
                ", username='" + username + '\'' +
                ", password='" + (password != null ? "********" : null) + '\'' +
                '}';
    }
}