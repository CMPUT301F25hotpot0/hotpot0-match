package com.example.hotpot0.section2.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * This view shows the notifications when a user wins or loses a draw
 */
public class EventSampledActivity extends AppCompatActivity {
    private Button acceptButton, declineButton, stayButton, backButton;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user won or lost draw
        boolean drawResult = getIntent().getBooleanExtra("drawResult", false);
        if (drawResult) {
            setContentView(R.layout.section2_invitationresponse_activity);
        } else {
            setContentView(R.layout.section2_rejectionresponse_activity);
        }

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        backButton = findViewById(R.id.button_BackInvitationResponse);

        backButton.setOnClickListener(v -> finish());

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_events) {
                Intent intent = new Intent(this, CreateEventActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }

            if (id == R.id.nav_search) {
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }

            if (id == R.id.nav_home) {
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }

            if (id == R.id.nav_profile) {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }

            return false;
        });
    }
}
