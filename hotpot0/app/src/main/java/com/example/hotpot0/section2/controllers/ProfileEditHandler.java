package com.example.hotpot0.section2.controllers;

import android.content.Context;
import android.util.Log;
import android.util.Patterns;

import com.example.hotpot0.models.Event;
import com.example.hotpot0.models.EventDB;
import com.example.hotpot0.models.EventUserLinkDB;
import com.example.hotpot0.models.ProfileDB;
import com.example.hotpot0.models.UserProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Handles operations related to editing and managing user profiles.
 * <p>
 * This class provides methods to update user details, validate inputs,
 * and delete user profiles in the Firestore database using {@link ProfileDB}.
 * </p>
 */
public class ProfileEditHandler {

    private ProfileDB profileDB;
    private EventUserLinkDB eventUserLinkDB;
    private EventDB eventDB;

    /**
     * Constructs a new {@code ProfileEditHandler}.
     * <p>
     * Initializes the {@link ProfileDB} instance used to interact with user profiles.
     * </p>
     */
    public ProfileEditHandler() {
        profileDB = new ProfileDB(); // Initialize ProfileDB to interact with Firestore
        eventUserLinkDB = new EventUserLinkDB();
        eventDB = new EventDB();
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
    public void handleProfileUpdate(Context context, int userID, String newName, String newEmailID, String newPhoneNumber, Boolean notificationsEnabled, ProfileDB.ActionCallback callback) {
        // Fetch the current user profile from Firestore using the userID
        profileDB.getUserByID(userID, new ProfileDB.GetCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile currentUser) {
                // Check if any of the fields have changed
                boolean isUpdated = false;

                if (newName == null || newName.trim().isEmpty()) {
                    callback.onFailure(new IllegalArgumentException("Please enter your name"));
                    return;
                }

                if (newEmailID == null || newEmailID.trim().isEmpty()) {
                    callback.onFailure(new IllegalArgumentException("Please enter your email"));
                    return;
                }

                // Name: only letters, spaces, dots, hyphens
                Pattern namePattern = Pattern.compile("^[a-zA-Z .-]+$");
                if (!namePattern.matcher(newName.trim()).matches()) {
                    callback.onFailure(new IllegalArgumentException("Name contains invalid characters"));
                    return;
                }

                // Email format
                if (!Patterns.EMAIL_ADDRESS.matcher(newEmailID.trim()).matches()) {
                    callback.onFailure(new IllegalArgumentException("Please enter a valid email address"));
                    return;
                }

                // Phone number optional
                if (newPhoneNumber != null && !newPhoneNumber.trim().isEmpty()) {
                    Pattern phonePattern = Pattern.compile("^[0-9]{7,15}$");
                    if (!phonePattern.matcher(newPhoneNumber.trim()).matches()) {
                        callback.onFailure(new IllegalArgumentException("Phone number is invalid"));
                        return;
                    }
                }

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

                if (currentUser.getNotificationsEnabled() != notificationsEnabled) {
                    currentUser.setNotificationsEnabled(notificationsEnabled);
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
                    }, 0.0, 0.0, notificationsEnabled);
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

    /**
     * Deletes the user profile with the given userID.
     * @param userID The user ID of the profile to delete
     * @param callback A callback interface to notify success or failure
     */
    public void deleteUserProfile(int userID, ProfileDB.ActionCallback callback) {
        // Call the ProfileDB method to delete all the EventUserLinks associated with the user and then delete the user profile
        profileDB.getUserByID(userID, new ProfileDB.GetCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile user) {
                ArrayList<String> linkIDs = user.getLinkIDs();
                if (linkIDs == null || linkIDs.isEmpty()) {
                    // No links to delete, proceed to delete user profile
                    profileDB.deleteUser(userID, new ProfileDB.ActionCallback() {
                        @Override
                        public void onSuccess() {
                            // Notify that the profile was successfully deleted
                            callback.onSuccess();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            // Notify failure in deleting the profile
                            callback.onFailure(e);
                        }
                    });
                    return;
                } else {
                    for (String linkID : linkIDs) {
//                        int eventID = Integer.parseInt(linkID.split("_")[0]);
                        eventUserLinkDB.getEventUserLinkByID(linkID, new EventUserLinkDB.GetCallback<com.example.hotpot0.models.EventUserLink>() {
                            @Override
                            public void onSuccess(com.example.hotpot0.models.EventUserLink eventUserLink) {
                                if ("Organizer".equals(eventUserLink.getStatus())) {

                                    // Handle failure to fetch event document
                                    eventDB.getEventByID(eventUserLink.getEventID(), new EventDB.GetCallback<Event>() {
                                        @Override
                                        public void onSuccess(Event event) {
                                            List<String> linkIDs = event.getLinkIDs();
                                            Log.d("EventDB", "Deleting Event with linkIDs: " + linkIDs);
                                            // Delete associated EventUserLink documents
                                            for (String linkID : linkIDs) {
                                                int userID = Integer.parseInt(linkID.split("_")[1]);
                                                eventUserLinkDB.deleteEventUserLink(linkID, new EventUserLinkDB.ActionCallback() {
                                                    @Override
                                                    public void onSuccess() {
                                                        profileDB.getUserByID(userID, new ProfileDB.GetCallback<UserProfile>() {
                                                            @Override
                                                            public void onSuccess(UserProfile userProfile) {
                                                                profileDB.removeLinkIDFromUser(userProfile, linkID, new ProfileDB.GetCallback<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        // Successfully removed linkID from user profile
                                                                        // Delete all events where user is an organizer
                                                                        eventDB.deleteEvent(eventUserLink.getEventID(), new ProfileDB.ActionCallback() {
                                                                            @Override
                                                                            public void onSuccess() {
                                                                                // Successfully deleted the event
                                                                                Log.d("ProfileEditHandler", "Deleted event with ID: " + eventUserLink.getEventID());
                                                                            }
                                                                            @Override
                                                                            public void onFailure(Exception e) {
                                                                                // Log the failure but continue
                                                                                e.printStackTrace();
                                                                            }
                                                                        });
                                                                    }
                                                                    @Override
                                                                    public void onFailure(Exception e) {
                                                                        // Handle failure to remove linkID from user profile
                                                                        e.printStackTrace();
                                                                    }
                                                                });
                                                            }
                                                            @Override
                                                            public void onFailure(Exception e) {
                                                                // Handle failure to fetch user profile
                                                                e.printStackTrace();
                                                            }
                                                        });
                                                    }
                                                    @Override
                                                    public void onFailure(Exception e) {
                                                        // Handle failure to delete EventUserLink
                                                        e.printStackTrace();
                                                    }
                                                });
                                            }
                                        }
                                        @Override
                                        public void onFailure(Exception e) {
                                            Log.e("EventDB", "Failed to fetch event for deletion: " + e.getMessage());
                                        }
                                    });

                                } else {
                                    // Successfully fetched the EventUserLink, now delete it
                                    eventUserLinkDB.deleteEventUserLink(linkID, new EventUserLinkDB.ActionCallback() {
                                        @Override
                                        public void onSuccess() {
                                            // Successfully deleted the EventUserLink
                                            eventDB.getEventByID(eventUserLink.getEventID(), new EventDB.GetCallback<com.example.hotpot0.models.Event>() {
                                                @Override
                                                public void onSuccess(com.example.hotpot0.models.Event event) {
                                                    eventDB.removeLinkIDFromEvent(event, linkID, new EventDB.GetCallback<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            // Successfully removed the linkID from the event
                                                            profileDB.deleteUser(userID, new ProfileDB.ActionCallback() {
                                                                @Override
                                                                public void onSuccess() {
                                                                    // Notify that the profile was successfully deleted
                                                                    callback.onSuccess();
                                                                }

                                                                @Override
                                                                public void onFailure(Exception e) {
                                                                    // Notify failure in deleting the profile
                                                                    callback.onFailure(e);
                                                                }
                                                            });
                                                        }

                                                        @Override
                                                        public void onFailure(Exception e) {
                                                            // Log the failure but continue
                                                            e.printStackTrace();
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onFailure(Exception e) {
                                                    // Log the failure but continue
                                                    e.printStackTrace();
                                                }
                                            });
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            // Log the failure but continue
                                            e.printStackTrace();
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                // Log the failure but continue
                                e.printStackTrace();
                            }
                        });
                    }
                }
            }
            @Override
            public void onFailure(Exception e) {
                // Log the failure but continue with deletion of user profile
                e.printStackTrace();
            }
        });
    }
}