package com.example.hotpot0.section2.controllers;

import com.example.hotpot0.models.EventDB;
import com.example.hotpot0.models.EventUserLink;
import com.example.hotpot0.models.EventUserLinkDB;
import com.example.hotpot0.models.InvalidStatusException;
import com.example.hotpot0.models.ProfileDB;

public class EventActionHandler {
    private ProfileDB profile;
    private EventDB event;

    public EventActionHandler(){
        profile = new ProfileDB();
        event = new EventDB();
    }

    public void joinWaitList(Integer userID, Integer eventID, ProfileDB.GetCallback<Integer> callback) {
        // Fetch the user's EventUserLink from Firestore
        EventUserLinkDB eventUserLinkDB = new EventUserLinkDB();

        eventUserLinkDB.getEventUserLinkByUserAndEvent(userID, eventID, new ProfileDB.GetCallback<EventUserLink>() {
            @Override
            public void onSuccess(EventUserLink eventUserLink) {
                // If an EventUserLink is found, it means the user already has an affiliation with the event.
                // So we cannot allow them to join the waitlist again.
                // Indicate that the user is already affiliated (hence failure to join waitlist)
                callback.onSuccess(1); // Failure: User already has an affiliation with the event
            }

            @Override
            public void onFailure(Exception e) {
                // If no EventUserLink was found, the user can join the waitlist.
                try {
                    // Create a new EventUserLink for the user with the default "inWaitList" status
                    EventUserLink newEventUserLink = new EventUserLink(userID, eventID);

                    // Add the new EventUserLink to Firestore
                    eventUserLinkDB.addEventUserLink(newEventUserLink, new ProfileDB.GetCallback<EventUserLink>() {
                        @Override
                        public void onSuccess(EventUserLink eventUserLink) {
                            // Successfully created a new EventUserLink, user is now on the waitlist
                            callback.onSuccess(0); // Success: User has been added to the waitlist
                        }

                        @Override
                        public void onFailure(Exception e) {
                            // Failed to create the new EventUserLink in Firestore
                            callback.onSuccess(1); // Failure to add user to waitlist
                        }
                    });
                } catch (InvalidStatusException statusException) {
                    // If there's an error creating the EventUserLink with the default status
                    callback.onSuccess(1); // Failure: Invalid status
                }
            }
        });
    }

    public void leaveWaitList(Integer userID, Integer eventID, ProfileDB.GetCallback<Integer> callback) {
        // Fetch the user's EventUserLink from Firestore
        EventUserLinkDB eventUserLinkDB = new EventUserLinkDB();

        eventUserLinkDB.getEventUserLinkByUserAndEvent(userID, eventID, new ProfileDB.GetCallback<EventUserLink>() {
            @Override
            public void onSuccess(EventUserLink eventUserLink) {
                // Check if the current status is "inWaitList"
                if ("inWaitList".equals(eventUserLink.getStatus())) {
                    // The user is on the waitlist, proceed with removing them from it
                    eventUserLinkDB.deleteEventUserLink(eventUserLink.getLinkID(), new ProfileDB.ActionCallback() {
                        @Override
                        public void onSuccess() {
                            // Successfully removed the user from the waitlist
                            callback.onSuccess(0); // Success: User has left the waitlist
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

    public void acceptInvite(Integer userID, Integer eventID, ProfileDB.GetCallback<Integer> callback) {
        // Fetch the user's EventUserLink from Firestore
        EventUserLinkDB eventUserLinkDB = new EventUserLinkDB();

        eventUserLinkDB.getEventUserLinkByUserAndEvent(userID, eventID, new ProfileDB.GetCallback<EventUserLink>() {
            @Override
            public void onSuccess(EventUserLink eventUserLink) {
                try {
                    // Check if the user’s status is "Sampled"
                    if ("Sampled".equals(eventUserLink.getStatus())) {
                        // Attempt to set the status to "Accepted"
                        eventUserLink.setStatus("Accepted"); // Directly set the status

                        // Now, update the EventUserLink in the database
                        eventUserLinkDB.updateEventUserLink(eventUserLink, new ProfileDB.ActionCallback() {
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
                } catch (InvalidStatusException e) {
                    // If setting the status throws an InvalidStatusException, handle it here
                    callback.onFailure(new Exception("Invalid status: " + e.getMessage())); // Failure: Invalid status
                }
            }

            @Override
            public void onFailure(Exception e) {
                // If no EventUserLink is found, it means the user was never affiliated with the event
                callback.onSuccess(1); // Failure: User not found
            }
        });
    }

    public void declineInvite(Integer userID, Integer eventID, ProfileDB.GetCallback<Integer> callback) {
        // Fetch the user's EventUserLink from Firestore
        EventUserLinkDB eventUserLinkDB = new EventUserLinkDB();

        eventUserLinkDB.getEventUserLinkByUserAndEvent(userID, eventID, new ProfileDB.GetCallback<EventUserLink>() {
            @Override
            public void onSuccess(EventUserLink eventUserLink) {
                try {
                    // Check if the user’s status is "Sampled"
                    if ("Sampled".equals(eventUserLink.getStatus())) {
                        // Attempt to set the status to "Declined"
                        eventUserLink.setStatus("Declined"); // Directly set the status to "Declined"

                        // Now, update the EventUserLink in the database
                        eventUserLinkDB.updateEventUserLink(eventUserLink, new ProfileDB.ActionCallback() {
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
                } catch (InvalidStatusException e) {
                    // If setting the status throws an InvalidStatusException, handle it here
                    callback.onFailure(new Exception("Invalid status: " + e.getMessage())); // Failure: Invalid status
                }
            }

            @Override
            public void onFailure(Exception e) {
                // If no EventUserLink is found, it means the user was never affiliated with the event
                callback.onSuccess(1); // Failure: User not found
            }
        });
    }

}
