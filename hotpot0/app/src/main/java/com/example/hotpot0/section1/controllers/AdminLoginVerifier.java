package com.example.hotpot0.section1.controllers;

import java.util.regex.Pattern; // for checking allowed characters in username
import androidx.annotation.NonNull; // for marking parameters that must not be null
import com.example.hotpot0.models.ProfileDB; // database class for admin profiles
import com.example.hotpot0.models.AdminProfile; // data model for an admin user

/**
 * This class is responsible for verifying admin login credentials.
 * It checks if the username and password are valid and match what's in the database.
 */
public class AdminLoginVerifier {

    // A reference to the ProfileDB (the database that stores admin info)
    private final ProfileDB profileDB;

    /**
     * Constructor — called when creating an AdminLoginVerifier object.
     * We pass in a ProfileDB so this class can check login info against stored data.
     */
    public AdminLoginVerifier(@NonNull ProfileDB profileDB) {
        this.profileDB = profileDB;
    }

    /**
     * This interface is a "callback" — it tells us what happens after we try to log in.
     * Because checking the database can take time, we don't get an answer immediately.
     * So, instead, the result is sent back later through these two methods:
     */
    // Any class or object that wants to listen to the login result must have these two methods implemented.
    // If you want me (AdminLoginVerifier) to tell you when login finishes,
    // then give me an object that knows what to do for success and failure.
    public interface AuthCallback {
        void onSuccess(AdminProfile admin); // called when login is successful
        void onFailure(Exception e);        // called when login fails (error or invalid)
    }

    /**
     * This method does the actual work of verifying login credentials.
     * It first checks if the inputs are valid, then checks the database.
     * The final result (success or failure) is sent through the AuthCallback.
     */
    public void verifyCredentials(final String username, final String password, final AuthCallback callback) {

        // STEP 1: Basic input validation

        // Check if username is empty or null
        if (username == null || username.trim().isEmpty()) {
            callback.onFailure(new IllegalArgumentException("Please enter a username"));
            return; // stop here if invalid
        }

        // Check if password is empty or null
        if (password == null || password.isEmpty()) {
            callback.onFailure(new IllegalArgumentException("Please enter a password"));
            return; // stop here if invalid
        }

        // Check if username is at least 3 characters long
        if (username.length() < 3) {
            callback.onFailure(new IllegalArgumentException("Username must be at least 3 characters"));
            return;
        }

        // Check if password is at least 4 characters long
        if (password.length() < 4) {
            callback.onFailure(new IllegalArgumentException("Password must be at least 4 characters"));
            return;
        }

        // Create a pattern that allows only letters, numbers, dots, underscores, and hyphens
        final Pattern allowed = Pattern.compile("^[a-zA-Z0-9._-]+$");

        // Check if username matches the allowed pattern
        if (!allowed.matcher(username).matches()) {
            callback.onFailure(new IllegalArgumentException("Username contains invalid characters"));
            return;
        }

        // STEP 2: If validation passes, authenticate with ProfileDB

        // Call the ProfileDB method that checks if the username and password exist and match
        profileDB.authenticateAdmin(username, password, new ProfileDB.GetCallback<AdminProfile>() {

            // This is called if the admin credentials are found and correct
            @Override
            public void onSuccess(AdminProfile result) {
                // Forward the success result to whoever called verifyCredentials
                callback.onSuccess(result);
            }

            // This is called if authentication failed or something went wrong
            @Override
            public void onFailure(Exception e) {
                // Forward the failure to the callback
                callback.onFailure(e);
            }
        });
    }
}