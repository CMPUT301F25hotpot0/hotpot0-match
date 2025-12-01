package com.example.hotpot0.models;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * EventUserLinkDB handles all Firestore operations for EventUserLink.
 * It can add, delete, update, and fetch EventUserLink data.
 * <p>
 * It also provides helper functions to retrieve all users currently
 * on a waitlist for a given event based on their {@code linkIDs}.
 * </p>
 */
public class EventUserLinkDB {

    // EventUserLinkDB attributes
    private final FirebaseFirestore db;
    private static final String EVENT_USER_LINK_COLLECTION = "EventUserLinks";

    /** Initializes a new {@code EventUserLinkDB} and connects to Firestore. */
    public EventUserLinkDB() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Callback interface for Firestore operations that return data.
     * @param <T> the expected result type
     */
    public interface GetCallback<T> {
        /**
         * Called when the Firestore operation completes successfully.
         * @param result the result data returned from Firestore
         */
        void onSuccess(T result);

        /**
         * Called when the Firestore operation fails.
         * @param e the exception representing the error
         */
        void onFailure(Exception e);
    }

    /**
     * Callback interface for Firestore operations that perform an action
     * without returning specific data, like update or delete.
     */
    public interface ActionCallback {
        /** Called when the action completes successfully. */
        void onSuccess();

        /**
         * Called when the action fails.
         * @param e the exception describing the error
         */
        void onFailure(Exception e);
    }

    // Utility Methods
    // ===============

    /**
     * Adds a new EventUserLink and returns the created EventUserLink on success.
     * This method creates and retrieves the new EventUserLink in one step.
     * @param eventUserLink The EventUserLink object to be added (without linkID)
     * @param callback      Callback that returns the created EventUserLink
     */
    public void addEventUserLink(@NonNull EventUserLink eventUserLink, @NonNull GetCallback<EventUserLink> callback) {
        // Create a new document reference with an auto-generated ID
        DocumentReference newLinkRef = db.collection(EVENT_USER_LINK_COLLECTION).document(eventUserLink.getLinkID());

        // Add the EventUserLink to Firestore
        newLinkRef.set(eventUserLink)
                .addOnSuccessListener(aVoid -> {
                    // Fetch the newly created EventUserLink to return it
                    newLinkRef.get()
                            .addOnSuccessListener(doc -> {
                                if (doc.exists()) {
                                    EventUserLink createdLink = doc.toObject(EventUserLink.class);
                                    if (createdLink != null) {
                                        callback.onSuccess(createdLink);
                                    } else {
                                        callback.onFailure(new Exception("Failed to parse created EventUserLink"));
                                    }
                                } else {
                                    callback.onFailure(new Exception("Created EventUserLink does not exist"));
                                }
                            })
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Updates an existing EventUserLink in Firestore.
     * Only updates status and notifications fields.
     * @param eventUserLink The EventUserLink object with updated values (must have linkID)
     * @param callback      Callback to notify success or failure
     */
    public void updateEventUserLink(@NonNull EventUserLink eventUserLink, @NonNull ActionCallback callback) {
        String linkID = eventUserLink.getLinkID();  // LinkID is now a String
        if (linkID == null) {
            callback.onFailure(new IllegalArgumentException("Link ID cannot be null for update"));
            return;
        }

        DocumentReference linkRef = db.collection(EVENT_USER_LINK_COLLECTION).document(linkID); // Use linkID as a String

        // Prepare the data to be updated
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("status", eventUserLink.getStatus());
        updates.put("notifications", eventUserLink.getNotifications());

        linkRef.update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Deletes an EventUserLink by linkID.
     * @param linkID  The ID of the EventUserLink to delete
     * @param callback Callback to notify success or failure
     */
    public void deleteEventUserLink(String linkID, @NonNull ActionCallback callback) {  // LinkID is now a String
        DocumentReference linkRef = db.collection(EVENT_USER_LINK_COLLECTION).document(linkID);  // Use linkID as a String

        linkRef.delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Fetches an EventUserLink by linkID.
     * @param linkID   The ID of the EventUserLink to fetch
     * @param callback Callback to return the EventUserLink or an error
     */
    public void getEventUserLinkByID(String linkID, @NonNull GetCallback<EventUserLink> callback) {  // LinkID is now a String
        DocumentReference linkRef = db.collection(EVENT_USER_LINK_COLLECTION).document(linkID);  // Use linkID as a String

        linkRef.get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        callback.onFailure(new Exception("EventUserLink with ID " + linkID + " does not exist"));
                        return;
                    }

                    EventUserLink eventUserLink = doc.toObject(EventUserLink.class);
                    if (eventUserLink == null) {
                        callback.onFailure(new Exception("Failed to parse EventUserLink"));
                    } else {
                        callback.onSuccess(eventUserLink);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Retrieves all user link IDs currently marked as {@code "inWaitList"}
     * from a given list of {@code linkIDs}.
     * <p>
     * For each provided link ID, this method asynchronously fetches the
     * corresponding {@link EventUserLink} document and checks if its status
     * equals {@code "inWaitList"}. Once all requests are complete,
     * the callback is invoked with the filtered list.
     * </p>
     *
     * @param linkIDs  list of {@code linkIDs} to check
     * @param callback callback returning the list of link IDs currently in the waitlist
     */
    public void getWaitListUsers(List<String> linkIDs, @NonNull GetCallback<List<String>> callback) {
        if (linkIDs == null || linkIDs.isEmpty()) {
            callback.onSuccess(Collections.emptyList());
            return;
        }

        List<String> waitListUsers = new ArrayList<>();
        final int[] completed = {0}; // track async completions

        for (String linkID : linkIDs) {
            getEventUserLinkByID(linkID, new GetCallback<EventUserLink>() {
                @Override
                public void onSuccess(EventUserLink result) {
                    if ("inWaitList".equalsIgnoreCase(result.getStatus())) {
                        waitListUsers.add(result.getLinkID());
                    }
                    checkIfDone();
                }

                @Override
                public void onFailure(Exception e) {
                    checkIfDone();
                }

                private void checkIfDone() {
                    completed[0]++;
                    if (completed[0] == linkIDs.size()) {
                        callback.onSuccess(waitListUsers);
                    }
                }
            });
        }
    }
    public void getAllOrganizers(@NonNull GetCallback<List<Integer>> callback) {

        db.collection(EVENT_USER_LINK_COLLECTION)
                .whereEqualTo("status.status", "Organizer")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    List<Integer> organizerIDs = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        EventUserLink link = doc.toObject(EventUserLink.class);
                        if (link != null && link.getUserID() != null) {
                            organizerIDs.add(link.getUserID());
                        }
                    }

                    callback.onSuccess(organizerIDs);
                })
                .addOnFailureListener(callback::onFailure);
    }
}