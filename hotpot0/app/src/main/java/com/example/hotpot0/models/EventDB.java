package com.example.hotpot0.models;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * EventDB handles all Firestore operations for Event.
 * It can add, delete, update, and fetch Event data.
 */
public class EventDB {
    private final FirebaseFirestore db;
    private static final String EVENT_COLLECTION = "Events";

    public EventDB() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Generic callback interface for asynchronous Firestore operations.
     * @param <T> The result type.
     */
    public interface GetCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }

    /**
     * Adds a new event to Firestore and generates a unique event ID.
     * @param event Event object to add.
     * @param callback Callback for success/failure.
     */
    public void addEvent(@NonNull Event event, @NonNull GetCallback<Event> callback) {
        generateNewID(new GetCallback<Integer>() {
            @Override
            public void onSuccess(Integer newEventID) {
                event.setEventID(newEventID);
                if (event.getLinkIDs() == null) event.setLinkIDs(new ArrayList<>());
                if (event.getSampledIDs() == null) event.setSampledIDs(new ArrayList<>());

                db.collection(EVENT_COLLECTION).document(String.valueOf(newEventID))
                        .set(event)
                        .addOnSuccessListener(aVoid -> callback.onSuccess(event))
                        .addOnFailureListener(callback::onFailure);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    /**
     * Updates an existing event in Firestore.
     * @param event Event object with updated fields.
     * @param callback Callback for success/failure.
     */
    public void updateEvent(@NonNull Event event, @NonNull ProfileDB.GetCallback<Void> callback) {
        if (event.getEventID() == null) {
            callback.onFailure(new IllegalArgumentException("Event ID cannot be null for update."));
            return;
        }

        DocumentReference eventRef = db.collection(EVENT_COLLECTION)
                .document(String.valueOf(event.getEventID()));
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("name", event.getName());
        updates.put("description", event.getDescription());
        updates.put("guidelines", event.getGuidelines());
        updates.put("location", event.getLocation());
        updates.put("time", event.getTime());
        updates.put("date", event.getDate());
        updates.put("duration", event.getDuration());
        updates.put("capacity", event.getCapacity());
        updates.put("price", event.getPrice());
        updates.put("registration_period", event.getRegistration_period());
        updates.put("imageURL", event.getImageURL());
        updates.put("geolocationRequired", event.getGeolocationRequired());
        updates.put("isEventActive", event.getIsEventActive());

        eventRef.update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Deletes an event from Firestore.
     * @param eventID ID of the event to delete.
     * @param callback Callback for success/failure.
     */
    public void deleteEvent(@NonNull Integer eventID, @NonNull ProfileDB.ActionCallback callback) {
        DocumentReference eventRef = db.collection(EVENT_COLLECTION).document(String.valueOf(eventID));
        eventRef.delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Generates a new unique event ID based on existing documents.
     * @param callback Callback returning the new ID or error.
     */
    private void generateNewID(@NonNull GetCallback<Integer> callback) {
        db.collection(EVENT_COLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int maxID = 0;
                    for (var document : queryDocumentSnapshots.getDocuments()) {
                        try {
                            int currentID = Integer.parseInt(document.getId());
                            if (currentID > maxID) maxID = currentID;
                        } catch (NumberFormatException e) {
                            // Skip non-integer IDs
                        }
                    }
                    callback.onSuccess(maxID + 1);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Fetches an event by its ID.
     */
    public void getEventByID(@NonNull Integer eventID, @NonNull GetCallback<Event> callback) {
        db.collection(EVENT_COLLECTION)
                .document(String.valueOf(eventID))
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Event event = documentSnapshot.toObject(Event.class);
                        callback.onSuccess(event);
                    } else {
                        callback.onFailure(new IllegalArgumentException("Event not found"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Fetches all active events.
     */
    public void getAllActiveEvents(@NonNull GetCallback<java.util.List<Event>> callback) {
        db.collection(EVENT_COLLECTION)
                .whereEqualTo("isEventActive", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    java.util.List<Event> events = new java.util.ArrayList<>();
                    for (var doc : querySnapshot.getDocuments()) {
                        events.add(doc.toObject(Event.class));
                    }
                    callback.onSuccess(events);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Fetches all events, regardless of status.
     */
    public void getAllEvents(@NonNull GetCallback<java.util.List<Event>> callback) {
        db.collection(EVENT_COLLECTION)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    java.util.List<Event> events = new java.util.ArrayList<>();
                    for (var doc : querySnapshot.getDocuments()) {
                        events.add(doc.toObject(Event.class));
                    }
                    callback.onSuccess(events);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Adds a linkID to an event's linkIDs array.
     */
    public void addLinkIDToEvent(@NonNull Event event, @NonNull String linkID, @NonNull GetCallback<Void> callback) {
        if (event.getLinkIDs() == null) event.setLinkIDs(new ArrayList<>());
        if (!event.getLinkIDs().contains(linkID)) event.getLinkIDs().add(linkID);

        DocumentReference eventRef = db.collection(EVENT_COLLECTION)
                .document(String.valueOf(event.getEventID()));
        eventRef.update("linkIDs", FieldValue.arrayUnion(linkID))
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Removes a linkID from an event's linkIDs array.
     */
    public void removeLinkIDFromEvent(@NonNull Event event, @NonNull String linkID, @NonNull GetCallback<Void> callback) {
        if (event.getLinkIDs() != null) event.getLinkIDs().remove(linkID);

        DocumentReference eventRef = db.collection(EVENT_COLLECTION)
                .document(String.valueOf(event.getEventID()));
        eventRef.update("linkIDs", FieldValue.arrayRemove(linkID))
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Adds a sampledID to an event's sampledIDs array.
     */
    public void addSampledIDToEvent(@NonNull Event event, @NonNull String sampledID, @NonNull GetCallback<Void> callback) {
        if (event.getSampledIDs() == null) event.setSampledIDs(new ArrayList<>());
        if (!event.getSampledIDs().contains(sampledID)) event.getSampledIDs().add(sampledID);

        DocumentReference eventRef = db.collection(EVENT_COLLECTION)
                .document(String.valueOf(event.getEventID()));
        eventRef.update("sampledIDs", FieldValue.arrayUnion(sampledID))
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Removes a sampledID from an event's sampledIDs array.
     */
    public void removeSampledIDFromEvent(@NonNull Event event, @NonNull String sampledID, @NonNull GetCallback<Void> callback) {
        if (event.getSampledIDs() != null) event.getSampledIDs().remove(sampledID);

        DocumentReference eventRef = db.collection(EVENT_COLLECTION)
                .document(String.valueOf(event.getEventID()));
        eventRef.update("sampledIDs", FieldValue.arrayRemove(sampledID))
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }
}