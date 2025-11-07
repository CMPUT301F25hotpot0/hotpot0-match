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
import com.example.hotpot0.models.Event;
import com.example.hotpot0.models.EventDB;
import com.example.hotpot0.models.EventUserLink;
import com.example.hotpot0.models.EventUserLinkDB;
import com.example.hotpot0.models.ProfileDB;
import com.example.hotpot0.section2.controllers.EventActionHandler;

public class EventSampledActivity extends AppCompatActivity {

    private TextView previewEventName, previewDescription, previewGuidelines, previewLocation, previewTimeAndDay,
            previewDateRange, previewDuration, previewPrice, previewSpotsOpen, previewDaysLeft, GeolocationStatus, statusMessage;
    private ImageView eventImage;
    private Button confirmButton, declineButton, backButton;

    private EventDB eventDB = new EventDB();
    private EventUserLinkDB eventUserLinkDB = new EventUserLinkDB();
    private EventActionHandler eventHandler = new EventActionHandler();
    private Event currentEvent;
    private int eventID;
    private int userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section2_entrantsampledview);

        // Retrieve user and event IDs
        userID = getSharedPreferences("app_prefs", MODE_PRIVATE).getInt("userID", -1);
        eventID = getIntent().getIntExtra("event_id", -1);

        // Initialize UI elements
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
        confirmButton = findViewById(R.id.button_confirm);
        declineButton = findViewById(R.id.Decline);

        // Create Back button programmatically
        backButton = new Button(this);
        backButton.setText("Back to Home");
        backButton.setVisibility(View.GONE);
        ((View) confirmButton.getParent()).post(() -> {
            ((android.widget.LinearLayout) confirmButton.getParent()).addView(backButton);
        });

        // Optional status message
        statusMessage = new TextView(this);
        statusMessage.setVisibility(View.GONE);
        ((View) confirmButton.getParent()).post(() -> {
            ((android.widget.LinearLayout) confirmButton.getParent()).addView(statusMessage);
        });

        // Back button click handler
        backButton.setOnClickListener(v -> navigateHome());

        // Fetch event details
        eventDB.getEventByID(eventID, new EventDB.GetCallback<Event>() {
            @Override
            public void onSuccess(Event event) {
                if (event == null) {
                    Toast.makeText(EventSampledActivity.this, "Event not found.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                currentEvent = event;
                populateUI();
                checkUserStatus();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EventSampledActivity.this, "Error loading event details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUI() {
        previewEventName.setText(currentEvent.getName());
        previewDescription.setText(currentEvent.getDescription());
        previewGuidelines.setText(currentEvent.getGuidelines());
        previewLocation.setText("Location: " + currentEvent.getLocation());
        previewTimeAndDay.setText("Time: " + currentEvent.getTime());
        previewDateRange.setText("Date: " + currentEvent.getDate());
        previewDuration.setText("Duration: " + currentEvent.getDuration());
        previewPrice.setText("Price: $" + currentEvent.getPrice());
        previewSpotsOpen.setText("Spots Open: " + currentEvent.getCapacity());
        previewDaysLeft.setText("Registration Period: " + currentEvent.getRegistration_period());

        boolean geolocationEnabled = currentEvent.getGeolocationRequired();
        GeolocationStatus.setVisibility(View.VISIBLE);
        GeolocationStatus.setText(getString(R.string.event_geolocation, geolocationEnabled ? "Enabled" : "Disabled"));
    }

    private void checkUserStatus() {
        String linkID = eventID + "_" + userID;

        eventUserLinkDB.getEventUserLinkByID(linkID, new EventUserLinkDB.GetCallback<EventUserLink>() {
            @Override
            public void onSuccess(EventUserLink eventUserLink) {
                if (eventUserLink == null) {
                    setupButtonListeners(); // user hasn’t confirmed or declined yet
                    return;
                }

                String status = eventUserLink.getStatus();
                if ("Accepted".equalsIgnoreCase(status)) {
                    showStatusMessage("You have confirmed this event.");
                } else if ("Declined".equalsIgnoreCase(status)) {
                    showStatusMessage("You have declined this event.");
                } else {
                    setupButtonListeners();
                }
            }

            @Override
            public void onFailure(Exception e) {
                setupButtonListeners();
            }
        });
    }

    private void setupButtonListeners() {
        confirmButton.setVisibility(View.VISIBLE);
        declineButton.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.VISIBLE);
        statusMessage.setVisibility(View.GONE);

        confirmButton.setOnClickListener(v -> {
            eventHandler.acceptInvite(userID, eventID, new ProfileDB.GetCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    Toast.makeText(EventSampledActivity.this, "You have confirmed attendance!", Toast.LENGTH_SHORT).show();
                    showStatusMessage("You have confirmed this event.");
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(EventSampledActivity.this, "Error confirming event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        declineButton.setOnClickListener(v -> {
            eventHandler.declineInvite(userID, eventID, new ProfileDB.GetCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    Toast.makeText(EventSampledActivity.this, "You have declined this event.", Toast.LENGTH_SHORT).show();
                    showStatusMessage("You have declined this event.");
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(EventSampledActivity.this, "Error declining event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showStatusMessage(String message) {
        confirmButton.setVisibility(View.GONE);
        declineButton.setVisibility(View.GONE);
        backButton.setVisibility(View.VISIBLE);

        statusMessage.setText(message);
        statusMessage.setVisibility(View.VISIBLE);
        statusMessage.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        statusMessage.setTextColor(getResources().getColor(R.color.text));
        statusMessage.setTextSize(16);
    }

    private void navigateHome() {
        Intent intent = new Intent(EventSampledActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
