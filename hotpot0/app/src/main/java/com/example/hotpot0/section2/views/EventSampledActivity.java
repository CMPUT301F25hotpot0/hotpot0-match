package com.example.hotpot0.section2.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.hotpot0.R;
import com.example.hotpot0.models.Event;
import com.example.hotpot0.models.EventDB;
import com.example.hotpot0.models.EventUserLink;
import com.example.hotpot0.models.EventUserLinkDB;
import com.example.hotpot0.models.ProfileDB;
import com.example.hotpot0.section2.controllers.EventActionHandler;
import com.google.android.material.card.MaterialCardView;

/**
 * Activity that displays a sampled view of an event for a user who
 * has received an invitation. Allows the user to confirm or decline
 * their attendance, and shows status messages accordingly.
 */
public class EventSampledActivity extends AppCompatActivity {

    private TextView previewEventName, previewDescription, previewGuidelines, previewLocation, previewTimeAndDay,
            previewDateRange, previewDuration, previewPrice, previewSpotsOpen, previewDaysLeft, GeolocationStatus, statusMessage;
    private ImageView eventImage;
    private MaterialCardView eventImageCard;
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
        eventImageCard = findViewById(R.id.eventImageCard);
        previewEventName = findViewById(R.id.previewEventName);
        previewDescription = findViewById(R.id.previewDescription);
        previewGuidelines = findViewById(R.id.previewGuidelines);
        previewLocation = findViewById(R.id.previewLocation);
        previewTimeAndDay = findViewById(R.id.previewTimeAndDay);
        previewDateRange = findViewById(R.id.previewDateRange);
        previewDuration = findViewById(R.id.previewDuration);
        previewPrice = findViewById(R.id.previewPrice);
        previewSpotsOpen = findViewById(R.id.previewSpotsOpen);
        GeolocationStatus = findViewById(R.id.GeolocationStatus);
        confirmButton = findViewById(R.id.button_confirm);
        declineButton = findViewById(R.id.Decline);
        backButton = findViewById(R.id.button_back);

        backButton.setOnClickListener(v -> navigateHome());

        // Optional status message
        statusMessage = new TextView(this);
        statusMessage.setVisibility(View.GONE);
        ((View) confirmButton.getParent()).post(() -> {
            ((android.widget.LinearLayout) confirmButton.getParent()).addView(statusMessage);
        });


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

    /**
     * Populates the UI elements with the current event's details.
     */
    private void populateUI() {
        String imageURL = currentEvent.getImageURL();
        if (imageURL == null || imageURL.isEmpty()) {
            // Hide the ImageView if no image is available
            eventImageCard.setVisibility(View.GONE);
            eventImage.setVisibility(View.GONE);
        } else {
            // Show the ImageView
            eventImageCard.setVisibility(View.VISIBLE);
            eventImage.setVisibility(View.VISIBLE);
            // Load image using Glide
            Glide.with(this)
                    .load(imageURL)
                    .placeholder(R.drawable.placeholder_image) // optional placeholder
                    .into(eventImage);
        }
        previewEventName.setText(currentEvent.getName());
        previewDescription.setText(currentEvent.getDescription());
        previewGuidelines.setText(currentEvent.getGuidelines());
        previewEventName.setText(currentEvent.getName());
        previewDescription.setText(currentEvent.getDescription());
        previewGuidelines.setText(currentEvent.getGuidelines());
        previewLocation.setText(currentEvent.getLocation());
        previewTimeAndDay.setText(currentEvent.getTime());
        previewDateRange.setText(buildDateRange(currentEvent.getStartDate(), currentEvent.getEndDate()));
        previewDuration.setText(currentEvent.getDuration());
        String priceText = formatPrice(currentEvent.getPrice().toString());
        previewPrice.setText(priceText);
        String spotsOpen = (currentEvent.getCapacity() - currentEvent.getTotalSampled()) == 0
                ? "All spots are filled!"
                : Integer.toString(currentEvent.getCapacity() - currentEvent.getTotalSampled());
        previewSpotsOpen.setText(spotsOpen);

        boolean geolocationEnabled = currentEvent.getGeolocationRequired();
        GeolocationStatus.setVisibility(View.VISIBLE);
        GeolocationStatus.setText(getString(R.string.event_geolocation, geolocationEnabled ? "Enabled" : "Disabled"));
    }

    /**
     * Checks the current user's status for this event (Accepted, Declined, or Pending)
     * and updates the UI and buttons accordingly.
     */
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
                } else if ("Cancelled".equalsIgnoreCase(status)) {
                    showStatusMessage("You have been cancelled from this event.");
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

    /**
     * Sets up click listeners for the confirm and decline buttons.
     * Also makes the buttons visible and hides the status message.
     */
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

    /**
     * Displays a status message to the user and hides the confirm/decline buttons.
     *
     * @param message The message to display
     */
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

    /**
     * Navigates the user back to HomeActivity.
     */
    private void navigateHome() {
        Intent intent = new Intent(EventSampledActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private String buildDateRange(String startDate, String endDate) {
        if (startDate != null && startDate.equals(endDate)) {
            return startDate;
        } else if (startDate != null && endDate != null && !endDate.isEmpty()) {
            return startDate + " to " + endDate;
        } else if (startDate != null) {
            return startDate;
        } else {
            return "No dates specified";
        }
    }

    private String formatPrice(String price) {
        if (price == null || price.isEmpty()) {
            return "Free";
        }

        try {
            double priceValue = Double.parseDouble(price);
            if (priceValue == 0) {
                return "Free";
            } else {
                return String.format("$%.2f CAD", priceValue);
            }
        } catch (NumberFormatException e) {
            return "Invalid price";
        }
    }
}
