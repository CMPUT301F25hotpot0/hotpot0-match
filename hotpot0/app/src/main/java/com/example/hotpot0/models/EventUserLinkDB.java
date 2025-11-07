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
 */
public class EventUserLinkDB {

    // Firestore instance
    private final FirebaseFirestore db;

    // Collection name for EventUserLink
    private static final String EVENT_USER_LINK_COLLECTION = "EventUserLinks";

    public EventUserLinkDB() {
        db = FirebaseFirestore.getInstance();
    }

    public interface GetCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }

    public interface ActionCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    /**
     * Adds a new EventUserLink and returns the created EventUserLink on success.
     * This method creates and retrieves the new EventUserLink in one step.
     *
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
     *
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
     *
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
     *
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
}
