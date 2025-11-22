package com.example.hotpot0.section2.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
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

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Activity that displays the details of a selected event and allows a user
 * to join or leave the waitlist based on their current status.
 */
public class EventInitialActivity extends AppCompatActivity {
    private TextView previewEventName, previewDescription, previewGuidelines, previewLocation, previewTimeAndDay, previewDateRange, previewDuration, previewPrice, previewSpotsOpen, previewDaysLeft;

    private ImageView eventImage;
    private Button joinLeaveButton, backButton;
    private TextView GeolocationStatus;
    private EventUserLinkDB eventUserLinkDB = new EventUserLinkDB();
    private EventActionHandler eventHandler = new EventActionHandler();
    private EventDB eventDB = new EventDB();
    private int eventID;
    private Event currentEvent;

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
        joinLeaveButton = findViewById(R.id.button_join_or_leave_waitlist);
        backButton = findViewById(R.id.button_BottomBackPreviewEvent);

        Spinner eventDetailsSpinner = findViewById(R.id.EventDetailsSpinner);
        Context activityContext = this;


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
                    eventImage.setVisibility(View.GONE);
                } else {
                    // Show the ImageView
                    eventImage.setVisibility(View.VISIBLE);
                    // Load image using Glide
                    Glide.with(activityContext)
                            .load(imageURL)
                            .placeholder(R.drawable.placeholder_image) // optional placeholder
                            .into(eventImage);
                }

                previewEventName.setText(currentEvent.getName());
                previewDescription.setText(currentEvent.getDescription());
                previewGuidelines.setText(currentEvent.getGuidelines());
                previewLocation.setText(currentEvent.getLocation());
                previewTimeAndDay.setText(currentEvent.getTime());
                // previewDateRange.setText("Date: " + currentEvent.getDate());
                previewDuration.setText(currentEvent.getDuration());
                previewPrice.setText("$" + currentEvent.getPrice());
                String spotsOpen = (currentEvent.getCapacity() - currentEvent.getTotalWaitlist()) == 0
                        ? "All spots are filled!"
                        : Integer.toString(currentEvent.getCapacity() - currentEvent.getTotalWaitlist());
                previewSpotsOpen.setText(spotsOpen);
                // previewDaysLeft.setText("Registration Period: " + currentEvent.getRegistration_period());

                // Handle geolocation status
                boolean geolocationEnabled = currentEvent.getGeolocationRequired(); // Assuming you have this info in your event model
                GeolocationStatus.setVisibility(View.VISIBLE);
                GeolocationStatus.setText(getString(R.string.event_geolocation, geolocationEnabled ? "Enabled" : "Disabled"));

                // Now handle the join/leave button based on the user's status
                String linkID = eventID + "_" + userID;

                eventDetailsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selectedOption = parent.getItemAtPosition(position).toString();
                        switch (selectedOption) {
                            case "Guidelines":
                                previewGuidelines.setVisibility(View.VISIBLE);
                                previewLocation.setVisibility(View.GONE);
                                previewTimeAndDay.setVisibility(View.GONE);
                                previewDateRange.setVisibility(View.GONE);
                                previewDuration.setVisibility(View.GONE);
                                previewPrice.setVisibility(View.GONE);
                                break;
                            case "Location":
                                previewGuidelines.setVisibility(View.GONE);
                                previewLocation.setVisibility(View.VISIBLE);
                                previewTimeAndDay.setVisibility(View.GONE);
                                previewDateRange.setVisibility(View.GONE);
                                previewDuration.setVisibility(View.GONE);
                                previewPrice.setVisibility(View.GONE);
                                break;
                            case "Time":
                                previewGuidelines.setVisibility(View.GONE);
                                previewLocation.setVisibility(View.GONE);
                                previewTimeAndDay.setVisibility(View.VISIBLE);
                                previewDateRange.setVisibility(View.GONE);
                                previewDuration.setVisibility(View.GONE);
                                previewPrice.setVisibility(View.GONE);
                                break;
                            case "Dates":
                                previewGuidelines.setVisibility(View.GONE);
                                previewLocation.setVisibility(View.GONE);
                                previewTimeAndDay.setVisibility(View.GONE);
                                previewDateRange.setVisibility(View.VISIBLE);
                                previewDuration.setVisibility(View.GONE);
                                previewPrice.setVisibility(View.GONE);
                                break;
                            case "Duration":
                                previewGuidelines.setVisibility(View.GONE);
                                previewLocation.setVisibility(View.GONE);
                                previewTimeAndDay.setVisibility(View.GONE);
                                previewDateRange.setVisibility(View.GONE);
                                previewDuration.setVisibility(View.VISIBLE);
                                previewPrice.setVisibility(View.GONE);
                                break;
                            case "Price":
                                previewGuidelines.setVisibility(View.GONE);
                                previewLocation.setVisibility(View.GONE);
                                previewTimeAndDay.setVisibility(View.GONE);
                                previewDateRange.setVisibility(View.GONE);
                                previewDuration.setVisibility(View.GONE);
                                previewPrice.setVisibility(View.VISIBLE);
                                break;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // Do nothing
                    }
                });

                ArrayList<String> spinnerOptions = new ArrayList<>();
                spinnerOptions.add("Guidelines");
                spinnerOptions.add("Location");
                spinnerOptions.add("Time");
                spinnerOptions.add("Dates");
                spinnerOptions.add("Duration");
                spinnerOptions.add("Price");
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(activityContext, R.layout.spinner_selected_item, spinnerOptions);
                spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                eventDetailsSpinner.setAdapter(spinnerAdapter);

                // Fetch EventUserLink to determine if user is already in the waitlist
                eventUserLinkDB.getEventUserLinkByID(linkID, new EventUserLinkDB.GetCallback<EventUserLink>() {
                    @Override
                    public void onSuccess(EventUserLink eventUserLink) {
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
                            joinLeaveButton.setOnClickListener(v -> {
                                eventHandler.joinWaitList(userID, eventID, new ProfileDB.GetCallback<Integer>() {
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
                    }

                    @Override
                    public void onFailure(Exception e) {
                        joinLeaveButton.setText(getString(R.string.join_waitlist));
                        joinLeaveButton.setOnClickListener(v -> {
                            eventHandler.joinWaitList(userID, eventID, new ProfileDB.GetCallback<Integer>() {
                                @Override
                                public void onSuccess(Integer result) {
                                    Toast.makeText(EventInitialActivity.this, "Successfully joined the waitlist!", Toast.LENGTH_SHORT).show();
                                    navigateHome();
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(EventInitialActivity.this, "Error joining waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
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
}
