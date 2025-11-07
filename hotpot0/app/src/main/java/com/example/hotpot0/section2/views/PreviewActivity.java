package com.example.hotpot0.section2.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;
import com.example.hotpot0.models.Event;
import com.example.hotpot0.section2.controllers.CreateEventHandler;
import com.example.hotpot0.models.EventDB;

/**
 * Activity that displays a preview of an event before it is officially created.
 * <p>
 * Shows all event details including name, description, guidelines, location, time, date,
 * duration, price, capacity, registration period, and geolocation status.
 * <p>
 * Allows the user (organizer) to confirm the creation of the event or go back to the previous screen.
 */
public class PreviewActivity extends AppCompatActivity {

    private TextView previewEventName, previewDescription, previewGuidelines, previewLocation, previewTimeAndDay, previewDateRange, previewDuration, previewPrice, previewSpotsOpen, previewDaysLeft;
    private ImageView eventImage;
    private Button confirmButton, backButton;
    private TextView GeolocationStatus;

    private CreateEventHandler eventHandler; // Reference to controller

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section2_previewevent_activity);

        int userID = getSharedPreferences("app_prefs", MODE_PRIVATE).getInt("userID", -1);

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
        backButton = findViewById(R.id.button_BottomBackPreviewEvent);

        // Initialize controller (CreateEventHandler)
        eventHandler = new CreateEventHandler(this);

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

        // Confirm button (Final event creation)
        confirmButton.setOnClickListener(v -> {
            // Extract data from the preview to pass to the handler
            String eventName = previewEventName.getText().toString().trim();
            String eventDesc = previewDescription.getText().toString().trim();
            String eventGuidelines = previewGuidelines.getText().toString().trim();
            String eventLocation = previewLocation.getText().toString().replace("Location ", ""); // Removing the prefix
            String eventTime = previewTimeAndDay.getText().toString().replace("Time ", "");
            String eventDate = previewDateRange.getText().toString().replace("Date ", "");
            String eventDuration = previewDuration.getText().toString().replace("Duration: ", "").trim();
            Integer eventCapacity = Integer.parseInt(previewSpotsOpen.getText().toString().replace("Capacity: ", "").trim());
            Double eventPrice = Double.parseDouble(previewPrice.getText().toString().replace("Price ", "").replace(" CAD", "").trim());
            String registrationPeriod = previewDaysLeft.getText().toString().replace("Registration Period: ", "").trim();
            Boolean geolocationRequired = GeolocationStatus.getText().toString().equals("Yes");

            String imageURL = ""; // Add the image URL logic here if needed

            // Call the handler to create the event
            eventHandler.createEvent(
                    userID, // Assume '1' is the organizer ID
                    eventName,
                    eventDesc,
                    eventGuidelines,
                    eventLocation,
                    eventTime,
                    eventDate,
                    eventDuration,
                    eventCapacity,
                    eventPrice,
                    registrationPeriod,
                    imageURL,
                    geolocationRequired,
                    new EventDB.GetCallback<Event>() {
                        @Override
                        public void onSuccess(Event result) {
                            // Event successfully created
                            Toast.makeText(PreviewActivity.this, "Event created successfully!", Toast.LENGTH_LONG).show();

                            // Navigate back to HomeActivity
                            Intent intent = new Intent(PreviewActivity.this, HomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);  // Clear the stack
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            finish(); // Finish PreviewActivity
                        }

                        @Override
                        public void onFailure(Exception e) {
                            // Error during event creation
                            Toast.makeText(PreviewActivity.this, "Error creating event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        });

        // Back button
        backButton.setOnClickListener(v -> finish());
    }
}
