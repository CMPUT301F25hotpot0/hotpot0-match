package com.example.hotpot0.models;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Handles all Firestore database operations related to {@link Event}.
 * <p>
 * This class encapsulates methods to add, update, delete, and fetch events,
 * as well as to modify participant-related arrays (linkIDs, sampledIDs, cancelledIDs).
 * </p>
 * <p>
 * Each method is asynchronous and uses callback interfaces to return results
 * or report failures to the caller.
 * </p>
 */
public class EventDB {

    // EventDB Attributes
    private final FirebaseFirestore db;
    private static final String EVENT_COLLECTION = "Events";
    private EventUserLinkDB eventUserLinkDB = new EventUserLinkDB();

    /** Initializes the EventDB instance and Firestore connection. */
    public EventDB() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Generic callback interface for asynchronous Firestore operations.
     * @param <T> The expected result type.
     */
    public interface GetCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }

    // Utility Methods
    // ===============

    /**
     * Adds a new event to Firestore and generates a unique event ID.
     * @param event Event object to add.
     * @param callback Callback for success/failure.
     */
    public void addEvent(@NonNull Event event, @NonNull GetCallback<Event> callback) {
        generateNewID(new GetCallback<Integer>() {
            @Override
            public void onSuccess(Integer newEventID) {
                // Set the generated ID
                event.setEventID(newEventID);

                // Generate QR value right here
                event.setQrValue("event:" + newEventID);

                // Initialize participant arrays if null
                if (event.getLinkIDs() == null) event.setLinkIDs(new ArrayList<>());
                if (event.getSampledIDs() == null) event.setSampledIDs(new ArrayList<>());
                if (event.getCancelledIDs() == null) event.setCancelledIDs(new ArrayList<>());

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
        updates.put("organizerID", event.getOrganizerID());
        updates.put("name", event.getName());
        updates.put("description", event.getDescription());
        updates.put("guidelines", event.getGuidelines());
        updates.put("location", event.getLocation());
        updates.put("time", event.getTime());
        updates.put("startDate", event.getStartDate());
        updates.put("endDate", event.getEndDate());
        updates.put("duration", event.getDuration());
        updates.put("regStartDate", event.getRegistrationStart());
        updates.put("regEndDate", event.getRegistrationEnd());

        updates.put("capacity", event.getCapacity());
        updates.put("price", event.getPrice());
        updates.put("waitingListCapacity", event.getWaitingListCapacity());

        updates.put("imageURL", event.getImageURL());
        updates.put("qrValue", event.getQrValue());
        updates.put("geolocationRequired", event.getGeolocationRequired());
        updates.put("isEventActive", event.getIsEventActive());

        updates.put("linkIDs", event.getLinkIDs());
        updates.put("sampledIDs", event.getSampledIDs());
        updates.put("cancelledIDs", event.getCancelledIDs());

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
     * Fetches a single event document by its unique ID.
     * @param eventID  the ID of the event to fetch
     * @param callback callback returning the {@code Event} object or an error
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
     * @param callback callback returning the list of active {@code Event} objects
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
     * @param callback callback returning all event documents
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
     * @param event    the event to update
     * @param linkID   the participant’s link ID to add
     * @param callback callback indicating completion status
     */
    public void addLinkIDToEvent(@NonNull Event event, @NonNull String linkID, @NonNull GetCallback<Void> callback) {
        if (event.getLinkIDs() == null) event.setLinkIDs(new ArrayList<>());
        if (!event.getLinkIDs().contains(linkID)) event.addLinkID(linkID);

        DocumentReference eventRef = db.collection(EVENT_COLLECTION)
                .document(String.valueOf(event.getEventID()));
        eventRef.update("linkIDs", FieldValue.arrayUnion(linkID))
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Removes a linkID from an event's linkIDs array.
     * @param event    the event to update
     * @param linkID   the link ID to remove
     * @param callback callback indicating completion status
     */
    public void removeLinkIDFromEvent(@NonNull Event event, @NonNull String linkID, @NonNull GetCallback<Void> callback) {
        if (event.getLinkIDs() != null) event.removeLinkID(linkID);

        DocumentReference eventRef = db.collection(EVENT_COLLECTION)
                .document(String.valueOf(event.getEventID()));
        eventRef.update("linkIDs", FieldValue.arrayRemove(linkID))
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Adds a sampledID to an event's sampledIDs array.
     * @param event      the event to update
     * @param sampledID  the sampled participant’s ID
     * @param callback   callback indicating completion status
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
     * @param event      the event to update
     * @param sampledID  the sampled participant ID to remove
     * @param callback   callback indicating completion status
     */
    public void removeSampledIDFromEvent(@NonNull Event event, @NonNull String sampledID, @NonNull GetCallback<Void> callback) {
        if (event.getSampledIDs() != null) event.getSampledIDs().remove(sampledID);

        DocumentReference eventRef = db.collection(EVENT_COLLECTION)
                .document(String.valueOf(event.getEventID()));
        eventRef.update("sampledIDs", FieldValue.arrayRemove(sampledID))
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Adds a cancelled participant ID to the event’s cancelledID array .
     * @param event       the event to update
     * @param cancelledID the cancelled participant ID to add
     * @param callback    callback indicating completion status
     */
    public void addCancelledIDToEvent(@NonNull Event event, @NonNull String cancelledID, @NonNull GetCallback<Void> callback) {
        if (event.getCancelledIDs() == null) event.setCancelledIDs(new ArrayList<>());
        if (!event.getCancelledIDs().contains(cancelledID))
            event.getCancelledIDs().add(cancelledID);

        DocumentReference eventRef = db.collection(EVENT_COLLECTION)
                .document(String.valueOf(event.getEventID()));
        eventRef.update("cancelledIDs", FieldValue.arrayUnion(cancelledID))
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Removes a cancelled participant ID from the event’s cancelledIDs array.
     * @param event       the event to update
     * @param cancelledID the cancelled participant ID to remove
     * @param callback    callback indicating completion status
     */
    public void removeCancelledIDFromEvent(@NonNull Event event, @NonNull String cancelledID, @NonNull GetCallback<Void> callback) {
        if (event.getCancelledIDs() != null) event.getCancelledIDs().remove(cancelledID);

        DocumentReference eventRef = db.collection(EVENT_COLLECTION)
                .document(String.valueOf(event.getEventID()));
        eventRef.update("cancelledIDs", FieldValue.arrayRemove(cancelledID))
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Updates the event’s active status in Firestore.
     * @param event    the event to update
     * @param isActive true to mark the event active, false otherwise
     * @param callback callback indicating completion status
     */
    public void setEventActiveStatus(@NonNull Event event, boolean isActive, @NonNull GetCallback<Void> callback) {
        event.setIsEventActive(isActive);

        DocumentReference eventRef = db.collection(EVENT_COLLECTION)
                .document(String.valueOf(event.getEventID()));
        eventRef.update("isEventActive", isActive)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Updates the event’s geolocation requirement flag in Firestore.
     * @param event      the event to update
     * @param isRequired true if geolocation verification is required
     * @param callback   callback indicating completion status
     */
    public void setGeolocationRequired(@NonNull Event event, boolean isRequired, @NonNull GetCallback<Void> callback) {
        event.setGeolocationRequired(isRequired);

        DocumentReference eventRef = db.collection(EVENT_COLLECTION)
                .document(String.valueOf(event.getEventID()));
        eventRef.update("geolocationRequired", isRequired)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Randomly samples new participants for an event based on its waitlist.
     * <p>
     * This method first retrieves the list of users on the waitlist using
     * {@link EventUserLinkDB#getWaitListUsers(List, EventUserLinkDB.GetCallback)}.
     * Once the waitlist is obtained, it calls {@link Event#sampleParticipants(List)}
     * to randomly select participants up to the event's capacity and updates
     * Firestore with the new {@code sampledIDs}.
     * </p>
     * @param event    the Event object for which participants are being sampled
     * @param callback the callback invoked upon success or failure;
     *                 {@code onSuccess} returns the list of sampled participant IDs
     */
    public void sampleEvent(@NonNull Event event, @NonNull GetCallback<List<String>> callback) {
        List<String> allLinkIDs = event.getLinkIDs();

        eventUserLinkDB.getWaitListUsers(allLinkIDs, new EventUserLinkDB.GetCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> waitListLinkIDs) {
                // Sample participants after waitlist is fetched
                ArrayList<String> sampled = event.sampleParticipants(waitListLinkIDs);

                DocumentReference eventRef = db.collection(EVENT_COLLECTION)
                        .document(String.valueOf(event.getEventID()));

                eventRef.update("sampledIDs", sampled)
                        .addOnSuccessListener(aVoid -> callback.onSuccess(sampled))
                        .addOnFailureListener(callback::onFailure);

                for (String linkID : allLinkIDs) {
                    String userID = linkID.split("_")[1]; // Extract userID from linkID
                    if (sampled.contains(linkID)) {
                        Status status = new Status();
                        status.setStatus("Sampled");
                        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        Date date = new Date();
                        String now = formatter.format(date);
                        Notification notif = new Notification(now, status, true, event.getName());
                        eventUserLinkDB.addSampledNotification(linkID, notif, new EventUserLinkDB.ActionCallback() {
                            @Override
                            public void onSuccess() {
                                // Notification added successfully
                            }

                            @Override
                            public void onFailure(Exception e) {
                                // Handle failure to add notification
                            }
                        });
                    } else if (event.getOrganizerID().toString().equals(userID)) {
                        Log.d("EventDB", "User is organizer, no notification sent for linkID: " + linkID);
                    } else {
                        // Else inWaitList
                        Status status = new Status();
                        status.setStatus("inWaitList");
                        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        Date date = new Date();
                        String now = formatter.format(date);
                        Notification notif = new Notification(now, status, event.getName());
                        eventUserLinkDB.addWaitlistNotification(linkID, notif, new EventUserLinkDB.ActionCallback() {
                            @Override
                            public void onSuccess() {
                                // Notification added successfully
                            }

                            @Override
                            public void onFailure(Exception e) {
                                // Handle failure to add notification
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    /**
     * Fills empty sampled participant slots for an event using its waitlist.
     * Updates Firestore with the newly added sampled participants.
     * @param event    the event to update
     * @param callback callback returning the newly sampled participant IDs
     */
    public void fillEmptySampledSpots(@NonNull Event event, @NonNull GetCallback<List<String>> callback) {
        List<String> allLinkIDs = event.getLinkIDs();

        eventUserLinkDB.getWaitListUsers(allLinkIDs, new EventUserLinkDB.GetCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> waitListLinkIDs) {
                // Fill empty spots after waitlist is fetched
                ArrayList<String> newlySampled = event.fillSampledParticipants(waitListLinkIDs);

                DocumentReference eventRef = db.collection(EVENT_COLLECTION)
                        .document(String.valueOf(event.getEventID()));

                eventRef.update("sampledIDs", event.getSampledIDs())
                        .addOnSuccessListener(aVoid -> callback.onSuccess(newlySampled))
                        .addOnFailureListener(callback::onFailure);

                for (String linkID : newlySampled) {
                    Status status = new Status();
                    status.setStatus("Sampled");
                    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    Date date = new Date();
                    String now = formatter.format(date);
                    Notification notif = new Notification(now, status, true, event.getName());
                    eventUserLinkDB.addSampledNotification(linkID, notif, new EventUserLinkDB.ActionCallback() {
                        @Override
                        public void onSuccess() {
                            // Notification added successfully
                        }

                        @Override
                        public void onFailure(Exception e) {
                            // Handle failure to add notification
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void updateEventImageURL(Event event, String imageURL, GetCallback<Void> callback) {
        db.collection(EVENT_COLLECTION)
                .document(String.valueOf(event.getEventID()))
                .update("imageURL", imageURL)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    // Firestore lookup by qrValue
    public void getEventByQrValue(String qrValue, GetCallback<Event> callback) {

        db.collection("Events")
                .whereEqualTo("qrValue", qrValue)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        // Indicate NOT FOUND by returning null
                        callback.onSuccess(null);
                        return;
                    }

                    Event event = snapshot.getDocuments()
                            .get(0)
                            .toObject(Event.class);

                    callback.onSuccess(event);
                })
                .addOnFailureListener(callback::onFailure);
    }
}