package com.example.hotpot0.section2.controllers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.hotpot0.models.EventUserLink;
import com.example.hotpot0.models.EventUserLinkDB;
import com.example.hotpot0.models.ProfileDB;
import com.example.hotpot0.models.Status;
import com.example.hotpot0.section2.views.EventInitialActivity;
import com.example.hotpot0.section2.views.EventSampledActivity;
import com.example.hotpot0.section2.views.OrganizerEventActivity;
import com.google.firebase.firestore.DocumentSnapshot;

/**
 * Controller class responsible for navigating users to the appropriate event-related activity
 * based on their affiliation and status with a given event.
 * <p>
 * This class fetches the {@link EventUserLink} for a user and event, then opens
 * one of the following activities depending on the status:
 * <ul>
 *     <li>{@link EventInitialActivity} – user is on waitlist or has no affiliation</li>
 *     <li>{@link EventSampledActivity} – user is sampled, accepted, or declined</li>
 *     <li>{@link OrganizerEventActivity} – user is an organizer</li>
 * </ul>
 * </p>
 */
public class EventActivityController {

    private final EventUserLinkDB eventUserLinkDB;
    private final Context context;

    /**
     * Constructs a new {@code EventActivityController}.
     *
     * @param context the Android context used to start activities
     */
    public EventActivityController(Context context) {
        this.context = context;
        this.eventUserLinkDB = new EventUserLinkDB();
    }

    /**
     * Method to navigate to the correct event activity based on the user's status with the event.
     *
     * @param eventID The event ID.
     * @param userID  The user ID.
     */
    public void navigateToEventActivity(int eventID, int userID) {
        String linkID = eventID + "_" + userID;

        // Fetch the EventUserLink for the user and event
        eventUserLinkDB.getEventUserLinkByID(linkID, new EventUserLinkDB.GetCallback<EventUserLink>() {
            @Override
            public void onSuccess(EventUserLink eventUserLink) {

                Log.d("EventActivityController", "EventUserLink status: " + eventUserLink.getStatus());
                // User is affiliated with the event, check their status
                String status = eventUserLink.getStatus();

                switch (status) {
                    case "inWaitList":
                        // User is on the waitlist
                        openEventInitialActivity(eventID);
                        break;

                    case "Sampled":
                    case "Accepted":
                    case "Declined":
                        // User has sampled, accepted, or declined the event
                        openSampledActivity(eventID);
                        break;

                    case "Organizer":
                        // User is an organizer
                        openOrganizerActivity(eventID);
                        break;

                    default:
                        // No special status, show initial event activity
                        openEventInitialActivity(eventID);
                        break;
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("EventActivityController", "Failed to fetch EventUserLink", e);
                // If the user is not affiliated with the event (no EventUserLink)
                openEventInitialActivity(eventID);
            }
        });
    }

    /**
     * Opens {@link EventInitialActivity} for the specified event.
     *
     * @param eventID the ID of the event
     */
    private void openEventInitialActivity(int eventID) {
        Intent intent = new Intent(context, EventInitialActivity.class);
        intent.putExtra("event_id", eventID);
        context.startActivity(intent);
    }

    /**
     * Opens {@link EventSampledActivity} for the specified event.
     *
     * @param eventID the ID of the event
     */
    private void openSampledActivity(int eventID) {
        Intent intent = new Intent(context, EventSampledActivity.class);
        intent.putExtra("event_id", eventID);
        context.startActivity(intent);
    }

    /**
     * Opens {@link OrganizerEventActivity} for the specified event.
     *
     * @param eventID the ID of the event
     */
    private void openOrganizerActivity(int eventID) {
        Intent intent = new Intent(context, OrganizerEventActivity.class);
        intent.putExtra("event_id", eventID);
        context.startActivity(intent);
    }
}
