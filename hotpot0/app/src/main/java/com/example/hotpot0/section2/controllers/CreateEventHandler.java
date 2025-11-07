package com.example.hotpot0.section2.controllers;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.hotpot0.models.Event;
import com.example.hotpot0.models.EventDB;
import com.example.hotpot0.models.EventUserLink;
import com.example.hotpot0.models.EventUserLinkDB;
import com.example.hotpot0.models.ProfileDB;
import com.example.hotpot0.models.UserProfile;

import java.util.regex.Pattern;

/**
 * Handles the creation of new events in the system.
 * <p>
 * This class performs validation on event details, creates a new {@link Event},
 * links it to the organizer through {@link EventUserLink}, and updates the organizer’s profile.
 * </p>
 */
public class CreateEventHandler {
    private final EventDB eventDB;
    private final Context context;
    private EventUserLinkDB eventUserLinkDB;
    private ProfileDB profileDB;

    /**
     * Constructs a new {@code CreateEventHandler}.
     *
     * @param context the Android context used for database access and UI feedback.
     */
    public CreateEventHandler(Context context) {
        this.context = context;
        this.eventDB = new EventDB();
        this.eventUserLinkDB = new EventUserLinkDB();
        this.profileDB = new ProfileDB();
    }

    /**
     * Creates a new event and associates it with the organizer.
     * <p>
     * This method performs validation on all required fields before creating the event.
     * If successful, it creates the event record, links it to the organizer as an "Organizer"
     * in {@link EventUserLinkDB}, and updates both the organizer’s profile and event record
     * with the link ID.
     * </p>
     *
     * @param organizerID          the ID of the user organizing the event
     * @param name                 the event name (required)
     * @param description          a brief description of the event (required)
     * @param guidelines           optional guidelines for participants
     * @param location             the location where the event will take place (required)
     * @param time                 the time of the event (required)
     * @param date                 the date of the event in YYYY-MM-DD format (required)
     * @param duration             the duration of the event (required)
     * @param capacity             the maximum number of participants allowed (must be positive)
     * @param price                the entry fee or price (cannot be negative)
     * @param registrationPeriod   the period during which registration is open (required)
     * @param imageURL             an optional image URL for the event
     * @param geolocationRequired  whether the event requires location access
     * @param callback             callback for success or failure
     */
    public void createEvent(Integer organizerID, String name,
                            String description,
                            String guidelines,
                            String location,
                            String time,
                            String date,
                            String duration,
                            Integer capacity,
                            Double price,
                            String registrationPeriod,
                            String imageURL,
                            Boolean geolocationRequired,
                            EventDB.GetCallback<Event> callback) {
        if (TextUtils.isEmpty(name)) {
            callback.onFailure(new IllegalArgumentException("Event name is required"));
            return;
        }
        if (TextUtils.isEmpty(description)) {
            callback.onFailure(new IllegalArgumentException("Event description is required"));
            return;
        }
        if (capacity == null || capacity <= 0) {
            callback.onFailure(new IllegalArgumentException("Capacity must be a positive integer"));
            return;
        }
        if (price == null || price < 0) {
            callback.onFailure(new IllegalArgumentException("Price cannot be negative"));
            return;
        }
        if (TextUtils.isEmpty(location)) {
            callback.onFailure(new IllegalArgumentException("Event location is required"));
            return;
        }
        if (TextUtils.isEmpty(date)) {
            callback.onFailure(new IllegalArgumentException("Event date is required"));
            return;
        }
        Pattern datePattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
        if (!datePattern.matcher(date).matches()) {
            callback.onFailure(new IllegalArgumentException("Event date must be in YYYY-MM-DD format"));
            return;
        }
        if (TextUtils.isEmpty(time)) {
            callback.onFailure(new IllegalArgumentException("Event time is required"));
            return;
        }
        if (TextUtils.isEmpty(duration)) {
            callback.onFailure(new IllegalArgumentException("Event duration is required"));
            return;
        }
        if (TextUtils.isEmpty(registrationPeriod)) {
            callback.onFailure(new IllegalArgumentException("Registration period is required"));
            return;
        }
        Event event = new Event(
                organizerID,
                name,
                description,
                guidelines,
                location,
                time,
                date,
                duration,
                capacity,
                price,
                registrationPeriod,
                imageURL,
                geolocationRequired
        );
        eventDB.addEvent(event, new EventDB.GetCallback<Event>() {
            @Override
            public void onSuccess(Event result) {
                int eventID = result.getEventID();

                EventUserLink link = new EventUserLink(organizerID, eventID, "Organizer");

                // Create the EventUserLink
                eventUserLinkDB.addEventUserLink(link, new EventUserLinkDB.GetCallback<EventUserLink>() {
                    @Override
                    public void onSuccess(EventUserLink linkResult) {
                        // Successfully created EventUserLink
                        profileDB.getUserByID(organizerID, new ProfileDB.GetCallback<UserProfile>() {
                            @Override
                            public void onSuccess(UserProfile userProfile) {
                                // Update the organizer's profile
                                // add log of success
                                Log.i("CreateEventHandler", "Successfully created EventUserLink with ID: " + linkResult.getLinkID());

                                profileDB.addLinkIDToUser(userProfile, linkResult.getLinkID(), new ProfileDB.GetCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        eventDB.addLinkIDToEvent(event, linkResult.getLinkID(), new EventDB.GetCallback<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                callback.onSuccess(result);
                                            }

                                            @Override
                                            public void onFailure(Exception e) {
                                                // Log the error but do not fail the event creation
                                                e.printStackTrace();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        // Log the error but do not fail the event creation
                                        e.printStackTrace();
                                        callback.onSuccess(result);  // Continue with success even if update fails
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                // Log the error but do not fail the event creation
                                Log.i("CreateEventHandler", "Failed to retrieve user profile for ID: " + organizerID);
                                e.printStackTrace();
                                callback.onSuccess(result);  // Continue with success even if user retrieval fails
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // Log the error but do not fail the event creation
                        e.printStackTrace();
                        callback.onSuccess(result);  // Continue with success even if link creation fails
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }
}
