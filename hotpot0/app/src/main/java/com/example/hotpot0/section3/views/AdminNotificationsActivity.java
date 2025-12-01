package com.example.hotpot0.section3.views;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminNotificationsActivity extends AppCompatActivity{
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section3_adminnotif_activity);

        BottomNavigationView bottomNav = findViewById(R.id.adminBottomNavigationView);

        bottomNav.setSelectedItemId(R.id.admin_notif);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.admin_notif) {
                // Already on settings
                return true;
            } else if (id == R.id.admin_search) {
                Intent searchIntent = new Intent(AdminNotificationsActivity.this, AdminSearchActivity.class);
                startActivity(searchIntent);
//                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                return true;
            } else if (id == R.id.admin_home) {
                startActivity(new Intent(AdminNotificationsActivity.this, AdminHomeActivity.class));
//                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                return true;
            } else if (id == R.id.admin_settings) {
                startActivity(new Intent(AdminNotificationsActivity.this, AdminSettingsActivity.class));
//                overridePendingTransition(android.R.anim.slide_out_right, android.R.anim.slide_in_left);
                return true;
            } else if (id == R.id.admin_images) {
                startActivity(new Intent(AdminNotificationsActivity.this, AdminImageActivity.class));
//                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                return true;
            }
            return false;
        });
    }
}


