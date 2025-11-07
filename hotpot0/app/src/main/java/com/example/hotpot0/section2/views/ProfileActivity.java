package com.example.hotpot0.section2.views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.hotpot0.R;
import com.example.hotpot0.models.ProfileDB;
import com.example.hotpot0.section1.views.StartupActivity;
import com.example.hotpot0.section2.controllers.ProfileEditHandler;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity{

    private Toolbar toolbar;
    private TextInputEditText nameInput, emailInput, phoneInput;
    private MaterialButton saveProfileButton, deleteProfileButton;
    private Switch notificationSwitch;
    private BottomNavigationView bottomNavigationView;
    private ProfileEditHandler profileHandler = new ProfileEditHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section2_profile);

        int userID = getSharedPreferences("app_prefs", MODE_PRIVATE).getInt("userID", -1);

        // Initialize UI components
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        notificationSwitch = findViewById(R.id.switch1);
        saveProfileButton = findViewById(R.id.saveProfileButton);
        deleteProfileButton = findViewById(R.id.deleteProfileButton);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set up switch behavior
        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                String message = isChecked
                        ? "Notifications/Location Enabled"
                        : "Notifications/Location Disabled";
            }
        });

        // Save profile button
        saveProfileButton.setOnClickListener(v -> {
            String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
            String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
            String phone = phoneInput.getText() != null ? phoneInput.getText().toString().trim() : "";
            Boolean notificationsEnabled = notificationSwitch.isChecked();

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please enter both name and email", Toast.LENGTH_SHORT).show();
                return;
            }

            profileHandler.handleProfileUpdate(this, userID, name, email, phone, notificationsEnabled, new ProfileDB.ActionCallback() {
                        @Override
                        public void onSuccess() {
                            // Handle success, e.g., show a Toast
                            Toast.makeText(getApplicationContext(), "Profile updated!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            // Handle failure
                            Toast.makeText(getApplicationContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        });

        // Delete profile button
        deleteProfileButton.setOnClickListener(v -> {
            profileHandler.deleteUserProfile(userID, new ProfileDB.ActionCallback() {
                @Override
                public void onSuccess() {
                    // Handle success, e.g., show a Toast
                    Toast.makeText(getApplicationContext(), "Profile deleted!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ProfileActivity.this, StartupActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }

                @Override
                public void onFailure(Exception e) {
                    // Handle failure
                    Toast.makeText(getApplicationContext(), "Deletion failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            nameInput.setText("");
            emailInput.setText("");
            phoneInput.setText("");
            notificationSwitch.setChecked(false);
        });

        // Bottom navigation setup
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                } else if (id == R.id.nav_search) {
                    Intent intent = new Intent(ProfileActivity.this, SearchActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                } else if (id == R.id.nav_notifications) {
                    Intent intent = new Intent(ProfileActivity.this, NotificationsActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                } else if (id == R.id.nav_events) {
                    Intent intent = new Intent(ProfileActivity.this, CreateEventActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                } else if (id == R.id.nav_profile) {
                    return true;
                }

                return false;
            }
        });
    }
}



