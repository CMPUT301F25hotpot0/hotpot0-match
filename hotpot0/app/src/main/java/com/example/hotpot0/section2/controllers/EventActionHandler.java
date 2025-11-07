package com.example.hotpot0.section2.controllers;

import android.util.Log;
import android.widget.Toast;

import com.example.hotpot0.models.Event;
import com.example.hotpot0.models.EventDB;
import com.example.hotpot0.models.EventUserLink;
import com.example.hotpot0.models.EventUserLinkDB;
import com.example.hotpot0.models.ProfileDB;

import java.util.List;

public class EventActionHandler {
    private ProfileDB profileDB;
    private EventDB eventDB;
    private EventUserLinkDB eventUserLinkDB;

    public EventActionHandler(){
        profileDB = new ProfileDB();
        eventDB = new EventDB();
        eventUserLinkDB = new EventUserLinkDB();
    }

    private String generateLinkID(Integer userID, Integer eventID) {
        // Assuming linkID is created by combining userID and eventID as a string
        return eventID + "_" + userID;  // You can adjust this structure as per your requirements
    }

    public void joinWaitList(Integer userID, Integer eventID, ProfileDB.GetCallback<Integer> callback) {
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
                        int capacity = eventObj.getCapacity();
                        int currentCount = eventObj.getTotalWaitlist();
                        if (currentCount < capacity) {
                            // There is space in the waitlist, proceed with adding the user
                            // If no EventUserLink was found, the user can join the waitlist.
                            // Create a new EventUserLink for the user with the default "inWaitList" status
                            EventUserLink newEventUserLink = new EventUserLink(userID, eventID);

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

//    public void sampleUsers(Integer eventID, ProfileDB.GetCallback<Integer> callback) {
//        eventDB.getEventByID(eventID, new EventDB.GetCallback<Event>() {
//            @Override
//            public void onSuccess(Event eventObj) {
//                eventDB.sampleEvent(eventObj, new EventDB.GetCallback<List<String>>(){
//                    @Override
//                    public void onSuccess(List<String> result) {
//                        for (String linkID : result) {
//                            // For each sampled user, update their EventUserLink status to "Sampled"
//                            eventUserLinkDB.getEventUserLinkByID(linkID, new EventUserLinkDB.GetCallback<EventUserLink>() {
//                                @Override
//                                public void onSuccess(EventUserLink eventUserLink) {
//                                    eventUserLink.setStatus("Sampled");
//                                    eventUserLinkDB.updateEventUserLink(eventUserLink, new EventUserLinkDB.ActionCallback() {
//                                        @Override
//                                        public void onSuccess() {
//                                            // Successfully updated the user's status to "Sampled"
//                                            callback.onSuccess(0); // Success: Users sampled
//                                        }
//
//                                        @Override
//                                        public void onFailure(Exception e) {
//                                            // Failed to update the user's status
//                                            callback.onFailure(e); // Failure to update user status
//                                        }
//                                    });
//                                }
//
//                                @Override
//                                public void onFailure(Exception e) {
//                                    // Failed to retrieve the EventUserLink
//                                    callback.onFailure(e); // Failure to retrieve EventUserLink
//                                }
//                            });
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Exception e) {
//                        // Failed to sample users for the event
//                        callback.onFailure(e); // Failure to sample users
//                    }
//                });
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//                // Failed to retrieve the event
//                callback.onFailure(e); // Failure to retrieve event
//            }
//        });
//
//    }

    public void cancelUser(Integer userID, Integer eventID) {}

}