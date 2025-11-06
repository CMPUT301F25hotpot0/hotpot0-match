package com.example.hotpot0.section2.controllers;

import android.content.Context;

import com.example.hotpot0.models.ProfileDB;
import com.example.hotpot0.models.UserProfile;

public class ProfileEditHandler {

    private ProfileDB profileDB;

    public ProfileEditHandler() {
        profileDB = new ProfileDB(); // Initialize ProfileDB to interact with Firestore
    }

    /**
     * Handles the updating of user profile details.
     * @param context The application context
     * @param userID The user ID of the profile to update
     * @param newName The new name to update to
     * @param newEmailID The new email to update to
     * @param newPhoneNumber The new phone number to update to
     * @param callback A callback interface to notify success or failure
     */
    public void handleProfileUpdate(Context context, int userID, String newName, String newEmailID, String newPhoneNumber, ProfileDB.ActionCallback callback) {
        // Fetch the current user profile from Firestore using the userID
        profileDB.getUserByID(userID, new ProfileDB.GetCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile currentUser) {
                // Check if any of the fields have changed
                boolean isUpdated = false;

                if (!currentUser.getName().equals(newName)) {
                    currentUser.setName(newName);
                    isUpdated = true;
                }

                if (!currentUser.getEmailID().equals(newEmailID)) {
                    currentUser.setEmailID(newEmailID);
                    isUpdated = true;
                }

                if (!currentUser.getPhoneNumber().equals(newPhoneNumber)) {
                    currentUser.setPhoneNumber(newPhoneNumber);
                    isUpdated = true;
                }

                // If any field has changed, proceed to update the profile in Firestore
                if (isUpdated) {
                    profileDB.updateUser(currentUser, new ProfileDB.ActionCallback() {
                        @Override
                        public void onSuccess() {
                            // Notify that the profile was successfully updated
                            callback.onSuccess();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            // Notify failure in updating the profile
                            callback.onFailure(e);
                        }
                    });
                } else {
                    // Notify that there was no change to update
                    callback.onFailure(new Exception("No changes detected in the user profile."));
                }
            }

            @Override
            public void onFailure(Exception e) {
                // Handle the case where the user profile could not be fetched
                callback.onFailure(e);
            }
        });
    }
}

