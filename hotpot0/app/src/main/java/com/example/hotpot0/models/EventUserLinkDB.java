package com.example.hotpot0.models;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;

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
        generateNewID("event_user_links", new ProfileDB.GetCallback<Integer>() {
            @Override
            public void onSuccess(Integer newLinkID) {
                eventUserLink.setLinkID(newLinkID);

                // Prepare the data to be added to Firestore
                DocumentReference linkRef = db.collection(EVENT_USER_LINK_COLLECTION).document(String.valueOf(newLinkID));

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
        Integer linkID = eventUserLink.getLinkID();
        if (linkID == null) {
            callback.onFailure(new IllegalArgumentException("Link ID cannot be null for update"));
            return;
        }

        DocumentReference linkRef = db.collection(EVENT_USER_LINK_COLLECTION).document(String.valueOf(linkID));

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
    public void deleteEventUserLink(int linkID, @NonNull ProfileDB.ActionCallback callback) {
        DocumentReference linkRef = db.collection(EVENT_USER_LINK_COLLECTION).document(String.valueOf(linkID));

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
    public void getEventUserLinkByID(int linkID, @NonNull ProfileDB.GetCallback<EventUserLink> callback) {
        DocumentReference linkRef = db.collection(EVENT_USER_LINK_COLLECTION).document(String.valueOf(linkID));

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
    private void generateNewID(@NonNull String type, @NonNull ProfileDB.GetCallback<Integer> callback) {
        // Similar to generateNewID in ProfileDB, but with "event_user_links" as the type
        DocumentReference counterRef = db.collection("Counters").document(type);

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
        }).addOnFailureListener(callback::onFailure);
    }
}
