package com.example.hotpot0.section2.controllers;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.hotpot0.models.Event;
import com.example.hotpot0.models.EventDB;
import com.example.hotpot0.models.EventUserLink;
import com.example.hotpot0.models.EventUserLinkDB;
import com.example.hotpot0.models.Notification;
import com.example.hotpot0.models.ProfileDB;
import com.example.hotpot0.models.Status;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Handles user actions related to events such as joining or leaving waitlists,
 * accepting or declining invitations, and managing event-user relationships.
 * <p>
 * This class provides asynchronous methods that interact with
 * {@link EventDB}, {@link ProfileDB}, and {@link EventUserLinkDB}
 * to perform these operations using callback-based responses.
 * </p>
 */
public class EventActionHandler {
    private ProfileDB profileDB;
    private EventDB eventDB;
    private EventUserLinkDB eventUserLinkDB;

    /**
     * Constructs a new {@code EventActionHandler}.
     * <p>
     * Initializes the database handler instances used for event, profile,
     * and event-user link operations.
     * </p>
     */
    public EventActionHandler(){
        profileDB = new ProfileDB();
        eventDB = new EventDB();
        eventUserLinkDB = new EventUserLinkDB();
    }

    /**
     * Generates a unique link ID combining the user ID and event ID.
     * <p>
     * The link ID is used as a unique key in the {@link EventUserLinkDB}.
     * </p>
     *
     * @param userID  the user’s ID
     * @param eventID the event’s ID
     * @return a string identifier in the format "eventID_userID"
     */
    private String generateLinkID(Integer userID, Integer eventID) {
        // Assuming linkID is created by combining userID and eventID as a string
        return eventID + "_" + userID;  // You can adjust this structure as per your requirements
    }

    /**
     * Allows a user to join an event’s waitlist if they are not already affiliated
     * with the event and the waitlist is not full.
     *
     * @param userID   the user’s ID
     * @param eventID  the event’s ID
     * @param callback callback invoked upon completion:
     *                 <ul>
     *                     <li>{@code onSuccess(0)} – successfully joined waitlist</li>
     *                     <li>{@code onSuccess(1)} – user already affiliated</li>
     *                     <li>{@code onSuccess(2)} – waitlist is full</li>
     *                     <li>{@code onFailure(e)} – failure during database operation</li>
     *                 </ul>
     */
    public void joinWaitList(Integer userID, Integer eventID, Double latitude, Double longitude, ProfileDB.GetCallback<Integer> callback) {
        // Construct the linkID
        String linkID = generateLinkID(userID, eventID);

        // Fetch the user's EventUserLink from Firestore
        EventUserLinkDB eventUserLinkDB = new EventUserLinkDB();

        eventUserLinkDB.getEventUserLinkByID(linkID, new EventUserLinkDB.GetCallback<EventUserLink>() {
            @Override
            public void onSuccess(EventUserLink eventUserLink) {
                // If an EventUserLink is found, it means the user already has an affiliation with the event.
                // So we cannot allow them to join the waitlist again.
                // Indicate that the user is already affiliated (hence failure to join waitlist)
                callback.onSuccess(1); // Failure: User already has an affiliation with the event
            }

            @Override
            public void onFailure(Exception e) {
                eventDB.getEventByID(eventID, new EventDB.GetCallback<Event>() {
                    @Override
                    public void onSuccess(Event eventObj) {
                        int capacity = eventObj.getWaitingListCapacity();
                        int currentCount = eventObj.getTotalWaitlist();
                        if (currentCount < capacity) {
                            // There is space in the waitlist, proceed with adding the user
                            // If no EventUserLink was found, the user can join the waitlist.
                            // Create a new EventUserLink for the user with the default "inWaitList" status
                            EventUserLink newEventUserLink = new EventUserLink(userID, eventID, latitude, longitude);

                            // Add the new EventUserLink to Firestore
                            eventUserLinkDB.addEventUserLink(newEventUserLink, new EventUserLinkDB.GetCallback<EventUserLink>() {
                                @Override
                                public void onSuccess(EventUserLink eventUserLink) {
                                    // Successfully created a new EventUserLink, user is now on the waitlist
                                    profileDB.getUserByID(userID, new ProfileDB.GetCallback<com.example.hotpot0.models.UserProfile>() {
                                        @Override
                                        public void onSuccess(com.example.hotpot0.models.UserProfile userProfile) {
                                            // Update the user's profile with the new linkID
                                            profileDB.addLinkIDToUser(userProfile, eventUserLink.getLinkID(), new ProfileDB.GetCallback<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    // Successfully updated the user's profile
                                                    eventDB.getEventByID(eventID, new EventDB.GetCallback<com.example.hotpot0.models.Event>() {
                                                        @Override
                                                        public void onSuccess(com.example.hotpot0.models.Event eventObj) {
                                                            // Update the event with the new linkID
                                                            eventDB.addLinkIDToEvent(eventObj, eventUserLink.getLinkID(), new EventDB.GetCallback<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    // Successfully updated the event
                                                                    callback.onSuccess(0); // Success: User added to waitlist
                                                                }

                                                                @Override
                                                                public void onFailure(Exception e) {
                                                                    // Failed to update the event
                                                                    callback.onFailure(e); // Failure to update event
                                                                }
                                                            });
                                                        }

                                                        @Override
                                                        public void onFailure(Exception e) {
                                                            // Failed to retrieve the event
                                                            callback.onFailure(e); // Failure to retrieve event
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onFailure(Exception e) {
                                                    // Failed to update the user's profile
                                                    callback.onFailure(e); // Failure to update user profile
                                                }
                                            });
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            // Failed to retrieve the user's profile
                                            callback.onFailure(e); // Failure to retrieve user profile
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    // Failed to create the new EventUserLink in Firestore
                                    callback.onSuccess(1); // Failure to add user to waitlist
                                }
                            });
                        } else {
                            // Waitlist is full, cannot add the user
                            callback.onSuccess(2); // Failure: Waitlist is full
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // Failed to retrieve the event
                        callback.onFailure(e); // Failure to retrieve event
                    }
                });
            }
        });
    }

    /**
     * Removes a user from an event’s waitlist.
     * <p>
     * The method deletes the associated {@link EventUserLink} and removes
     * the link reference from both the user’s profile and the event record.
     * </p>
     *
     * @param userID   the user’s ID
     * @param eventID  the event’s ID
     * @param callback callback invoked upon completion:
     *                 <ul>
     *                     <li>{@code onSuccess(0)} – successfully removed</li>
     *                     <li>{@code onSuccess(1)} – user not on waitlist</li>
     *                     <li>{@code onFailure(e)} – failure during database operation</li>
     *                 </ul>
     */
    public void leaveWaitList(Integer userID, Integer eventID, ProfileDB.GetCallback<Integer> callback) {
        // Construct the linkID
        String linkID = generateLinkID(userID, eventID);

        // Fetch the user's EventUserLink from Firestore
        EventUserLinkDB eventUserLinkDB = new EventUserLinkDB();

        eventUserLinkDB.getEventUserLinkByID(linkID, new EventUserLinkDB.GetCallback<EventUserLink>() {
            @Override
            public void onSuccess(EventUserLink eventUserLink) {
                // Check if the current status is "inWaitList"
                if ("inWaitList".equals(eventUserLink.getStatus())) {
                    // The user is on the waitlist, proceed with removing them from it
                    eventUserLinkDB.deleteEventUserLink(linkID, new EventUserLinkDB.ActionCallback() {
                        @Override
                        public void onSuccess() {
                            // Successfully removed the user from the waitlist
                            profileDB.getUserByID(userID, new ProfileDB.GetCallback<com.example.hotpot0.models.UserProfile>() {
                                @Override
                                public void onSuccess(com.example.hotpot0.models.UserProfile userProfile) {
                                    // Update the user's profile to remove the linkID
                                    profileDB.removeLinkIDFromUser(userProfile, linkID, new ProfileDB.GetCallback<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Successfully updated the user's profile
                                            eventDB.getEventByID(eventID, new EventDB.GetCallback<com.example.hotpot0.models.Event>() {
                                                @Override
                                                public void onSuccess(com.example.hotpot0.models.Event eventObj) {
                                                    // Update the event to remove the linkID
                                                    eventDB.removeLinkIDFromEvent(eventObj, linkID, new EventDB.GetCallback<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            // Successfully updated the event
                                                            callback.onSuccess(0); // Success: User removed from waitlist
                                                        }

                                                        @Override
                                                        public void onFailure(Exception e) {
                                                            // Failed to update the event
                                                            callback.onFailure(e); // Failure to update event
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onFailure(Exception e) {
                                                    // Failed to retrieve the event
                                                    callback.onFailure(e); // Failure to retrieve event
                                                }
                                            });
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            // Failed to update the user's profile
                                            callback.onFailure(e); // Failure to update user profile
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    // Failed to retrieve the user's profile
                                    callback.onFailure(e); // Failure to retrieve user profile
                                }
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            // Failed to remove the user from the waitlist
                            callback.onFailure(e); // Failure to remove user from waitlist
                        }
                    });
                } else {
                    // If the user is not on the waitlist, we can't allow them to leave
                    callback.onSuccess(1); // Failure: User not on the waitlist
                }
            }

            @Override
            public void onFailure(Exception e) {
                // If no EventUserLink is found, the user was never on the waitlist
                callback.onSuccess(1); // Failure: User not found or never on waitlist
            }
        });
    }

    /**
     * Allows a user to accept an invitation to an event.
     * <p>
     * Only users with the "Sampled" status can accept an invitation.
     * Upon acceptance, the status is updated to "Accepted".
     * </p>
     *
     * @param userID   the user’s ID
     * @param eventID  the event’s ID
     * @param callback callback invoked upon completion:
     *                 <ul>
     *                     <li>{@code onSuccess(0)} – successfully accepted invite</li>
     *                     <li>{@code onSuccess(1)} – user not eligible</li>
     *                     <li>{@code onFailure(e)} – failure during update</li>
     *                 </ul>
     */
    public void acceptInvite(Integer userID, Integer eventID, ProfileDB.GetCallback<Integer> callback) {
        // Construct the linkID
        String linkID = generateLinkID(userID, eventID);

        // Fetch the user's EventUserLink from Firestore
        EventUserLinkDB eventUserLinkDB = new EventUserLinkDB();

        eventUserLinkDB.getEventUserLinkByID(linkID, new EventUserLinkDB.GetCallback<EventUserLink>() {
            @Override
            public void onSuccess(EventUserLink eventUserLink) {
                // Check if the user’s status is "Sampled"
                if ("Sampled".equals(eventUserLink.getStatus())) {
                    // Attempt to set the status to "Accepted"
                    eventUserLink.setStatus("Accepted"); // Directly set the status

                    // Now, update the EventUserLink in the database
                    eventUserLinkDB.updateEventUserLink(eventUserLink, new EventUserLinkDB.ActionCallback() {
                        @Override
                        public void onSuccess() {
                            // Successfully updated the status to "Accepted"
                            callback.onSuccess(0); // Success: Invite accepted
                        }

                        @Override
                        public void onFailure(Exception e) {
                            // Failed to update the status in the database
                            callback.onFailure(e); // Failure: Could not update status
                        }
                    });
                } else {
                    // If the user is not in the "Sampled" status, they cannot accept the invite
                    callback.onSuccess(1); // Failure: User is not sampled
                }
            }

            @Override
            public void onFailure(Exception e) {
                // If no EventUserLink is found, it means the user was never affiliated with the event
                callback.onSuccess(1); // Failure: User not found
            }
        });
    }

    /**
     * Allows a user to decline an invitation to an event.
     * <p>
     * Only users with the "Sampled" status can decline an invitation.
     * Upon declining, the status is updated to "Declined".
     * </p>
     *
     * @param userID   the user’s ID
     * @param eventID  the event’s ID
     * @param callback callback invoked upon completion:
     *                 <ul>
     *                     <li>{@code onSuccess(0)} – successfully declined invite</li>
     *                     <li>{@code onSuccess(1)} – user not eligible</li>
     *                     <li>{@code onFailure(e)} – failure during update</li>
     *                 </ul>
     */
    public void declineInvite(Integer userID, Integer eventID, ProfileDB.GetCallback<Integer> callback) {
        // Construct the linkID
        String linkID = generateLinkID(userID, eventID);

        // Fetch the user's EventUserLink from Firestore
        EventUserLinkDB eventUserLinkDB = new EventUserLinkDB();

        eventUserLinkDB.getEventUserLinkByID(linkID, new EventUserLinkDB.GetCallback<EventUserLink>() {
            @Override
            public void onSuccess(EventUserLink eventUserLink) {
                // Check if the user’s status is "Sampled"
                if ("Sampled".equals(eventUserLink.getStatus())) {
                    // Attempt to set the status to "Declined"
                    eventUserLink.setStatus("Declined"); // Directly set the status to "Declined"

                    // Now, update the EventUserLink in the database
                    eventUserLinkDB.updateEventUserLink(eventUserLink, new EventUserLinkDB.ActionCallback() {
                        @Override
                        public void onSuccess() {
                            // Successfully updated the status to "Declined"
                            callback.onSuccess(0); // Success: Invite declined
                        }

                        @Override
                        public void onFailure(Exception e) {
                            // Failed to update the status in the database
                            callback.onFailure(e); // Failure: Could not update status
                        }
                    });
                } else {
                    // If the user is not in the "Sampled" status, they cannot decline the invite
                    callback.onSuccess(1); // Failure: User is not sampled
                }
            }

            @Override
            public void onFailure(Exception e) {
                // If no EventUserLink is found, it means the user was never affiliated with the event
                callback.onSuccess(1); // Failure: User not found
            }
        });
    }
    /**
     * Allows an organizer to cancel their participation in an event.
     * <p>
     * The user's status is updated to "Cancelled", and the event record
     * is updated to reflect the cancellation.
     * </p>
     *
     * @param userID   the user’s ID
     * @param event    the event object
     * @param callback callback invoked upon completion:
     *                 <ul>
     *                     <li>{@code onSuccess(0)} – successfully cancelled participation</li>
     *                     <li>{@code onSuccess(1)} – user not affiliated</li>
     *                     <li>{@code onFailure(e)} – failure during update</li>
     *                 </ul>
     */
    public void cancelUser(Event event, Integer userID, ProfileDB.GetCallback<Integer> callback) {
        // Construct the linkID
        String linkID = generateLinkID(userID, event.getEventID());

        // Fetch the user's EventUserLink from Firestore
        EventUserLinkDB eventUserLinkDB = new EventUserLinkDB();

        eventUserLinkDB.getEventUserLinkByID(linkID, new EventUserLinkDB.GetCallback<EventUserLink>() {
            @Override
            public void onSuccess(EventUserLink eventUserLink) {
                // Attempt to set the status to "Cancelled"
                eventUserLink.setStatus("Cancelled"); // Directly set the status to "Cancelled"

                Status status = new Status();
                status.setStatus(eventUserLink.getStatus());
                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = new Date();
                String now = formatter.format(date);
                Notification notif = new Notification(now, status, event.getName(), event.getEventID());

                eventUserLink.addNotification(notif);

                // Now, update the EventUserLink in the database
                eventUserLinkDB.updateEventUserLink(eventUserLink, new EventUserLinkDB.ActionCallback() {
                    @Override
                    public void onSuccess() {
                        // Successfully updated the status to "Cancelled"
                        eventDB.addCancelledIDToEvent(event, linkID, new EventDB.GetCallback<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                eventDB.removeSampledIDFromEvent(event, linkID.toString(), new EventDB.GetCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        callback.onSuccess(0); // Success: User cancelled
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        // Failed to update the event by removing sampled ID
                                        callback.onFailure(e); // Failure to update event
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                // Failed to update the event with the cancelled ID
                                callback.onFailure(e); // Failure to update event
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // Failed to update the status in the database
                        callback.onFailure(e); // Failure: Could not update status
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                // If no EventUserLink is found, it means the user was never affiliated with the event
                callback.onSuccess(1); // Failure: User not found
            }
        });
    }

    public void sendCustomNotification(String message, String status, List<String> linkIDs, Event event, @NonNull EventUserLinkDB.ActionCallback callback) {
        for (String linkID : linkIDs) {
            eventUserLinkDB.getEventUserLinkByID(linkID, new EventUserLinkDB.GetCallback<EventUserLink>() {
                @Override
                public void onSuccess(EventUserLink eventUserLink) {
                    Status statusObj = new Status();
                    statusObj.setStatus(status);
                    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date date = new Date();
                    String now = formatter.format(date);
                    Notification notif = new Notification(now, statusObj, message, event.getName(), event.getEventID(),true);
                    eventUserLink.addNotification(notif);
                    eventUserLinkDB.updateEventUserLink(eventUserLink, new EventUserLinkDB.ActionCallback() {
                        @Override
                        public void onSuccess() {
                            // Notification added successfully
                            callback.onSuccess();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            // Failed to update EventUserLink with notification
                            callback.onFailure(e);
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    // Failed to retrieve EventUserLink
                    callback.onFailure(e);
                }
            });
        }
    }
}