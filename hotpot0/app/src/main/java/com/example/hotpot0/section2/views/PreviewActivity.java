package com.example.hotpot0.section2.views;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;

public class PreviewActivity extends AppCompatActivity {

    private TextView previewEventName, previewDescription, previewGuidelines, previewLocation, previewTimeAndDay, previewDateRange, previewDuration, previewPrice, previewSpotsOpen, previewDaysLeft;
    private ImageView eventImage;
    private Button confirmButton, backButton;
    private TextView GeolocationStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section2_previewevent_activity);

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

        // Get event data
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

        confirmButton.setOnClickListener(v -> {
            Toast.makeText(this, "Event created successfully!", Toast.LENGTH_LONG).show();
            finish();
        });

        // Back button
        backButton.setOnClickListener(v -> finish());
    }
}
