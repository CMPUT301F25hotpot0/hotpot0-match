package com.example.hotpot0.section2.views;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;
import com.example.hotpot0.section2.controllers.EventActivityController;

public class EventBlobActivity extends AppCompatActivity {

    private TextView eventName, eventRole;
    private EventActivityController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_blob); // Layout for EventBlobActivity

        eventName = findViewById(R.id.event_name);
        eventRole = findViewById(R.id.event_role);
        controller = new EventActivityController(this); // Initialize controller

        // Retrieve event details passed via intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String eventNameText = extras.getString("eventName", "Event Name");
            String eventRoleText = extras.getString("eventRole", "Role");
            int eventID = extras.getInt("eventID");
            int userID = extras.getInt("userID");

            eventName.setText(eventNameText);
            eventRole.setText(eventRoleText);

            // Set onClick to navigate based on the event's status
            eventName.setOnClickListener(v -> {
                // Use the controller to handle navigation
                controller.navigateToEventActivity(eventID, userID);
            });
        }
    }
}


