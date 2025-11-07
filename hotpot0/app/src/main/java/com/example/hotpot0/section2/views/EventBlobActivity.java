package com.example.hotpot0.section2.views;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;

public class EventBlobActivity extends AppCompatActivity {

    private TextView eventName, eventRole;
    private ImageView arrowIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_blob);

        eventName = findViewById(R.id.event_name);
        eventRole = findViewById(R.id.event_role);
//        arrowIcon = findViewById(R.id.event_role); // optional, adjust if you want click behavior

        // Retrieve and display data (if passed)
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            eventName.setText(extras.getString("eventName", "Event Name"));
            eventRole.setText(extras.getString("eventRole", "Role"));
        }

        // Example click
        eventName.setOnClickListener(v -> {
            // open preview or detailed event page
        });
    }
}

