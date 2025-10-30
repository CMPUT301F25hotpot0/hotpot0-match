package com.example.hotpot0.section1.controllers;

import android.content.Context;
import android.util.Patterns;

import androidx.annotation.NonNull;

import com.example.hotpot0.models.ProfileDB;
import com.example.hotpot0.models.UserProfile;

import java.util.regex.Pattern;

/**
 * This class verifies user input when creating a new user profile.
 * It ensures all required fields are valid before adding the user to the database.
 */
public class UserSignupValidator {

    private final ProfileDB profileDB;
    private final Context context; // needed to create UserProfile

    public UserSignupValidator(@NonNull Context context, @NonNull ProfileDB profileDB) {
        this.context = context;
        this.profileDB = profileDB;
    }

    /**
     * Callback interface for signup result
     */
    public interface SignupCallback {
        void onSuccess(UserProfile user);
        void onFailure(Exception e);
    }

    /**
     * Validates user inputs and adds the user to Firestore
     */
    public void verifyInputs(final String name,
                             final String email,
                             final String phone,
                             final SignupCallback callback) {

        // ===== Step 1: Basic validation =====
        if (name == null || name.trim().isEmpty()) {
            callback.onFailure(new IllegalArgumentException("Please enter your name"));
            return;
        }

        if (email == null || email.trim().isEmpty()) {
            callback.onFailure(new IllegalArgumentException("Please enter your email"));
            return;
        }

        // Name: only letters, spaces, dots, hyphens
        Pattern namePattern = Pattern.compile("^[a-zA-Z .-]+$");
        if (!namePattern.matcher(name.trim()).matches()) {
            callback.onFailure(new IllegalArgumentException("Name contains invalid characters"));
            return;
        }

        // Email format
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            callback.onFailure(new IllegalArgumentException("Please enter a valid email address"));
            return;
        }

        // Phone number optional
        if (phone != null && !phone.trim().isEmpty()) {
            Pattern phonePattern = Pattern.compile("^[0-9]{7,15}$");
            if (!phonePattern.matcher(phone.trim()).matches()) {
                callback.onFailure(new IllegalArgumentException("Phone number is invalid"));
                return;
            }
        }

        // ===== Step 2: Create UserProfile object =====
        UserProfile newUser = new UserProfile(
                context,
                name.trim(),
                email.trim(),
                (phone != null && !phone.trim().isEmpty()) ? phone.trim() : ""
        );

        // ===== Step 3: Save to Firestore via ProfileDB =====
        profileDB.addUserProfile(newUser, new ProfileDB.GetCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile result) {
                callback.onSuccess(result); // forward the saved UserProfile with assigned userID
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }
}