package com.example.hotpot0.section2.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;
import com.example.hotpot0.models.Event;
import com.example.hotpot0.section2.controllers.CreateEventHandler;
import com.example.hotpot0.models.EventDB;

public class PreviewActivity extends AppCompatActivity {

    private TextView tvEventName, tvDescription, tvGuidelines, tvLocation, tvTimeAndDay, tvDateRange, tvDuration, tvPrice, tvSpotsOpen, tvDaysLeft, geolocationSwitch;
    private ImageView eventImage;
    private Button confirmButton, backButton;

    private CreateEventHandler eventHandler; // Reference to controller

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section2_previewevent_activity);

        int userID = getSharedPreferences("app_prefs", MODE_PRIVATE).getInt("userID", -1);

        eventImage = findViewById(R.id.eventImage);
        tvEventName = findViewById(R.id.tvEventName);
        tvDescription = findViewById(R.id.tvDescription);
        tvGuidelines = findViewById(R.id.tvGuidelines);
        tvLocation = findViewById(R.id.tvLocation);
        tvTimeAndDay = findViewById(R.id.tvTimeAndDay);
        tvDateRange = findViewById(R.id.tvDateRange);
        tvDuration = findViewById(R.id.tvDuration);
        tvPrice = findViewById(R.id.tvPrice);
        tvSpotsOpen = findViewById(R.id.tvSpotsOpen);
        tvDaysLeft = findViewById(R.id.tvDaysLeft);
        geolocationSwitch = findViewById(R.id.tvGeolocation);

        confirmButton = findViewById(R.id.button_confirm);
        backButton = findViewById(R.id.button_BottomBackPreviewEvent);

        // Initialize controller (CreateEventHandler)
        eventHandler = new CreateEventHandler(this);

        // Get event data from the Intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            tvEventName.setText(extras.getString("name", ""));
            tvDescription.setText(extras.getString("description", ""));
            tvGuidelines.setText(extras.getString("guidelines", ""));
            tvLocation.setText("Location " + extras.getString("location", ""));
            tvTimeAndDay.setText("Time " + extras.getString("time", ""));
            tvDateRange.setText("Date " + extras.getString("date", ""));
            tvDuration.setText("Duration: " + extras.getString("duration", ""));
            tvPrice.setText("Price " + extras.getString("price", "") + " CAD");
            tvSpotsOpen.setText("Capacity: " + extras.getString("capacity", ""));
            tvDaysLeft.setText("Registration Period: " + extras.getString("registration", ""));
            geolocationSwitch.setText(extras.getBoolean("geolocation", false) ? "Yes" : "No");
        }

        // Confirm button (Final event creation)
        confirmButton.setOnClickListener(v -> {
            // Extract data from the preview to pass to the handler
            String eventName = tvEventName.getText().toString().trim();
            String eventDesc = tvDescription.getText().toString().trim();
            String eventGuidelines = tvGuidelines.getText().toString().trim();
            String eventLocation = tvLocation.getText().toString().replace("Location ", ""); // Removing the prefix
            String eventTime = tvTimeAndDay.getText().toString().replace("Time ", "");
            String eventDate = tvDateRange.getText().toString().replace("Date ", "");
            String eventDuration = tvDuration.getText().toString().replace("Duration: ", "").trim();
            Integer eventCapacity = Integer.parseInt(tvSpotsOpen.getText().toString().replace("Capacity: ", "").trim());
            Double eventPrice = Double.parseDouble(tvPrice.getText().toString().replace("Price ", "").replace(" CAD", "").trim());
            String registrationPeriod = tvDaysLeft.getText().toString().replace("Registration Period: ", "").trim();
            Boolean geolocationRequired = geolocationSwitch.getText().toString().equals("Yes");

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
        backButton.setOnClickListener(v -> finish()); // Just return to previous screen
    }
}
