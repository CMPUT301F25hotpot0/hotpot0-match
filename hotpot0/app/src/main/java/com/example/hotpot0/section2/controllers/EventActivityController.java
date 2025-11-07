package com.example.hotpot0.section2.controllers;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.hotpot0.models.EventUserLink;
import com.example.hotpot0.models.EventUserLinkDB;
import com.example.hotpot0.models.ProfileDB;
import com.example.hotpot0.models.Status;
import com.google.firebase.firestore.DocumentSnapshot;

public class EventActivityController {

    private final EventUserLinkDB eventUserLinkDB;
    private final Context context;

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
                // User is affiliated with the event, check their status
                String status = eventUserLink.getStatus();

                switch (status) {
                    case "inWaitList":
                        // User is on the waitlist
                        openWaitListActivity();
                        break;

                    case "Sampled":
                    case "Accepted":
                    case "Declined":
                        // User has sampled, accepted, or declined the event
                        openSampledActivity();
                        break;

                    case "Organizer":
                        // User is an organizer
                        openOrganizerActivity();
                        break;

                    default:
                        // No special status, show initial event activity
                        openEventInitialActivity();
                        break;
                }
            }

            @Override
            public void onFailure(Exception e) {
                // If the user is not affiliated with the event (no EventUserLink)
                openEventInitialActivity();
            }
        });
    }

    private void openEventInitialActivity() {
//        Intent intent = new Intent(context, EventInitialActivity.class);
//        context.startActivity(intent);
    }

    private void openWaitListActivity() {
//        Intent intent = new Intent(context, EventWaitListActivity.class);
//        context.startActivity(intent);
    }

    private void openSampledActivity() {
//        Intent intent = new Intent(context, EventSampledActivity.class);
//        context.startActivity(intent);
    }

    private void openOrganizerActivity() {
//        Intent intent = new Intent(context, OrganizerEventActivity.class);
//        context.startActivity(intent);
    }
}
