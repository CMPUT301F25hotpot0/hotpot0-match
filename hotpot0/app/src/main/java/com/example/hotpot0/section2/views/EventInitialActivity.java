package com.example.hotpot0.section2.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;
import com.example.hotpot0.models.EventUserLink;
import com.example.hotpot0.models.EventUserLinkDB;
import com.example.hotpot0.models.ProfileDB;
import com.example.hotpot0.section2.controllers.EventActionHandler;


public class EventInitialActivity extends AppCompatActivity {
    private TextView previewEventName, previewDescription, previewGuidelines, previewLocation, previewTimeAndDay, previewDateRange, previewDuration, previewPrice, previewSpotsOpen, previewDaysLeft;
    private ImageView eventImage;
    private Button joinLeaveButton, backButton;
    private TextView GeolocationStatus;
    private EventUserLinkDB eventUserLinkDB = new EventUserLinkDB();

    private EventActionHandler eventHandler = new EventActionHandler(); // Reference to controller

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section3_entranteventview_activity);

        int userID = getSharedPreferences("app_prefs", MODE_PRIVATE).getInt("userID", -1);
        int eventID = getIntent().getIntExtra("event_id", -1);

        eventImage = findViewById(R.id.eventImage);
        previewEventName = findViewById(R.id.previewEventName);
        previewDescription = findViewById(R.id.previewDescription);
        previewGuidelines = findViewById(R.id.previewGuidelines);
        previewLocation = findViewById(R.id.previewLocation);
        previewTimeAndDay = findViewById(R.id.previewTimeAndDay);
        previewDateRange = findViewById(R.id.previewDateRange);
        previewDuration = findViewById(R.id.previewDuration);
        previewPrice = findViewById(R.id.previewPrice);
        previewSpotsOpen = findViewById(R.id.previewSpotsOpen);
        previewDaysLeft = findViewById(R.id.previewDaysLeft);
        GeolocationStatus = findViewById(R.id.GeolocationStatus);

        joinLeaveButton = findViewById(R.id.button_join_or_leave_waitlist);

        // Build the linkID based on user and event
        String linkID = eventID + "_" + userID;

        eventUserLinkDB.getEventUserLinkByID(linkID, new EventUserLinkDB.GetCallback<EventUserLink>() {
            @Override
            public void onSuccess(EventUserLink eventUserLink) {
                // If the user is already in the event (e.g., in the waitlist)
                if (eventUserLink != null && "inWaitList".equals(eventUserLink.getStatus())) {
                    joinLeaveButton.setText(getString(R.string.leave_waitlist));
                    // When the button is clicked, leave the waitlist
                    joinLeaveButton.setOnClickListener(v -> {
                        eventHandler.leaveWaitList(userID, eventID, new ProfileDB.GetCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer result) {
                                Toast.makeText(EventInitialActivity.this, "Successfully left the waitlist!", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(EventInitialActivity.this, "Error leaving waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                } else {
                    // If the user does not have an EventUserLink (they're not on the waitlist)
                    joinLeaveButton.setText(getString(R.string.join_waitlist));
                    // When the button is clicked, join the waitlist
                    joinLeaveButton.setOnClickListener(v -> {
                        eventHandler.joinWaitList(userID, eventID, new ProfileDB.GetCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer result) {
                                Toast.makeText(EventInitialActivity.this, "Successfully joined the waitlist!", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(EventInitialActivity.this, "Error joining waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                // In case the EventUserLink does not exist, allow joining the waitlist
                joinLeaveButton.setText(getString(R.string.join_waitlist));
                joinLeaveButton.setOnClickListener(v -> {
                    eventHandler.joinWaitList(userID, eventID, new ProfileDB.GetCallback<Integer>() {
                        @Override
                        public void onSuccess(Integer result) {
                            Toast.makeText(EventInitialActivity.this, "Successfully joined the waitlist!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(EventInitialActivity.this, "Error joining waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }
        });

        // Back button
        backButton = findViewById(R.id.button_BottomBackPreviewEvent);
        backButton.setOnClickListener(v -> finish());

        // Get event data from the Intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            previewEventName.setText(extras.getString("name", ""));
            previewDescription.setText(extras.getString("description", ""));
            previewGuidelines.setText(extras.getString("guidelines", ""));
            previewLocation.setText(getString(R.string.event_location, extras.getString("location", "")));
            previewTimeAndDay.setText(getString(R.string.event_time, extras.getString("time", "")));
            previewDateRange.setText(getString(R.string.event_date, extras.getString("date", "")));
            previewDuration.setText(getString(R.string.event_duration, extras.getString("duration", "")));
            previewPrice.setText(getString(R.string.event_price, extras.getString("price", "")));
            previewSpotsOpen.setText(getString(R.string.event_capacity, extras.getString("capacity", "")));
            previewDaysLeft.setText(getString(R.string.event_registration, extras.getString("registration", "")));

            boolean geolocationEnabled = getIntent().getBooleanExtra("geolocationEnabled", false);
            GeolocationStatus.setVisibility(View.VISIBLE);
            GeolocationStatus.setText(getString(R.string.event_geolocation, geolocationEnabled ? "Enabled" : "Disabled"));
        }
    }
}
