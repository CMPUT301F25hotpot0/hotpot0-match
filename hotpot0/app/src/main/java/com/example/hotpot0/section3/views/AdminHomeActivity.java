package com.example.hotpot0.section3.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminHomeActivity extends AppCompatActivity {

    // Stats TextViews
    private TextView activeEventsCount, activeProfilesCount, activeOrganizersCount, imagesStoredCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Use your XML layout
        setContentView(R.layout.section3_adminhome_activity);

        // Retrieve adminID from the Intent if needed
        int adminID = getIntent().getIntExtra("adminID", -1);

        // Initialize TextViews
        activeEventsCount = findViewById(R.id.activeEventsCount);
        activeProfilesCount = findViewById(R.id.activeProfilesCount);
        activeOrganizersCount = findViewById(R.id.activeOrganizersCount);
        imagesStoredCount = findViewById(R.id.imagesStoredCount);

        // Optional: set default values or fetch from DB
        activeEventsCount.setText("55");        // Replace with real data
        activeProfilesCount.setText("120");     // Replace with real data
        activeOrganizersCount.setText("25");    // Replace with real data
        imagesStoredCount.setText("50");        // Replace with real data

        // Set Home as selected by default
        BottomNavigationView bottomNav = findViewById(R.id.adminBottomNavigationView);

        // Optional: highlight "Home" as selected initially
        bottomNav.setSelectedItemId(R.id.admin_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.admin_home) {
                // Already on home
                return true;
            } else if (id == R.id.admin_search) {
                Intent searchIntent = new Intent(AdminHomeActivity.this, AdminSearchActivity.class);
                startActivity(searchIntent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                return true;
//            } else if (id == R.id.admin_images) {
//                startActivity(new Intent(AdminHomeActivity.this, AdminSearchActivity.class));
//                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
//                return true;
//            } else if (id == R.id.admin_settings) {
//                startActivity(new Intent(AdminHomeActivity.this, AdminSettingsActivity.class));
//                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
//                return true;
            }
            return false;
        });
    }
}