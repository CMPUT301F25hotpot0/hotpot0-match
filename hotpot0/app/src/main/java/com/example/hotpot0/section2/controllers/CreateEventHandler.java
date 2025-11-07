package com.example.hotpot0.section2.controllers;

import android.content.Context;
import android.text.TextUtils;

import com.example.hotpot0.models.Event;
import com.example.hotpot0.models.EventDB;
import com.example.hotpot0.models.EventUserLink;
import com.example.hotpot0.models.EventUserLinkDB;
import com.example.hotpot0.models.ProfileDB;
import com.example.hotpot0.models.UserProfile;

import java.util.regex.Pattern;

public class CreateEventHandler {
    private final EventDB eventDB;
    private final Context context;
    private EventUserLinkDB eventUserLinkDB;
    private ProfileDB profileDB;

    public CreateEventHandler(Context context) {
        this.context = context;
        this.eventDB = new EventDB();
        this.eventUserLinkDB = new EventUserLinkDB();
        this.profileDB = new ProfileDB();
    }

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
                                profileDB.addLinkIDToUser(userProfile, linkResult.getLinkID(), new ProfileDB.GetCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Successfully updated organizer profile
                                        callback.onSuccess(result);  // Call success after all actions complete
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        // Log the error but do not fail the event creation
                                        e.printStackTrace();
                                        callback.onSuccess(result);  // Continue with success even if update fails
                                    }
                                });
                                eventDB.addLinkIDToEvent(event, linkResult.getLinkID(), new EventDB.GetCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Link ID added successfully
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
