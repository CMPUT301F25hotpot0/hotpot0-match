package com.example.hotpot0.section3.views;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.hotpot0.R;
import com.example.hotpot0.models.Event;
import com.example.hotpot0.models.EventDB;
import com.example.hotpot0.models.ProfileDB;
import com.google.android.material.button.MaterialButton;

public class ManageEventActivity extends AppCompatActivity {

    private EventDB eventDB;
    private Integer eventID;
    private Event loadedEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section3_manageevent_activity);

        eventDB = new EventDB();

        eventID = getIntent().getIntExtra("eventID", -1);

        setupBackButton();
        loadEvent();
    }

    private void setupBackButton() {
        ImageButton back = findViewById(R.id.backButton);
        back.setOnClickListener(v -> finish());
    }

    private void loadEvent() {
        eventDB.getEventByID(eventID, new EventDB.GetCallback<Event>() {
            @Override
            public void onSuccess(Event event) {
                loadedEvent = event;
                fillUI(event);
                setupRemoveButton();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ManageEventActivity.this, "Failed to load event.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void fillUI(Event e) {

        ((TextView) findViewById(R.id.manageEventTitle)).setText(e.getName());

        ImageView img = findViewById(R.id.eventImage);
        if (e.getImageURL() != null && !e.getImageURL().isEmpty()) {
            Glide.with(this).load(e.getImageURL()).into(img);
        }

        ((TextView) findViewById(R.id.previewDescription)).setText(e.getDescription());
        ((TextView) findViewById(R.id.previewGuidelines)).setText(e.getGuidelines());
        ((TextView) findViewById(R.id.previewLocation)).setText(e.getLocation());
        ((TextView) findViewById(R.id.previewTimeAndDay)).setText(e.getTime());
        ((TextView) findViewById(R.id.previewDateRange))
                .setText(e.getStartDate() + " - " + e.getEndDate());
        ((TextView) findViewById(R.id.previewDuration)).setText(e.getDuration());
        ((TextView) findViewById(R.id.previewPrice)).setText("$" + e.getPrice());
        ((TextView) findViewById(R.id.previewSpotsOpen))
                .setText(e.getCapacity() + " spots");
        ((TextView) findViewById(R.id.previewWaitingList))
                .setText(e.getTotalWaitlist() + " users");
        ((TextView) findViewById(R.id.previewDaysLeft))
                .setText(e.getRegistrationStart() + " → " + e.getRegistrationEnd());

        if (e.getGeolocationRequired() != null && e.getGeolocationRequired()) {
            findViewById(R.id.GeolocationStatus).setVisibility(View.VISIBLE);
        }
    }

    private void setupRemoveButton() {
        MaterialButton removeBtn = findViewById(R.id.removeEventButton);

        removeBtn.setOnClickListener(v -> {
            eventDB.deleteEvent(eventID, new ProfileDB.ActionCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(ManageEventActivity.this, "Event removed", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(ManageEventActivity.this, "Failed to remove event", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}

