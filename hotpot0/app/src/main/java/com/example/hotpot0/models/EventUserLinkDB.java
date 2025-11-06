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

    /**
     * Adds a new EventUserLink and returns the created EventUserLink on success.
     * This method creates and retrieves the new EventUserLink in one step.
     *
     * @param eventUserLink The EventUserLink object to be added (without linkID)
     * @param callback      Callback that returns the created EventUserLink
     */
    public void addEventUserLink(@NonNull EventUserLink eventUserLink, @NonNull ProfileDB.GetCallback<EventUserLink> callback) {
        // Generate the linkID (can be handled similarly to userID)
        generateNewID("event_user_links", new ProfileDB.GetCallback<String>() {
            @Override
            public void onSuccess(String newLinkID) {
                eventUserLink.setLinkID(newLinkID);

                // Prepare the data to be added to Firestore
                DocumentReference linkRef = db.collection(EVENT_USER_LINK_COLLECTION).document(newLinkID); // Use newLinkID as a String

                linkRef.set(eventUserLink)
                        .addOnSuccessListener(aVoid -> callback.onSuccess(eventUserLink))
                        .addOnFailureListener(callback::onFailure);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    /**
     * Updates an existing EventUserLink in Firestore.
     * Only updates status and notifications fields.
     *
     * @param eventUserLink The EventUserLink object with updated values (must have linkID)
     * @param callback      Callback to notify success or failure
     */
    public void updateEventUserLink(@NonNull EventUserLink eventUserLink, @NonNull ProfileDB.ActionCallback callback) {
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
    public void deleteEventUserLink(String linkID, @NonNull ProfileDB.ActionCallback callback) {  // LinkID is now a String
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
    public void getEventUserLinkByID(String linkID, @NonNull ProfileDB.GetCallback<EventUserLink> callback) {  // LinkID is now a String
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
     * Generates the next smallest available linkID for EventUserLink.
     * This function can be used to avoid conflicts when generating new IDs for EventUserLink.
     *
     * @param type     Type of link, like "event_user_links"
     * @param callback Callback to return the generated linkID asynchronously
     */
    private void generateNewID(@NonNull String type, @NonNull ProfileDB.GetCallback<String> callback) {
        // Similar to generateNewID in ProfileDB, but with "event_user_links" as the type
        DocumentReference counterRef = db.collection("Counters").document(type);

        counterRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                callback.onFailure(new Exception("Counters document for " + type + " does not exist"));
                return;
            }

            // Get the array of IDs already in use
            ArrayList<Long> idsInUseLong = (ArrayList<Long>) documentSnapshot.get("idsInUse");
            ArrayList<String> idsInUse = new ArrayList<>();
            if (idsInUseLong != null) {
                for (Long id : idsInUseLong) {
                    idsInUse.add(String.valueOf(id));  // Convert to String
                }
            }

            // Find the next smallest available integer, convert to string
            int nextID = 1; // start from 1
            Collections.sort(idsInUse);
            for (String id : idsInUse) {
                int idInt = Integer.parseInt(id);  // Convert back to int for comparison
                if (idInt == nextID) {
                    nextID++;
                } else if (idInt > nextID) {
                    break; // found a gap
                }
            }

            // Return the next available ID as a string
            callback.onSuccess(String.valueOf(nextID));
        }).addOnFailureListener(callback::onFailure);
    }
}
