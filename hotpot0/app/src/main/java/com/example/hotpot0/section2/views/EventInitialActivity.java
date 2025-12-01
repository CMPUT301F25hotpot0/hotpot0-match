package com.example.hotpot0.section2.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;
import com.example.hotpot0.models.Event;
import com.example.hotpot0.models.EventDB;
import com.example.hotpot0.models.EventUserLink;
import com.example.hotpot0.models.EventUserLinkDB;
import com.example.hotpot0.models.ProfileDB;
import com.example.hotpot0.section2.controllers.EventActionHandler;
import com.google.android.material.card.MaterialCardView;

/**
 * Activity that displays the details of a selected event and allows a user
 * to join or leave the waitlist based on their current status.
 */
public class EventInitialActivity extends AppCompatActivity {
    private TextView previewEventName, previewDescription, previewGuidelines, previewLocation, previewTimeAndDay, previewDateRange, previewDuration, previewPrice, previewSpotsOpen, previewDaysLeft, previewWaitingList;
    private ImageView eventImage;
    private MaterialCardView eventImageCard;
    private Button joinLeaveButton, backButton;
    private TextView GeolocationStatus;
    private EventUserLinkDB eventUserLinkDB = new EventUserLinkDB();
    private EventActionHandler eventHandler = new EventActionHandler();
    private EventDB eventDB = new EventDB();
    private int eventID;
    private Event currentEvent;

    private FusedLocationProviderClient fusedLocationClient;
    private Double latitude = null;
    private Double longitude = null;

    /**
     * Called when the activity is first created.
     * Initializes UI elements, fetches event details, and sets up the
     * join/leave waitlist functionality.
     *
     * @param savedInstanceState Android cached state bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section2_entranteventview_activity);

        int userID = getSharedPreferences("app_prefs", MODE_PRIVATE).getInt("userID", -1);
        eventID = getIntent().getIntExtra("event_id", -1); // Get event ID from Intent
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fetchCurrentLocation();

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
        previewWaitingList = findViewById(R.id.previewWaitingList);
        previewDaysLeft = findViewById(R.id.previewDaysLeft);
        GeolocationStatus = findViewById(R.id.GeolocationStatus);
        joinLeaveButton = findViewById(R.id.button_join_or_leave_waitlist);
        backButton = findViewById(R.id.button_BottomBackPreviewEvent);

        // Fetch the event details from EventDB
        eventDB.getEventByID(eventID, new EventDB.GetCallback<Event>() {
            @Override
            public void onSuccess(Event event) {
                if (event == null) {
                    Toast.makeText(EventInitialActivity.this, "Event not found.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                currentEvent = event;
                // Populate the UI with the event details
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
                    Glide.with(EventInitialActivity.this)
                            .load(imageURL)
                            .placeholder(R.drawable.placeholder_image) // optional placeholder
                            .into(eventImage);
                }

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
                previewWaitingList.setText(currentEvent.getTotalWaitlist().toString());
                // previewDaysLeft.setText("Registration Period: " + currentEvent.getRegistration_period());

                // Handle geolocation status
                boolean geolocationEnabled = currentEvent.getGeolocationRequired(); // Assuming you have this info in your event model
                GeolocationStatus.setVisibility(View.VISIBLE);
                GeolocationStatus.setText(getString(R.string.event_geolocation, geolocationEnabled ? "Enabled" : "Disabled"));

                // Now handle the join/leave button based on the user's status
                String linkID = eventID + "_" + userID;

                // Fetch EventUserLink to determine if user is already in the waitlist
                eventUserLinkDB.getEventUserLinkByID(linkID, new EventUserLinkDB.GetCallback<EventUserLink>() {
                    @Override
                    public void onSuccess(EventUserLink eventUserLink) {
                        if (event.getJoinable()) {
                            if (eventUserLink != null && "inWaitList".equals(eventUserLink.getStatus())) {
                                joinLeaveButton.setText(getString(R.string.leave_waitlist));
                                joinLeaveButton.setOnClickListener(v -> {
                                    eventHandler.leaveWaitList(userID, eventID, new ProfileDB.GetCallback<Integer>() {
                                        @Override
                                        public void onSuccess(Integer result) {
                                            Toast.makeText(EventInitialActivity.this, "Successfully left the waitlist!", Toast.LENGTH_SHORT).show();
                                            navigateHome();
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            Toast.makeText(EventInitialActivity.this, "Error leaving waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                });
                            } else {
                                joinLeaveButton.setText(getString(R.string.join_waitlist));
                                if (latitude == null || longitude == null) {
                                    latitude = 0.0;
                                    longitude = 0.0;
                                    return;
                                }
                                joinLeaveButton.setOnClickListener(v -> {
                                    eventHandler.joinWaitList(userID, eventID, latitude, longitude, new ProfileDB.GetCallback<Integer>() {
                                        @Override
                                        public void onSuccess(Integer result) {
                                            switch (result) {
                                                case 0: // Successfully added to waitlist
                                                    Toast.makeText(EventInitialActivity.this, "Successfully joined the waitlist!", Toast.LENGTH_SHORT).show();
                                                    navigateHome();
                                                    break;
                                                case 1:
                                                    Toast.makeText(EventInitialActivity.this, "You are already affiliated with this event.", Toast.LENGTH_SHORT).show();
                                                    break;
                                                case 2: // Waitlist full
                                                    Toast.makeText(EventInitialActivity.this, "Waitlist is full, cannot join!", Toast.LENGTH_SHORT).show();
                                                    break;
                                                default:
                                                    Toast.makeText(EventInitialActivity.this, "Unknown status: " + result, Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            Toast.makeText(EventInitialActivity.this, "Error joining waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                });
                            }
                        } else {
                            joinLeaveButton.setText(getString(R.string.event_not_joinable));
                            joinLeaveButton.setEnabled(false);
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (event.getJoinable()) {
                            joinLeaveButton.setText(getString(R.string.join_waitlist));
                            if (latitude == null || longitude == null) {
                                latitude = 0.0;
                                longitude = 0.0;
                                return;
                            }
                            joinLeaveButton.setOnClickListener(v -> {
                                eventHandler.joinWaitList(userID, eventID, latitude, longitude, new ProfileDB.GetCallback<Integer>() {
                                    @Override
                                    public void onSuccess(Integer result) {
                                        switch (result) {
                                            case 0: // Successfully added to waitlist
                                                Toast.makeText(EventInitialActivity.this, "Successfully joined the waitlist!", Toast.LENGTH_SHORT).show();
                                                navigateHome();
                                                break;
                                            case 1:
                                                Toast.makeText(EventInitialActivity.this, "You are already affiliated with this event.", Toast.LENGTH_SHORT).show();
                                                break;
                                            case 2: // Waitlist full
                                                Toast.makeText(EventInitialActivity.this, "Waitlist is full, cannot join!", Toast.LENGTH_SHORT).show();
                                                break;
                                            default:
                                                Toast.makeText(EventInitialActivity.this, "Unknown status: " + result, Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        Toast.makeText(EventInitialActivity.this, "Error joining waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            });
                        } else {
                            joinLeaveButton.setText(getString(R.string.event_not_joinable));
                            joinLeaveButton.setEnabled(false);
                        }
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EventInitialActivity.this, "Error loading event details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Back button to go back to the previous screen
        backButton.setOnClickListener(v -> finish());
    }

    /** Helper method to navigate back to HomeActivity */
    private void navigateHome() {
        Intent intent = new Intent(EventInitialActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);  // Clear the stack
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void fetchCurrentLocation() {
        // Check for location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    101);
            return;
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            } else {
                // If last location is null, request location updates
                fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if (locationResult == null) return;
                        Location loc = locationResult.getLastLocation();
                        if (loc != null) {
                            latitude = loc.getLatitude();
                            longitude = loc.getLongitude();
                            fusedLocationClient.removeLocationUpdates(this); // Stop updates after first fix
                        }
                    }
                }, Looper.getMainLooper());
            }
        });
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
