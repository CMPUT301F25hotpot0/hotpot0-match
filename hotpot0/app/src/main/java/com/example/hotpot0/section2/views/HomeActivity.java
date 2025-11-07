package com.example.hotpot0.section2.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;
import com.example.hotpot0.models.ProfileDB;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int userID = getSharedPreferences("app_prefs", MODE_PRIVATE).getInt("userID", -1);
        Toast .makeText(this, "Welcome User #" + userID, Toast.LENGTH_SHORT).show();
        setContentView(R.layout.section2_userhome_activity);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_events) {
                Intent intent = new Intent(HomeActivity.this, CreateEventActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }

            if (id == R.id.nav_search) {
                Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }

            if (id == R.id.nav_notifications) {
                Intent intent = new Intent(HomeActivity.this, NotificationsActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }

            if (id == R.id.nav_profile) {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }

            // PUT THE OTHER TOOLBAR ITEMS HERE
            return false;
        });
    }
}
