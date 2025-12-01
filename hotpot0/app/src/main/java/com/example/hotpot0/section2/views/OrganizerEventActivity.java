package com.example.hotpot0.section2.views;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hotpot0.R;
import com.example.hotpot0.models.Event;
import com.example.hotpot0.models.EventDB;
import com.example.hotpot0.models.EventUserLink;
import com.example.hotpot0.models.EventUserLinkDB;
import com.example.hotpot0.models.PicturesDB;
import com.example.hotpot0.models.ProfileDB;
import com.example.hotpot0.models.UserProfile;
import com.example.hotpot0.section2.controllers.EventActionHandler;
import com.example.hotpot0.section2.controllers.QRGenerator;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


/**
 * Activity that allows event organizers to view and manage their events.
 * <p>
 * Provides two layouts:
 * <ul>
 *     <li>Pre-draw layout: before any participants are sampled.</li>
 *     <li>Post-draw layout: after a random sample of participants has been selected.</li>
 * </ul>
 * <p>
 * Implements the following functionalities:
 * <ul>
 *     <li>Viewing event details</li>
 *     <li>Viewing waitlist and entrants</li>
 *     <li>Generating a random sample of participants</li>
 *     <li>Confirming entrants and filling empty spots</li>
 *     <li>Navigation via bottom navigation bar</li>
 * </ul>
 */
public class OrganizerEventActivity extends AppCompatActivity {

    private int eventID;
    private Event currentEvent = new Event();
    private EventUserLink currentEventUserLink;

    TextView previewGeolocation;
    private EventDB eventDB = new EventDB();
    private ProfileDB profileDB = new ProfileDB();
    private EventUserLinkDB eventUserLinkDB = new EventUserLinkDB();
    private EventActionHandler eventHandler = new EventActionHandler();

    private String csvDataToSave;

    // Handling Image Uploads
    private Uri selectedImageUri;
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            selectedImageUri = result.getData().getData();
                            if (selectedImageUri != null) {
                                uploadImageToFirebase(selectedImageUri);
                            }
                        }
                    }
            );

    private final ActivityResultLauncher<Intent> createCsvLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                            outputStream.write(csvDataToSave.getBytes());
                            Toast.makeText(this, "CSV saved successfully!", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to save CSV: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get UserID and EventID to display the required view
        int userID = getSharedPreferences("app_prefs", MODE_PRIVATE).getInt("userID", -1);
        eventID = getIntent().getIntExtra("event_id", -1);

        // Generating EventUserLink linkID
        String linkID = eventID + "_" + userID;

        // Fetch EventUserLink Object
        eventUserLinkDB.getEventUserLinkByID(linkID, new EventUserLinkDB.GetCallback<EventUserLink>() {
            @Override
            public void onSuccess(EventUserLink eventUserLink) {
                // Check if eventUserLink is null
                if (eventUserLink == null) {
                    Toast.makeText(OrganizerEventActivity.this, "Event link not found.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                // Save current EventUserLink
                currentEventUserLink = eventUserLink;

                // Then fetch Event
                eventDB.getEventByID(eventID, new EventDB.GetCallback<Event>() {
                    @Override
                    public void onSuccess(Event event) {
                        // Check if event is null
                        if (event == null) {
                            Toast.makeText(OrganizerEventActivity.this, "Event not found.", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        // Save current Event
                        currentEvent = event;

                        // Decide which layout to show
                        if (event.getSampledIDs().isEmpty() && event.getCancelledIDs().isEmpty()) {
                            setContentView(R.layout.section2_organizereventview_activity);
                            setupPreDrawLayout();
                        } else {
                            setContentView(R.layout.section2_organizereventview_postdraw);
                            setupPostDrawLayout();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(OrganizerEventActivity.this, "Error loading event details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(OrganizerEventActivity.this, "Error loading event link: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    // ------------------ PRE-DRAW LAYOUT ------------------
    private void setupPreDrawLayout() {

        // View bindings
        ImageView eventImage = findViewById(R.id.eventImage);
        MaterialCardView eventImageCard = findViewById(R.id.eventImageCard);
        TextView previewEventName = findViewById(R.id.previewEventName);
        TextView previewDescription = findViewById(R.id.previewDescription);
        TextView previewGuidelines = findViewById(R.id.previewGuidelines);
        TextView previewLocation = findViewById(R.id.previewLocation);
        TextView previewTime = findViewById(R.id.previewTime);
        TextView previewDateRange = findViewById(R.id.previewDateRange);
        TextView previewDuration = findViewById(R.id.previewDuration);
        TextView previewPrice = findViewById(R.id.previewPrice);
        TextView previewSpotsOpen = findViewById(R.id.previewSpotsOpen);
        TextView previewWaitingListCapacity = findViewById(R.id.previewWaitingListCapacity);
        TextView previewCurrentlyWaiting = findViewById(R.id.previewCurrentlyWaiting);
        TextView previewDaysLeft = findViewById(R.id.previewDaysLeft);
        Button generateSampleButton = findViewById(R.id.generate_sample_button);
        generateSampleButton.setEnabled(currentEvent.getTotalSampled() == 0);
        LinearLayout entrantsContainer = findViewById(R.id.entrants_container);
        previewGeolocation = findViewById(R.id.GeolocationStatus);
        ImageView qrCodeImage = findViewById(R.id.qr_code_image);
        TextView mapTitle = findViewById(R.id.event_map_title);
        FrameLayout mapContainer = findViewById(R.id.map_container);

        // Image Handling Buttons
        Button uploadImageButton = findViewById(R.id.upload_image_button);
        if (currentEvent.getImageURL() == null || currentEvent.getImageURL().isEmpty()) {
            uploadImageButton.setVisibility(View.VISIBLE);
        } else {
            uploadImageButton.setVisibility(View.GONE);
        }
        Button deleteImageButton = findViewById(R.id.delete_image_button);
        if (currentEvent.getImageURL() != null && !currentEvent.getImageURL().isEmpty()){
            deleteImageButton.setVisibility(View.VISIBLE);
        } else {
            deleteImageButton.setVisibility(View.GONE);
        }
        uploadImageButton.setOnClickListener(v -> openImagePicker());
        deleteImageButton.setOnClickListener(v -> deleteEventImage());

        // Populate Event Info
        previewEventName.setText(currentEvent.getName() != null ? currentEvent.getName() : "No name provided");
        previewDescription.setText(currentEvent.getDescription() != null ? currentEvent.getDescription() : "No description provided");
        previewGuidelines.setText(currentEvent.getGuidelines() != null ? currentEvent.getGuidelines() : "No guidelines provided");
        previewLocation.setText(currentEvent.getLocation() != null ? currentEvent.getLocation() : "No location provided");
        previewTime.setText(currentEvent.getTime() != null ? currentEvent.getTime() : "No time specified");
        // Handle date range
        String dateRange = buildDateRange(currentEvent.getStartDate(), currentEvent.getEndDate());
        previewDateRange.setText(dateRange);
        previewDuration.setText(currentEvent.getDuration() != null ? currentEvent.getDuration() : "No duration specified");
        // Handle price
        String priceText = formatPrice(currentEvent.getPrice().toString());
        previewPrice.setText(priceText);
        // Handle capacity
        String spotsOpen = (currentEvent.getCapacity() - currentEvent.getTotalWaitlist()) == 0
                ? "All spots are filled!"
                : Integer.toString(currentEvent.getCapacity() - currentEvent.getTotalWaitlist());
        previewSpotsOpen.setText(spotsOpen);
        // Handle waiting list
        String waitingListText = formatWaitingList(currentEvent.getWaitingListCapacity().toString());
        previewWaitingListCapacity.setText(waitingListText);
        // Handle registration period
        String registrationText = buildRegistrationPeriod(currentEvent.getRegistrationStart(), currentEvent.getRegistrationEnd());
        previewDaysLeft.setText(registrationText);
        // Handle geolocation status
        updateGeolocationStatus(currentEvent.getGeolocationRequired());

        // Handle Event Image
        String imageURL = currentEvent.getImageURL();
        if (imageURL == null || imageURL.isEmpty()) {
            // Hide the ImageView if no image is available
            eventImageCard.setVisibility(View.GONE);
            eventImage.setVisibility(View.GONE);
        } else {
            // Show the ImageView
            eventImage.setVisibility(View.VISIBLE);
            eventImageCard.setVisibility(View.VISIBLE);
            // Load image using Glide
            Glide.with(this)
                    .load(imageURL)
                    .placeholder(R.drawable.placeholder_image) // optional placeholder
                    .into(eventImage);
        }

        // Generate QR Code for the Event
        QRGenerator qrGenerator = new QRGenerator();
        Bitmap qrBitmap = qrGenerator.generateQR("event:"+currentEvent.getEventID());

        if (qrBitmap != null) {
            qrCodeImage.setImageBitmap(qrBitmap);
            qrCodeImage.setVisibility(View.VISIBLE);
        } else {
            qrCodeImage.setVisibility(View.GONE); // hide if QR generation failed
        }

        // Load waitlist users
        eventUserLinkDB.getWaitListUsers(currentEvent.getLinkIDs(), new EventUserLinkDB.GetCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> waitListLinkIDs) {
                // Get Number of People in Wait List
                String numWaitList = waitListLinkIDs.size() + "";
                previewCurrentlyWaiting.setText(numWaitList);
                TextView geoHeading = findViewById(R.id.event_map_title);
                // Hide mapContainer if no one is waiting or if current event has no geolocation enabled
                if (waitListLinkIDs.isEmpty() || !currentEvent.getGeolocationRequired()) {
                    geoHeading.setVisibility(View.GONE);
                    mapTitle.setVisibility(View.GONE);
                    mapContainer.setVisibility(View.GONE);
                } else {
                    mapContainer.setVisibility(View.VISIBLE);
                    // Check if geolocation is enabled, only then show the map
                    if (currentEvent.getGeolocationRequired()) {
                        // At this point, geolocation required - KEEP MAP VISIBLE
                        mapContainer.setVisibility(View.VISIBLE);
                        mapTitle.setVisibility(View.VISIBLE);

                        // Create a SupportMapFragment dynamically
                        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.map_container, mapFragment)
                                .commit();

                        // When map is ready, plot the entrants
                        mapFragment.getMapAsync(googleMap -> {
                            plotEntrantsOnMap(googleMap, currentEvent.getLinkIDs(), mapContainer);
                        });

                    } else {
                        geoHeading.setVisibility(View.GONE);
                        mapContainer.setVisibility(View.GONE);
                    }
                }
                // Populate Entrants Section
                populateEntrants(entrantsContainer, waitListLinkIDs);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(OrganizerEventActivity.this, "Failed to load waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Generate Sample Button
        generateSampleButton.setOnClickListener(v -> {
            Toast.makeText(this, "Generating random sample...", Toast.LENGTH_SHORT).show();

            eventDB.sampleEvent(currentEvent, new EventDB.GetCallback<List<String>>() {
                @Override
                public void onSuccess(List<String> sampledUsers) {

                    // Change status of EventUserLinks to "Sampled"
                    for (String linkID : sampledUsers) {
                        eventUserLinkDB.getEventUserLinkByID(linkID, new EventUserLinkDB.GetCallback<EventUserLink>() {
                            @Override
                            public void onSuccess(EventUserLink eventUserLink) {
                                if (eventUserLink != null) {
                                    // Update status to "Sampled"
                                    eventUserLink.setStatus("Sampled");
                                    eventUserLinkDB.updateEventUserLink(eventUserLink, new EventUserLinkDB.ActionCallback() {
                                        @Override
                                        public void onSuccess() {
                                            ;
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            Toast.makeText(
                                                    OrganizerEventActivity.this,
                                                    "Error updating link " + linkID + ": " + e.getMessage(),
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                        }
                                    });
                                } else {
                                    Toast.makeText(
                                            OrganizerEventActivity.this,
                                            "EventUserLink not found for " + linkID,
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(
                                        OrganizerEventActivity.this,
                                        "Error fetching EventUserLink " + linkID + ": " + e.getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });
                    }


                    Toast.makeText(OrganizerEventActivity.this,
                            "Sample generated successfully! Total: " + sampledUsers.size(),
                            Toast.LENGTH_SHORT).show();

                    // Switch to Post-Draw layout
                    setContentView(R.layout.section2_organizereventview_postdraw);
                    setupPostDrawLayout();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(OrganizerEventActivity.this, "Error generating sample: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        // Setup Bottom Navigation
        setupBottomNavigation();
    }

    // ------------------ POST-DRAW LAYOUT ------------------
    /**
     * Sets up the post-draw layout for organizers.
     * Displays event details, sampled entrants, cancelled entrants, and allows confirming entrants or filling empty spots.
     */
    @SuppressLint("SetTextI18n")
    private void setupPostDrawLayout() {
        // --- View bindings ---
        ImageView eventImage = findViewById(R.id.eventImage);
        MaterialCardView eventImageCard = findViewById(R.id.eventImageCard);
        TextView previewEventName = findViewById(R.id.previewEventName);
        TextView previewDescription = findViewById(R.id.previewDescription);
        TextView previewGuidelines = findViewById(R.id.previewGuidelines);
        TextView previewLocation = findViewById(R.id.previewLocation);
        TextView previewTimeAndDay = findViewById(R.id.previewTimeAndDay);
        TextView previewDateRange = findViewById(R.id.previewDateRange);
        TextView previewDuration = findViewById(R.id.previewDuration);
        TextView previewPrice = findViewById(R.id.previewPrice);
        TextView previewSpotsOpen = findViewById(R.id.previewSpotsOpen);

        ImageView sampledSendCustomNotif = findViewById(R.id.SampledSendCustomNotif);
        ImageView acceptedSendCustomNotif = findViewById(R.id.AcceptedSendCustomNotif);
        ImageView cancelledSendCustomNotif = findViewById(R.id.CancelledSendCustomNotif);
        ImageView allSendCustomNotif = findViewById(R.id.AllSendCustomNotif);

        LinearLayout sampledEntrantsContainer = findViewById(R.id.sampled_entrants_container);
        LinearLayout acceptedEntrantsContainer = findViewById(R.id.accepted_entrants_container);
        LinearLayout cancelledEntrantsContainer = findViewById(R.id.cancelled_entrants_container);
        LinearLayout allEntrantsContainer = findViewById(R.id.all_entrants_container);

        Button buttonFillSpots = findViewById(R.id.button_fillSpots);
        Button buttonConfirm = findViewById(R.id.button_confirm);
        Button buttonBack = findViewById(R.id.button_BackPostDraw);

        FrameLayout mapContainer = findViewById(R.id.map_container);
        if (currentEvent.getGeolocationRequired()) {
            mapContainer.setVisibility(View.VISIBLE);

            // Create a SupportMapFragment dynamically
            SupportMapFragment mapFragment = SupportMapFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.map_container, mapFragment)
                    .commit();

            // When map is ready, plot the entrants
            mapFragment.getMapAsync(googleMap -> {
                plotEntrantsOnMap(googleMap, currentEvent.getLinkIDs(), mapContainer);
            });

        } else {
            TextView geoHeading = findViewById(R.id.GeolocationHeading);
            geoHeading.setVisibility(View.GONE);
            mapContainer.setVisibility(View.GONE);
        }

        // --- Populate Event info ---
        String imageURL = currentEvent.getImageURL();
        if (imageURL == null || imageURL.isEmpty()) {
            // Hide the ImageView if no image is available
            eventImageCard.setVisibility(View.GONE);
            eventImage.setVisibility(View.GONE);
        } else {
            // Show the ImageView
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
        previewLocation.setText(currentEvent.getLocation());
        previewTimeAndDay.setText(currentEvent.getTime());
        previewDateRange.setText(buildDateRange(currentEvent.getStartDate(), currentEvent.getEndDate()));
        previewDuration.setText(currentEvent.getDuration());
        String priceText = formatPrice(currentEvent.getPrice().toString());
        previewPrice.setText(priceText);
        String spotsOpen = (currentEvent.getCapacity() - currentEvent.getTotalWaitlist()) == 0
                ? "All spots are filled!"
                : Integer.toString(currentEvent.getCapacity() - currentEvent.getTotalWaitlist());
        previewSpotsOpen.setText(spotsOpen);

        // Populate sampled entrants

        List<String> acceptedIDs = new ArrayList<>();
        List<String> sampledIDs = new ArrayList<>();

        int total = currentEvent.getSampledIDs().size();
        AtomicInteger completed = new AtomicInteger(0);

        for (String id : currentEvent.getSampledIDs()) {
            eventUserLinkDB.getEventUserLinkByID(id, new EventUserLinkDB.GetCallback<EventUserLink>() {
                @Override
                public void onSuccess(EventUserLink eventUserLink) {
                    if (eventUserLink != null) {
                        if ("Accepted".equals(eventUserLink.getStatus())) {
                            acceptedIDs.add(id);
                        } else if ("Sampled".equals(eventUserLink.getStatus())) {
                            sampledIDs.add(id);
                        }
                    }

                    if (completed.incrementAndGet() == total) {
                        // Update UI
                        updatePostDrawUI(acceptedIDs, sampledIDs);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    if (completed.incrementAndGet() == total) {
                        updatePostDrawUI(acceptedIDs, sampledIDs);
                    }
                }
            });
        }

        populateEntrants(cancelledEntrantsContainer, currentEvent.getCancelledIDs());
        populateEntrants(allEntrantsContainer, currentEvent.getLinkIDs());

        // --- Button actions ---
        buttonBack.setOnClickListener(v -> finish());

        buttonFillSpots.setOnClickListener(v -> {
            Toast.makeText(this, "Filling remaining spots...", Toast.LENGTH_SHORT).show();
            eventDB.fillEmptySampledSpots(currentEvent, new EventDB.GetCallback<List<String>>() {
                @Override
                public void onSuccess(List<String> newlySampledUsers) {

                    // Change status of EventUserLinks to "Sampled"
                    for (String linkID : newlySampledUsers) {
                        eventUserLinkDB.getEventUserLinkByID(linkID, new EventUserLinkDB.GetCallback<EventUserLink>() {
                            @Override
                            public void onSuccess(EventUserLink eventUserLink) {
                                if (eventUserLink != null) {
                                    // Update status to "Sampled"
                                    eventUserLink.setStatus("Sampled");
                                    eventUserLinkDB.updateEventUserLink(eventUserLink, new EventUserLinkDB.ActionCallback() {
                                        @Override
                                        public void onSuccess() {
                                            ;
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            Toast.makeText(
                                                    OrganizerEventActivity.this,
                                                    "Error updating link " + linkID + ": " + e.getMessage(),
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                        }
                                    });
                                } else {
                                    Toast.makeText(
                                            OrganizerEventActivity.this,
                                            "EventUserLink not found for " + linkID,
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(
                                        OrganizerEventActivity.this,
                                        "Error fetching EventUserLink " + linkID + ": " + e.getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });
                    }

                    Toast.makeText(OrganizerEventActivity.this,
                            "Filled spots successfully! Total: " + newlySampledUsers.size(),
                            Toast.LENGTH_SHORT).show();

                    // Switch to Post-Draw layout
                    setContentView(R.layout.section2_organizereventview_postdraw);
                    setupPostDrawLayout();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(OrganizerEventActivity.this, "Error generating sample: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        buttonConfirm.setOnClickListener(v -> {
            Toast.makeText(this, "Entrants confirmed!", Toast.LENGTH_SHORT).show();
            openFinalListDialog(acceptedIDs, currentEvent);
        });

        sampledSendCustomNotif.setOnClickListener(v -> {
            showCustomMessageDialog("Sampled", sampledIDs, currentEvent);
        });

        acceptedSendCustomNotif.setOnClickListener(v -> {
            showCustomMessageDialog("Accepted", acceptedIDs, currentEvent);
        });

        cancelledSendCustomNotif.setOnClickListener(v -> {
            showCustomMessageDialog("Cancelled", currentEvent.getCancelledIDs(), currentEvent);
        });

        allSendCustomNotif.setOnClickListener(v -> {
            showCustomMessageDialog("All", currentEvent.getLinkIDs(), currentEvent);
        });

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        if (bottomNav == null) return;

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(OrganizerEventActivity.this, HomeActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }
            if (id == R.id.nav_profile) {
                startActivity(new Intent(OrganizerEventActivity.this, ProfileActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }
            if (id == R.id.nav_notifications) {
                startActivity(new Intent(OrganizerEventActivity.this, NotificationsActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }
            if (id == R.id.nav_events) {
                startActivity(new Intent(OrganizerEventActivity.this, CreateEventActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }
            if (id == R.id.nav_search) {
                Intent intent = new Intent(OrganizerEventActivity.this, SearchActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            }
            return false;
        });
    }

    // HELPER METHODS

    private void populateEntrants(LinearLayout container, List<String> linkIDs) {
        container.removeAllViews();
        if (linkIDs == null || linkIDs.isEmpty()) {
            TextView noEntrantsText = new TextView(this);
            noEntrantsText.setText("No entrants found.");
            container.addView(noEntrantsText);
            return;
        }

        int organizerId = getSharedPreferences("app_prefs", MODE_PRIVATE).getInt("userID", -1);

        for (String id : linkIDs) {
            String userID = id.split("_")[1];
            if (Integer.parseInt(userID) == organizerId) {
                continue; // Skip organizer's own profile
            }
            profileDB.getUserByID(Integer.parseInt(userID), new ProfileDB.GetCallback<UserProfile>() {
                @Override
                public void onSuccess(UserProfile profile) {
                    View blobView = LayoutInflater.from(OrganizerEventActivity.this)
                            .inflate(R.layout.admin_profile_blob, container, false);
                    ((TextView) blobView.findViewById(R.id.profileNameTextView)).setText(profile.getName());
                    ((ImageView) blobView.findViewById(R.id.profileIcon)).setImageResource(R.drawable.ic_profile);
                    container.addView(blobView);
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(OrganizerEventActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void populateSampledEntrants(LinearLayout container, List<String> linkIDs) {
        container.removeAllViews();
        if (linkIDs == null || linkIDs.isEmpty()) {
            TextView noEntrantsText = new TextView(this);
            noEntrantsText.setText("No entrants found.");
            container.addView(noEntrantsText);
            return;
        }

        int organizerId = getSharedPreferences("app_prefs", MODE_PRIVATE).getInt("userID", -1);

        for (String id : linkIDs) {
            String userID = id.split("_")[1];
            if (Integer.parseInt(userID) == organizerId) {
                continue; // Skip organizer's own profile
            }
            profileDB.getUserByID(Integer.parseInt(userID), new ProfileDB.GetCallback<UserProfile>() {
                @Override
                public void onSuccess(UserProfile profile) {
                    View blobView = LayoutInflater.from(OrganizerEventActivity.this)
                            .inflate(R.layout.sampled_user_blob, container, false);
                    ((TextView) blobView.findViewById(R.id.profileNameTextView)).setText(profile.getName());
                    ((ImageView) blobView.findViewById(R.id.profileIcon)).setImageResource(R.drawable.ic_profile);
                    ((ImageView) blobView.findViewById(R.id.cancelButton)).setImageResource(R.drawable.ic_cross);
                    container.addView(blobView);
                    // Set an OnClickListener for the cancel button
                    blobView.findViewById(R.id.cancelButton).setOnClickListener(v -> {
                        eventHandler.cancelUser(currentEvent, Integer.parseInt(userID), new ProfileDB.GetCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer result) {
                                Toast.makeText(OrganizerEventActivity.this, "User cancelled successfully", Toast.LENGTH_SHORT).show();
                                // Refresh the layout to reflect changes
                                setContentView(R.layout.section2_organizereventview_postdraw);

                                setupPostDrawLayout();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(OrganizerEventActivity.this, "Error cancelling user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(OrganizerEventActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show();
                }
            });
        }
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
        if (registrationStart != null && registrationStart.equals(registrationEnd)) {
            return registrationStart;
        } else if (registrationStart != null && registrationEnd != null && !registrationEnd.isEmpty()) {
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

    private void plotEntrantsOnMap(GoogleMap googleMap, List<String> linkIDs, FrameLayout mapContainer) {

        if (linkIDs == null || linkIDs.isEmpty()) {
            mapContainer.setVisibility(View.GONE);
            return;
        }

        int organizerID = getSharedPreferences("app_prefs", MODE_PRIVATE).getInt("userID", -1);
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        List<LatLng> allPositions = new ArrayList<>();

        final int total = linkIDs.size();
        final int[] completed = {0};

        for (String linkID : linkIDs) {

            String userIDStr = linkID.split("_")[1];
            if (Integer.parseInt(userIDStr) == organizerID) {
                completed[0]++;
                continue;
            }

            eventUserLinkDB.getEventUserLinkByID(linkID, new EventUserLinkDB.GetCallback<EventUserLink>() {
                @Override
                public void onSuccess(EventUserLink link) {
                    if (link != null && link.getLatitude() != null && link.getLongitude() != null) {
                        LatLng pos = new LatLng(link.getLatitude(), link.getLongitude());
                        googleMap.addMarker(new MarkerOptions().position(pos));
                        allPositions.add(pos);
                    }

                    completed[0]++;
                    checkIfDone();
                }

                @Override
                public void onFailure(Exception e) {
                    completed[0]++;
                    checkIfDone();
                }

                private void checkIfDone() {
                    if (completed[0] == total) {
                        if (allPositions.isEmpty()) {
                            mapContainer.setVisibility(View.GONE);
                            return;
                        }
                        for (LatLng p : allPositions) boundsBuilder.include(p);

                        LatLngBounds bounds = boundsBuilder.build();
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                    }
                }
            });
        }
    }

    // Image Upload Helper

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void uploadImageToFirebase(Uri imageUri) {

        if (imageUri == null || imageUri.getPath() == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            return;
        } else {

            PicturesDB picturesDB = new PicturesDB();

            Uri localUri = getSafeUriForUpload(imageUri);

            if (localUri != null) {

                picturesDB.uploadEventImage(localUri, eventID, new PicturesDB.Callback<String>() {
                    @Override
                    public void onSuccess(String downloadURL) {
                        new File(localUri.getPath()).delete();

                        eventDB.updateEventImageURL(currentEvent, downloadURL, new EventDB.GetCallback<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(OrganizerEventActivity.this,"Image Uploaded!", Toast.LENGTH_SHORT).show();
                                refreshEventAndSetupLayout();
                                // setupPreDrawLayout();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(OrganizerEventActivity.this, "EventDB.updateEventImageURL failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(OrganizerEventActivity.this, "PicturesDB.uploadEventImage failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

            } else {
                Toast.makeText(OrganizerEventActivity.this, "Failed to prepare image for upload", Toast.LENGTH_LONG).show();
            }

        }
    }

    private void deleteEventImage() {

        String imageURL = currentEvent.getImageURL();

        if (imageURL == null || imageURL.isEmpty()) {
            Toast.makeText(this, "No image to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        // Modern progress indicator
        AlertDialog progressDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .create();

        ProgressBar progressBar = new ProgressBar(this);
        progressDialog.setView(progressBar);

        progressDialog.show();

        StorageReference storageRef;
        try {
            storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageURL);
        } catch (Exception e) {
            progressDialog.dismiss();
            Toast.makeText(this, "Invalid image URL", Toast.LENGTH_SHORT).show();
            return;
        }

        storageRef.delete()
                .addOnSuccessListener(unused -> {

                    // Update ONLY the imageURL field in Firestore
                    eventDB.updateEventImageURL(currentEvent, null,
                            new EventDB.GetCallback<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();

                                    Toast.makeText(OrganizerEventActivity.this,
                                            "Image deleted", Toast.LENGTH_SHORT).show();

                                    refreshEventAndSetupLayout();
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(OrganizerEventActivity.this,
                                            "Failed to update event: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private Uri getSafeUriForUpload(Uri originalUri) {
        if (originalUri == null) return null;

        try {
            Context context = OrganizerEventActivity.this;
            if (context == null) return null;

            File tempFile = new File(
                    context.getCacheDir(),
                    "event_upload_" + System.currentTimeMillis() + ".png"
            );

            try (InputStream in = context.getContentResolver().openInputStream(originalUri);
                 OutputStream out = new FileOutputStream(tempFile)) {

                if (in == null) return null;

                byte[] buffer = new byte[1024];
                int read;

                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            }

            return Uri.fromFile(tempFile);

        } catch (Exception e) {
            Log.e("CreateEventHandler", "Error creating safe URI", e);
            return null;
        }
    }

    private void refreshEventAndSetupLayout() {
        eventDB.getEventByID(eventID, new EventDB.GetCallback<Event>() {
            @Override
            public void onSuccess(Event updatedEvent) {
                // Update currentEvent with fresh data
                currentEvent = updatedEvent;
                Log.d("CreateEventHandler", "Refreshed event: " + updatedEvent);

                // Re-setup the current layout
                setupPreDrawLayout();

            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(OrganizerEventActivity.this,
                        "Failed to refresh event: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCustomMessageDialog(String status, List<String> targetLinkIDs, Event event) {
        // Inflate custom layout
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.custom_message_popout, null);

        EditText messageEditText = dialogView.findViewById(R.id.customMessageEditText);

        new androidx.appcompat.app.AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setView(dialogView)
                .setCancelable(true)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String message = messageEditText.getText().toString().trim();

                    if (message.isEmpty()) {
                        Toast.makeText(this, "Message cannot be empty.", Toast.LENGTH_SHORT).show();
                        return; // do nothing else
                    }

                    eventHandler.sendCustomNotification(message, status, targetLinkIDs, event, new EventUserLinkDB.ActionCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(OrganizerEventActivity.this, "Custom notification sent to " + status + " users.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(OrganizerEventActivity.this, "Failed to send notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void updatePostDrawUI(List<String> acceptedIDs, List<String> sampledIDs) {
        Log.d("OrganizerEventActivity", "Final accepted IDs: " + acceptedIDs);

        populateSampledEntrants(findViewById(R.id.sampled_entrants_container), sampledIDs);
        populateEntrants(findViewById(R.id.accepted_entrants_container), acceptedIDs);

    }

    private void openFinalListDialog(List<String> acceptedIDs, Event event) {
        // Inflate your custom layout
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.confirm_final_list_verification, null);

        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        androidx.appcompat.app.AlertDialog dialog =
                new androidx.appcompat.app.AlertDialog.Builder(this, R.style.CustomAlertDialog)
                        .setView(dialogView)
                        .setCancelable(true)
                        .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            eventHandler.confirmEntrants(acceptedIDs, event, new EventUserLinkDB.ActionCallback() {
                @Override
                public void onSuccess() {
                    // Proceed to show final list
                    // Inflate custom layout
                    View finalListView = LayoutInflater.from(OrganizerEventActivity.this)
                            .inflate(R.layout.section3_final_list_activity, null);
                    RecyclerView finalListRecycler = finalListView.findViewById(R.id.finalParticipantRecycler);
                    finalListRecycler.setLayoutManager(new LinearLayoutManager(OrganizerEventActivity.this));
                    FinalParticipantAdapter adapter = new FinalParticipantAdapter(new ArrayList<>(), OrganizerEventActivity.this);
                    finalListRecycler.setAdapter(adapter);
                    // Fetch profiles for acceptedIDs
                    List<UserProfile> acceptedProfiles = new ArrayList<>();
                    final int total = acceptedIDs.size();
                    final int[] completed = {0};

                    for (String linkID : acceptedIDs) {
                        String userIDStr = linkID.split("_")[1];
                        profileDB.getUserByID(Integer.parseInt(userIDStr), new ProfileDB.GetCallback<UserProfile>() {
                            @Override
                            public void onSuccess(UserProfile profile) {
                                acceptedProfiles.add(profile);
                                completed[0]++;
                                checkIfDone();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                completed[0]++;
                                checkIfDone();
                            }

                            private void checkIfDone() {
                                if (completed[0] == total) {
                                    // All profiles fetched
                                    adapter.updateProfiles(acceptedProfiles);
                                }
                            }
                        });
                    }

                    Button buttonExportCSV = finalListView.findViewById(R.id.exportCsvButton);

                    // Show the final list dialog
                    androidx.appcompat.app.AlertDialog finalListDialog =
                            new androidx.appcompat.app.AlertDialog.Builder(OrganizerEventActivity.this, R.style.CustomAlertDialog)
                                    .setView(finalListView)
                                    .setCancelable(true)
                                    .setNegativeButton("Close", (d, w) -> d.dismiss())
                                    .create();
                    finalListDialog.show();

                    buttonExportCSV.setOnClickListener(v2 -> {
                        eventHandler.exportEntrantsToCSV(acceptedIDs, event.getName(), new EventActionHandler.ExportCallback() {
                            @Override
                            public void onSuccess(String csvData) {
                                csvDataToSave = csvData;
                                String defaultFileName = event.getName().replaceAll("\\s+", "_") + "_Final_Entrants.csv";

                                // Open file picker to save CSV
                                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                                intent.setType("text/csv");
                                intent.putExtra(Intent.EXTRA_TITLE, defaultFileName);
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                createCsvLauncher.launch(intent);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(OrganizerEventActivity.this,
                                        "Failed to export CSV: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(OrganizerEventActivity.this,
                            "Failed to confirm entrants: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });


            Toast.makeText(this, "Entrants confirmed!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private static class FinalParticipantAdapter extends RecyclerView.Adapter<FinalParticipantAdapter.ViewHolder> {
        private List<UserProfile> profiles;
        private final Context context;

        public FinalParticipantAdapter(List<UserProfile> profiles, Context context) {
            this.profiles = profiles;
            this.context = context;
        }

        public void updateProfiles(List<UserProfile> newProfiles) {
            this.profiles = newProfiles;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.admin_profile_blob, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            UserProfile profile = profiles.get(position);
            holder.nameTextView.setText(profile.getName());
            // Load profile image if available
            Glide.with(context)
                    .load(R.drawable.ic_profile)
                    .into(holder.profileImageView);
        }

        @Override
        public int getItemCount() {
            return profiles.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView nameTextView;
            public ImageView profileImageView;

            public ViewHolder(View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.profileNameTextView);
                profileImageView = itemView.findViewById(R.id.profileIcon);
            }
        }
    }
}