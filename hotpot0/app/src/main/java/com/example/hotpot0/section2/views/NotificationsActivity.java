package com.example.hotpot0.section2.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.hotpot0.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

/**
 * This view shows the Notifications the user receives. It shows current invitations
 * and other updates.
 * */
public class NotificationsActivity extends AppCompatActivity {
    private ListView currentInvitationsList, otherUpdatesList;
    private BottomNavigationView bottomNavigationView;
    private ArrayAdapter<String> currentInvitationsAdapter;
    private ArrayAdapter<String> otherUpdatesAdapter;
    private List<String> currentInvitations;
    private List<String> otherUpdates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section2_notifications_activity);

        // Initialize UI elements
        currentInvitationsList = findViewById(R.id.current_invitations_list);
        otherUpdatesList = findViewById(R.id.other_updates_list);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set adapters for ListViews
        currentInvitationsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, currentInvitations);
        currentInvitationsList.setAdapter(currentInvitationsAdapter);

        otherUpdatesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, otherUpdates);
        otherUpdatesList.setAdapter(otherUpdatesAdapter);

        // Handle ListView item clicks
        currentInvitationsList.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = currentInvitations.get(position);
            // want it to do something??
        });

        otherUpdatesList.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = otherUpdates.get(position);
            // want it to do something?
        });

        // Bottom Navigation behavior
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    Intent intent = new Intent(NotificationsActivity.this, HomeActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                } else if (id == R.id.nav_profile) {
                    Intent intent = new Intent(NotificationsActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                } else if (id == R.id.nav_search) {
                    Intent intent = new Intent(NotificationsActivity.this, SearchActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                } else if (id == R.id.nav_events) {
                    Intent intent = new Intent(NotificationsActivity.this, CreateEventActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                } else if (id == R.id.nav_notifications) {
                    return true;
                }

                return false;
            }
        });
    }

}
