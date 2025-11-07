package com.example.hotpot0.section2.views;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;

public class PreviewActivity extends AppCompatActivity {

    private TextView tvEventName, tvDescription, tvGuidelines, tvLocation, tvTimeAndDay, tvDateRange, tvDuration, tvPrice, tvSpotsOpen, tvDaysLeft;
    private ImageView eventImage;
    private Button confirmButton, backButton;
    private TextView tvGeolocationStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section2_previewevent_activity);

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
        tvGeolocationStatus = findViewById(R.id.tvGeolocationStatus);

        confirmButton = findViewById(R.id.button_confirm);
        backButton = findViewById(R.id.button_BottomBackPreviewEvent);

        // Get event data
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            tvEventName.setText(extras.getString("name", ""));
            tvDescription.setText(extras.getString("description", ""));
            tvGuidelines.setText(extras.getString("guidelines", ""));
            tvLocation.setText(getString(R.string.event_location, extras.getString("location", "")));
            tvTimeAndDay.setText(getString(R.string.event_time, extras.getString("time", "")));
            tvDateRange.setText(getString(R.string.event_date, extras.getString("date", "")));
            tvDuration.setText(getString(R.string.event_duration, extras.getString("duration", "")));
            tvPrice.setText(getString(R.string.event_price, extras.getString("price", "")));
            tvSpotsOpen.setText(getString(R.string.event_capacity, extras.getString("capacity", "")));
            tvDaysLeft.setText(getString(R.string.event_registration, extras.getString("registration", "")));

            boolean geolocationEnabled = getIntent().getBooleanExtra("geolocationEnabled", false);

            tvGeolocationStatus.setVisibility(View.VISIBLE);
            tvGeolocationStatus.setText(getString(R.string.event_geolocation, geolocationEnabled ? "Enabled" : "Disabled"));
        }

        confirmButton.setOnClickListener(v -> {
            Toast.makeText(this, "Event created successfully!", Toast.LENGTH_LONG).show();
            finish();
        });

        // Back button
        backButton.setOnClickListener(v -> finish());
    }
}
