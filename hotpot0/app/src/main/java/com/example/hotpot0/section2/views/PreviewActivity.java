package com.example.hotpot0.section2.views;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;

public class PreviewActivity extends AppCompatActivity {

    private TextView tvEventName, tvDescription, tvGuidelines, tvLocation, tvTimeAndDay, tvDateRange, tvPrice, tvSpotsOpen, tvDaysLeft;
    private ImageView eventImage;
    private Button confirmButton, backButton;

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
        tvPrice = findViewById(R.id.tvPrice);
        tvSpotsOpen = findViewById(R.id.tvSpotsOpen);
        tvDaysLeft = findViewById(R.id.tvDaysLeft);

        confirmButton = findViewById(R.id.button_confirm);
        backButton = findViewById(R.id.button_BottomBackPreviewEvent);

        // Get event data
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            tvEventName.setText(extras.getString("name", ""));
            tvDescription.setText(extras.getString("description", ""));
            tvGuidelines.setText(extras.getString("guidelines", ""));
            tvLocation.setText("Location " + extras.getString("location", ""));
            tvTimeAndDay.setText("Time " + extras.getString("time", ""));
            tvDateRange.setText("Date " + extras.getString("date", ""));
            tvPrice.setText("Price " + extras.getString("price", "") + " CAD");
            tvSpotsOpen.setText("Capacity: " + extras.getString("capacity", ""));
            tvDaysLeft.setText("Registration Period: " + extras.getString("registration", ""));
        }

        confirmButton.setOnClickListener(v -> {
            Toast.makeText(this, "Event created successfully!", Toast.LENGTH_LONG).show();
            finish();
        });

        // Back button
        backButton.setOnClickListener(v -> finish());
    }
}
