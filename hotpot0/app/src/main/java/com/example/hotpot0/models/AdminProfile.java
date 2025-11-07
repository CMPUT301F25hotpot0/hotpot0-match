package com.example.hotpot0.models;

import android.content.Context;
import androidx.annotation.NonNull;

/**
 * Represents an admin profile stored in Firestore.
 * For simplicity, the password is stored as plain text.
 */
public class AdminProfile {

    // Firestore document fields
    private Integer adminID;     // Firestore document ID
    private String username;    // Admin username
    private String password;    // Raw password

    // Constructors

    /** Required empty constructor for Firestore deserialization
     */
    public AdminProfile() {}

    /**
     * Parameterized constructor
     * @param context
     * @param username
     * @param password
     */
    public AdminProfile(Context context, String username, String password) {
        this.adminID = null;    // Firestore will assign this later
        this.username = username;
        this.password = password;
    }

    // Getters and Setters

    public Integer getAdminID() { return adminID; }
    public void setAdminID(Integer adminID) { this.adminID = adminID; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // Utility

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