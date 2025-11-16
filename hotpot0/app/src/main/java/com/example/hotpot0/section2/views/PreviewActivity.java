package com.example.hotpot0.section2.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;
import com.example.hotpot0.models.EventDB;
import com.example.hotpot0.section2.controllers.CreateEventHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class PreviewActivity extends AppCompatActivity {

    private CreateEventHandler eventHandler;

    private TextView previewEventName, previewDescription, previewGuidelines, previewLocation, previewTime, previewDateRange, previewDuration, previewPrice, previewCapacity,
    previewWaitingList, previewRegistrationPeriod, previewGeolocation;
    private ImageView eventImage;
    private Button confirmButton, backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section2_previewevent_activity);

        int userID = getSharedPreferences("app_prefs", MODE_PRIVATE).getInt("userID", -1);

        // Bind UI elements
        initializeViews();

        eventHandler = new CreateEventHandler(this);

        // Extract Intent extras
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String description = intent.getStringExtra("description");
        String guidelines = intent.getStringExtra("guidelines");
        String location = intent.getStringExtra("location");
        String time = intent.getStringExtra("time");
        String startDate = intent.getStringExtra("startDate");
        String endDate = intent.getStringExtra("endDate");
        String duration = intent.getStringExtra("duration");
        String price = intent.getStringExtra("price");
        String capacity = intent.getStringExtra("capacity");
        String waitingList = intent.getStringExtra("waitingListCapacity");
        String registrationStart = intent.getStringExtra("registrationStart");
        String registrationEnd = intent.getStringExtra("registrationEnd");
        boolean geolocationEnabled = intent.getBooleanExtra("geolocationEnabled", false);
        String imageUriString = intent.getStringExtra("imageUri");

        // Populate fields
        populatePreviewFields(name, description, guidelines, location, time, startDate, endDate, duration, price, capacity,
                waitingList, registrationStart, registrationEnd, geolocationEnabled, imageUriString);

        // Set up button listeners
        setupButtonListeners(userID, name, description, guidelines, location, time, startDate, endDate, duration, price, capacity, waitingList,
                registrationStart, registrationEnd, geolocationEnabled, imageUriString);
    }

    private void initializeViews() {

        eventImage = findViewById(R.id.eventImage);
        previewEventName = findViewById(R.id.previewEventName);
        previewDescription = findViewById(R.id.previewDescription);
        previewGuidelines = findViewById(R.id.previewGuidelines);
        previewLocation = findViewById(R.id.previewLocation);
        previewTime = findViewById(R.id.previewTimeAndDay);
        previewDateRange = findViewById(R.id.previewDateRange);
        previewDuration = findViewById(R.id.previewDuration);
        previewPrice = findViewById(R.id.previewPrice);
        previewCapacity = findViewById(R.id.previewSpotsOpen);
        previewWaitingList = findViewById(R.id.previewWaitingList);
        previewRegistrationPeriod = findViewById(R.id.previewDaysLeft);
        previewGeolocation = findViewById(R.id.GeolocationStatus);
        confirmButton = findViewById(R.id.button_confirm);
        backButton = findViewById(R.id.button_BottomBackPreviewEvent);
    }

    private void populatePreviewFields(String name, String description, String guidelines, String location, String time, String startDate,
                                       String endDate, String duration, String price, String capacity, String waitingList, String registrationStart, String registrationEnd,
                                       boolean geolocationEnabled, String imageUriString) {

        // Set text values
        previewEventName.setText(name != null ? name : "No name provided");
        previewDescription.setText(description != null ? description : "No description provided");
        previewGuidelines.setText(guidelines != null ? guidelines : "No guidelines provided");
        previewLocation.setText(location != null ? location : "No location provided");
        previewTime.setText(time != null ? time : "No time specified");

        // Handle date range
        String dateRange = buildDateRange(startDate, endDate);
        previewDateRange.setText(dateRange);

        previewDuration.setText(duration != null ? duration : "No duration specified");

        // Handle price - ensure it's formatted properly
        String priceText = formatPrice(price);
        previewPrice.setText(priceText);

        // Handle capacity
        String capacityText = formatCapacity(capacity);
        previewCapacity.setText(capacityText);

        // Handle waiting list - show properly
        String waitingListText = formatWaitingList(waitingList);
        previewWaitingList.setText(waitingListText);

        // Handle registration period
        String registrationText = buildRegistrationPeriod(registrationStart, registrationEnd);
        previewRegistrationPeriod.setText(registrationText);

        // Handle geolocation status
        updateGeolocationStatus(geolocationEnabled);

        // Load image
        loadEventImage(imageUriString);
    }

    private void setupButtonListeners(int userID, String name, String description, String guidelines, String location, String time,
                                      String startDate, String endDate, String duration, String price, String capacity, String waitingList,
                                      String registrationStart, String registrationEnd, boolean geolocationEnabled, String imageUriString) {

        // Confirm button --> create event in database
        confirmButton.setOnClickListener(v -> {
            confirmButton.setEnabled(false);
            confirmButton.setText("Creating Event...");

            createEventWithValidation(userID, name, description, guidelines,
                    location, time, startDate, endDate, duration,
                    price, capacity, waitingList,
                    registrationStart, registrationEnd,
                    geolocationEnabled, imageUriString);
        });

        // Back button --> return to CreateEventActivity
        backButton.setOnClickListener(v -> {
            finish();
        });
    }

    private void createEventWithValidation(int userID, String name, String description, String guidelines, String location, String time,
                                           String startDate, String endDate, String duration, String price, String capacity, String waitingList,
                                           String registrationStart, String registrationEnd, boolean geolocationEnabled, String imageUriString) {

        // Validate required fields
        if (!validateEventData(name, description, guidelines, location, time,
                startDate, duration, price, capacity, registrationStart)) {
            resetButtonState();
            return;
        }

        // Parse numeric values safely
        int capacityValue;
        double priceValue;
        int waitingListValue;

        try {
            capacityValue = Integer.parseInt(capacity);
            priceValue = Double.parseDouble(price);
            waitingListValue = (waitingList != null && !waitingList.isEmpty()) ?
                    Integer.parseInt(waitingList) : 0;

            // Validate positive values
            if (capacityValue <= 0) {
                Toast.makeText(this, "Capacity must be positive", Toast.LENGTH_SHORT).show();
                resetButtonState();
                return;
            }

            if (priceValue < 0) {
                Toast.makeText(this, "Price cannot be negative", Toast.LENGTH_SHORT).show();
                resetButtonState();
                return;
            }

            if (waitingListValue < 0) {
                Toast.makeText(this, "Waiting list capacity cannot be negative", Toast.LENGTH_SHORT).show();
                resetButtonState();
                return;
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid numeric values", Toast.LENGTH_SHORT).show();
            resetButtonState();
            return;
        }

        // Build date strings
        String eventDate = buildDateRange(startDate, endDate);
        String registrationPeriod = buildRegistrationPeriod(registrationStart, registrationEnd);

        Uri safeUri = null;
        if (!TextUtils.isEmpty(imageUriString)) {
            Uri originalUri = Uri.parse(imageUriString);
            safeUri = getSafeUriForUpload(originalUri);
        }

        // Create the event
        eventHandler.createEvent(
                userID,
                name,
                description,
                guidelines,
                location,
                time,
                startDate,
                endDate,
                duration,
                capacityValue,
                waitingListValue,
                priceValue,
                registrationStart,
                registrationEnd,
                safeUri != null ? safeUri.toString() : "",
                geolocationEnabled,
                new com.example.hotpot0.models.EventDB.GetCallback<com.example.hotpot0.models.Event>() {

                    @Override
                    public void onSuccess(com.example.hotpot0.models.Event event) {
                        Toast.makeText(PreviewActivity.this, "Event created successfully!", Toast.LENGTH_LONG).show();

                        // Navigate to home
                        Intent homeIntent = new Intent(PreviewActivity.this, HomeActivity.class);
                        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(homeIntent);
                        finish();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(PreviewActivity.this, "Error creating event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        resetButtonState();
                    }
                }
        );
    }

    // Helper Methods

    private boolean validateEventData(String name, String description, String guidelines, String location, String time, String startDate,
                                      String duration, String price, String capacity, String registrationStart) {

        if (name == null || name.trim().isEmpty()) {
            Toast.makeText(this, "Event name is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (description == null || description.trim().isEmpty()) {
            Toast.makeText(this, "Event description is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (guidelines == null || guidelines.trim().isEmpty()) {
            Toast.makeText(this, "Event guidelines are required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (location == null || location.trim().isEmpty()) {
            Toast.makeText(this, "Event location is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (time == null || time.trim().isEmpty()) {
            Toast.makeText(this, "Event time is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (startDate == null || startDate.trim().isEmpty()) {
            Toast.makeText(this, "Event start date is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (duration == null || duration.trim().isEmpty()) {
            Toast.makeText(this, "Event duration is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (price == null || price.trim().isEmpty()) {
            Toast.makeText(this, "Event price is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (capacity == null || capacity.trim().isEmpty()) {
            Toast.makeText(this, "Event capacity is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (registrationStart == null || registrationStart.trim().isEmpty()) {
            Toast.makeText(this, "Registration start date is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void resetButtonState() {
        confirmButton.setEnabled(true);
        confirmButton.setText("Confirm");
    }

    private String buildDateRange(String startDate, String endDate) {
        if (startDate != null && endDate != null) {
            return startDate + " to " + endDate;
        } else if (startDate != null) {
            return startDate + " (Single day event)";
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

    private String formatCapacity(String capacity) {
        if (capacity == null || capacity.isEmpty()) {
            return "Capacity not specified";
        }

        try {
            int capacityValue = Integer.parseInt(capacity);
            return capacityValue + " spots";
        } catch (NumberFormatException e) {
            return "Invalid capacity";
        }
    }

    private String formatWaitingList(String waitingList) {
        if (waitingList == null || waitingList.isEmpty() || waitingList.equals("0")) {
            return "No Cap on waiting list";
        }

        try {
            int waitingListValue = Integer.parseInt(waitingList);
            return waitingListValue + " waiting list spots";
        } catch (NumberFormatException e) {
            return "Invalid waiting list capacity";
        }
    }

    private String buildRegistrationPeriod(String registrationStart, String registrationEnd) {
        if (registrationStart != null && registrationEnd != null) {
            return registrationStart + " to " + registrationEnd;
        } else if (registrationStart != null) {
            return "Starts: " + registrationStart;
        } else {
            return "Registration period not specified";
        }
    }

    private void updateGeolocationStatus(boolean geolocationEnabled) {
        if (geolocationEnabled) {
            previewGeolocation.setText("NOTE: Geolocation tracking enabled");
            previewGeolocation.setVisibility(View.VISIBLE);
        } else {
            previewGeolocation.setVisibility(View.GONE);
        }
    }

    private void loadEventImage(String imageUriString) {
        if (imageUriString != null && !imageUriString.isEmpty()) {
            try {
                Uri imageUri = Uri.parse(imageUriString);
                eventImage.setImageURI(imageUri); // simple and direct
            } catch (Exception e) {
                e.printStackTrace();
                eventImage.setImageResource(R.drawable.ic_camera);
            }
        } else {
            eventImage.setImageResource(R.drawable.ic_camera);
        }
    }

    private Uri getSafeUriForUpload(Uri originalUri) {
        if (originalUri == null) return null;

        try {
            InputStream in = getContentResolver().openInputStream(originalUri);
            if (in == null) {
                Log.e("PreviewActivity", "Cannot open input stream from URI: " + originalUri);
                return null;
            }

            // Create a file in cache directory instead of internal storage
            File cacheDir = getCacheDir();
            File tempFile = new File(cacheDir, "event_upload_" + System.currentTimeMillis() + ".png");

            OutputStream out = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();

            Log.d("PreviewActivity", "Temp file created: " + tempFile.getAbsolutePath());
            return Uri.fromFile(tempFile);

        } catch (Exception e) {
            Log.e("PreviewActivity", "Error creating safe URI", e);
            return null;
        }
    }
}