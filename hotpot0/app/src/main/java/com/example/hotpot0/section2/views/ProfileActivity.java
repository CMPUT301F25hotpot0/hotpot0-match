package com.example.hotpot0.section2.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;
import com.example.hotpot0.models.ProfileDB;
import com.example.hotpot0.models.UserProfile;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity{

    private TextInputEditText nameInput, emailInput, phoneInput;
    private MaterialButton saveProfileButton, deleteProfileButton;
    private Switch notificationSwitch, locationSwitch;
    private BottomNavigationView bottomNavigationView;
    private ProfileDB profileDB;
    private int userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section2_profile_activity);

        // Initialize UI components
        nameInput = findViewById(R.id.edit_name_input);
        emailInput = findViewById(R.id.edit_email_input);
        phoneInput = findViewById(R.id.edit_phone_input);
        notificationSwitch = findViewById(R.id.toggle_notif);
        locationSwitch = findViewById(R.id.toggle_location);
        saveProfileButton = findViewById(R.id.save_profile_button);
        deleteProfileButton = findViewById(R.id.delete_profile_button);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        profileDB = new ProfileDB();

        userID = getIntent().getIntExtra("userID", -1);
        if (userID == -1) {
            Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadUserProfile();
        saveProfileButton.setOnClickListener(view -> saveUserProfile());
        deleteProfileButton.setOnClickListener(view -> deleteUserProfile());

        // Set up notification switch behavior
        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isOn) {
                String message = isOn
                        ? "Notifications Enabled"
                        : "Notifications Disabled";
            }
        });

        // Set up geo-location switch behavior
        locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isOn) {
                String message = isOn
                        ? "Location Enabled"
                        : "Location Disabled";
            }
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

    /**
     * This methods get the information of an existing user
     * from the database and displays it on the Profile tab
     */
    private void loadUserProfile() {
        profileDB.getUserByID(userID, new ProfileDB.GetCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile user) {
                nameInput.setText(user.getName());
                emailInput.setText(user.getEmailID());
                phoneInput.setText(user.getPhoneNumber());
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ProfileActivity.this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * This methods saves the information of an existing user
     * who updated their profile to their database
     */
    private void saveUserProfile() {
        String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        String phone = phoneInput.getText() != null ? phoneInput.getText().toString().trim() : "";

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        UserProfile updatedUser = new UserProfile();
        updatedUser.setUserID(userID);
        updatedUser.setName(name);
        updatedUser.setEmailID(email);
        updatedUser.setPhoneNumber(phone);

        profileDB.updateUser(updatedUser, new ProfileDB.ActionCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(ProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ProfileActivity.this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * This methods deletes the profile of a user
     */
    private void deleteUserProfile() {
        profileDB.deleteUser(userID, new ProfileDB.ActionCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(ProfileActivity.this, "Profile deleted successfully", Toast.LENGTH_SHORT).show();
                nameInput.setText("");
                emailInput.setText("");
                phoneInput.setText("");
                notificationSwitch.setChecked(false);
                locationSwitch.setChecked(false);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ProfileActivity.this, "Failed to delete profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}



