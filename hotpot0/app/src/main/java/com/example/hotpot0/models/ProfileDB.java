package com.example.hotpot0.models;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;

/**
 * ProfileDB handles all Firestore operations for UserProfile and AdminProfile.
 * It can add, delete, update, and fetch profiles.
 * It also manages the ID assignment using the Counters collection.
 */
public class ProfileDB {

    // Firestore instance
    private final FirebaseFirestore db;

    // Collection names
    private static final String USERS_COLLECTION = "UserProfiles";
    private static final String ADMINS_COLLECTION = "AdminProfiles";
    private static final String COUNTERS_COLLECTION = "Counters";

    /** Initializes a ProfileDB instance and connects to Firestore. */
    public ProfileDB() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Generic callback interface for async Firestore operations that return a result.
     */
    public interface GetCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }

    /**
     * Generic callback interface for async Firestore operations that don't return a result.
     */
    public interface ActionCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    /**
     * Utility method to convert an ArrayList<String> of linkIDs in UserProfile
     * to Firestore format (ArrayList<String>), if needed.
     * Currently UserProfile uses ArrayList<String> so no conversion is needed,
     * but this method is ready if any processing is required in the future.
     *
     * @param linkIDs list of link IDs to convert
     * @return a Firestore-compatible list of link IDs
     */
    private ArrayList<String> convertLinkIDsForFirestore(ArrayList<String> linkIDs) {
        if (linkIDs == null) return new ArrayList<>();
        return new ArrayList<>(linkIDs);
    }

    /**
     * Generates the next smallest available ID for a given type ("users" or "admins").
     *
     * @param type     Either "users" or "admins"
     * @param callback Callback to return the generated ID asynchronously
     */
    public void generateNewID(@NonNull String type, @NonNull GetCallback<Integer> callback) {
        if (!type.equals("users") && !type.equals("admins")) {
            callback.onFailure(new IllegalArgumentException("Type must be 'users' or 'admins'"));
            return;
        }

        DocumentReference counterRef = db.collection(COUNTERS_COLLECTION).document(type);

        counterRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                callback.onFailure(new Exception("Counters document for " + type + " does not exist"));
                return;
            }

            // Get the array of IDs already in use
            ArrayList<Long> idsInUseLong = (ArrayList<Long>) documentSnapshot.get("idsInUse");
            ArrayList<Integer> idsInUse = new ArrayList<>();
            if (idsInUseLong != null) {
                for (Long id : idsInUseLong) {
                    idsInUse.add(id.intValue());
                }
            }

            // Find the next smallest available integer
            int nextID = 1; // start from 1
            Collections.sort(idsInUse);
            for (int id : idsInUse) {
                if (id == nextID) {
                    nextID++;
                } else if (id > nextID) {
                    break; // found a gap
                }
            }

            // Return the ID
            callback.onSuccess(nextID);

        }).addOnFailureListener(e -> callback.onFailure(e));
    }

    /**
     * Authenticates an admin with the given username and password.
     *
     * @param username The admin's username
     * @param password The admin's password
     * @param callback Callback to return the AdminProfile on success or error on failure
     */
    public void authenticateAdmin(@NonNull String username, @NonNull String password, @NonNull GetCallback<AdminProfile> callback) {
        // Query AdminProfiles collection where username matches
        db.collection(ADMINS_COLLECTION)
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        callback.onFailure(new Exception("Admin username not found"));
                        return;
                    }

                    // There should only be one admin with this username
                    DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                    AdminProfile admin = doc.toObject(AdminProfile.class);

                    if (admin == null) {
                        callback.onFailure(new Exception("Failed to parse AdminProfile"));
                        return;
                    }

                    // Check if the password matches
                    if (admin.getPassword().equals(password)) {
                        callback.onSuccess(admin);
                    } else {
                        callback.onFailure(new Exception("Incorrect password"));
                    }

                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Deletes an AdminProfile by adminID.
     *
     * @param adminID  The ID of the admin to delete
     * @param callback Callback to notify success or failure
     */
    public void deleteAdmin(int adminID, @NonNull ActionCallback callback) {
        DocumentReference adminRef = db.collection(ADMINS_COLLECTION).document(String.valueOf(adminID));

        // Step 1: Delete the admin document
        adminRef.delete()
                .addOnSuccessListener(aVoid -> {
                    // Step 2: Update Counters/admins
                    DocumentReference counterRef = db.collection(COUNTERS_COLLECTION).document("admins");
                    counterRef.get().addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            Long count = doc.getLong("count");
                            ArrayList<Long> idsInUse = (ArrayList<Long>) doc.get("idsInUse");
                            if (idsInUse == null) idsInUse = new ArrayList<>();

                            idsInUse.remove(Long.valueOf(adminID));
                            if (count != null && count > 0) count -= 1;

                            counterRef.update("count", count, "idsInUse", idsInUse)
                                    .addOnSuccessListener(aVoid1 -> callback.onSuccess())
                                    .addOnFailureListener(callback::onFailure);
                        } else {
                            callback.onFailure(new Exception("Counters document for admins does not exist"));
                        }
                    }).addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Adds a new UserProfile and returns the created profile on success.
     * This method is used by UserSignupVerifier to create and retrieve the new user in one step.
     *
     * @param user     The UserProfile object (without userID)
     * @param callback Callback that returns the created UserProfile
     */
    // Use the generated userID as the Firestore document ID to ensure 1-to-1 mapping.
    // This avoids Firestore auto-generating a random ID and simplifies lookups and updates.
    public void addUserProfile(@NonNull UserProfile user, @NonNull GetCallback<UserProfile> callback) {
        generateNewID("users", new GetCallback<Integer>() {
            @Override
            public void onSuccess(Integer newID) {
                user.setUserID(newID);
                ArrayList<String> linkIDsForFirestore = convertLinkIDsForFirestore(user.getLinkIDs());
                user.setLinkIDs(linkIDsForFirestore);

                db.collection(USERS_COLLECTION)
                        .document(String.valueOf(newID))
                        .set(user)
                        .addOnSuccessListener(aVoid -> {
                            // Update Counters/users
                            DocumentReference counterRef = db.collection(COUNTERS_COLLECTION).document("users");
                            counterRef.get().addOnSuccessListener(doc -> {
                                if (doc.exists()) {
                                    Long count = doc.getLong("count");
                                    ArrayList<Long> idsInUse = (ArrayList<Long>) doc.get("idsInUse");
                                    if (idsInUse == null) idsInUse = new ArrayList<>();
                                    idsInUse.add(Long.valueOf(newID));
                                    if (count == null) count = 0L;
                                    count += 1;

                                    counterRef.update("count", count, "idsInUse", idsInUse)
                                            .addOnSuccessListener(aVoid1 -> callback.onSuccess(user))
                                            .addOnFailureListener(callback::onFailure);
                                } else {
                                    callback.onFailure(new Exception("Counters document for users does not exist"));
                                }
                            }).addOnFailureListener(callback::onFailure);
                        })
                        .addOnFailureListener(callback::onFailure);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }


    /**
     * Updates an existing UserProfile in Firestore.
     * Only updates name, emailID, and phoneNumber.
     *
     * @param user     The UserProfile object with updated values (must have userID)
     * @param callback Callback to notify success or failure
     */
    public void updateUser(@NonNull UserProfile user, @NonNull ActionCallback callback, Double longitude, Double latitude, Boolean notificationsEnabled) {
        Integer userID = user.getUserID();
        if (userID == null) {
            callback.onFailure(new IllegalArgumentException("User ID cannot be null for update"));
            return;
        }

        DocumentReference userRef = db.collection(USERS_COLLECTION).document(String.valueOf(userID));

        // Prepare a map of fields to update
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("name", user.getName());
        updates.put("emailID", user.getEmailID());
        updates.put("phoneNumber", user.getPhoneNumber());

        // UPDATE NOTIFICATIONS PREFERENCES AND LOCATION
        // Add latitude and longitude only if provided
        if (latitude != null && longitude != null) {
            updates.put("latitude", latitude);
            updates.put("longitude", longitude);
        }
        // Update notifications preference
        if (notificationsEnabled != null) updates.put("notificationsEnabled", notificationsEnabled);

        userRef.update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Deletes a UserProfile by userID.
     *
     * @param userID   The ID of the user to delete
     * @param callback Callback to notify success or failure
     */
    public void deleteUser(int userID, @NonNull ActionCallback callback) {
        DocumentReference userRef = db.collection(USERS_COLLECTION).document(String.valueOf(userID));

        // Step 1: Delete the user document
        userRef.delete()
                .addOnSuccessListener(aVoid -> {
                    // Step 2: Update Counters/users
                    DocumentReference counterRef = db.collection(COUNTERS_COLLECTION).document("users");
                    counterRef.get().addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            Long count = doc.getLong("count");
                            ArrayList<Long> idsInUse = (ArrayList<Long>) doc.get("idsInUse");
                            if (idsInUse == null) idsInUse = new ArrayList<>();

                            idsInUse.remove(Long.valueOf(userID));
                            if (count != null && count > 0) count -= 1;

                            counterRef.update("count", count, "idsInUse", idsInUse)
                                    .addOnSuccessListener(aVoid1 -> callback.onSuccess())
                                    .addOnFailureListener(callback::onFailure);
                        } else {
                            callback.onFailure(new Exception("Counters document for users does not exist"));
                        }
                    }).addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Fetches a UserProfile by userID.
     *
     * @param userID   The ID of the user to fetch
     * @param callback Callback to return the UserProfile or an error
     */
    public void getUserByID(int userID, @NonNull GetCallback<UserProfile> callback) {
        DocumentReference userRef = db.collection(USERS_COLLECTION).document(String.valueOf(userID));

        userRef.get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        callback.onFailure(new Exception("UserProfile with ID " + userID + " does not exist"));
                        return;
                    }

                    UserProfile user = doc.toObject(UserProfile.class);
                    if (user == null) {
                        callback.onFailure(new Exception("Failed to parse UserProfile"));
                    } else {
                        callback.onSuccess(user);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * To check if User already exists - and skip StartupActivity.java
     *
     * @param deviceID device identifier associated with the user
     * @param callback callback returning the {@code UserProfile} or an error
     */
    public void getUserByDeviceID(@NonNull String deviceID, @NonNull GetCallback<UserProfile> callback) {
        db.collection(USERS_COLLECTION)
                .whereEqualTo("deviceID", deviceID)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        callback.onFailure(new Exception("No user found for this device"));
                    } else {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        UserProfile user = doc.toObject(UserProfile.class);
                        if (user == null) {
                            callback.onFailure(new Exception("Failed to parse UserProfile"));
                        } else {
                            callback.onSuccess(user);
                        }
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }
    // MANAGING EVENT-USER-LINK FOR A GIVEN USER

    /**
     * Adds a linkID to the user's linkIDs array, avoiding duplicates.
     *
     * @param user     the user profile to update
     * @param linkID   the link ID to add
     * @param callback callback to notify success or failure
     */
    public void addLinkIDToUser(@NonNull UserProfile user, @NonNull String linkID, @NonNull GetCallback<Void> callback) {

        Integer userID = user.getUserID();
        if (userID == null) {
            callback.onFailure(new IllegalArgumentException("User ID cannot be null for update"));
            return;
        }

        DocumentReference userRef = db.collection(USERS_COLLECTION).document(String.valueOf(userID));

        // Use Firestore arrayUnion to add the linkID if it doesn't already exist
        userRef.update("linkIDs", FieldValue.arrayUnion(linkID))
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Removes a linkID from the user's linkIDs array.
     * @param user     the user profile to update
     * @param linkID   the link ID to remove
     * @param callback callback to notify success or failure
     */
    public void removeLinkIDFromUser(@NonNull UserProfile user, @NonNull String linkID, @NonNull GetCallback<Void> callback) {

        Integer userID = user.getUserID();
        if (userID == null) {
            callback.onFailure(new IllegalArgumentException("User ID cannot be null for update"));
            return;
        }

        DocumentReference userRef = db.collection(USERS_COLLECTION).document(String.valueOf(userID));

        // Use Firestore arrayRemove to remove the linkID if it exists
        userRef.update("linkIDs", FieldValue.arrayRemove(linkID))
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }
}