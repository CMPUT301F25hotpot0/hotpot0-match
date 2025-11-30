package com.example.hotpot0.section2.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.hotpot0.R;
import com.example.hotpot0.models.ProfileDB;
import com.example.hotpot0.models.UserProfile;
import com.example.hotpot0.section1.views.StartupActivity;
import com.example.hotpot0.section2.controllers.ProfileEditHandler;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Activity that displays the user's profile and allows the user to:
 * <ul>
 *     <li>Edit profile details (name, email, phone)</li>
 *     <li>Toggle notification and location preferences</li>
 *     <li>Delete the profile</li>
 * </ul>
 * <p>
 * Also provides bottom navigation to other sections of the app.
 * </p>
 */
public class ProfileActivity extends AppCompatActivity{
    private TextInputEditText nameInput, emailInput, phoneInput;
    private MaterialButton saveProfileButton, deleteProfileButton;
    private SwitchCompat notificationSwitch, locationSwitch;
    private BottomNavigationView bottomNavigationView;
    private ProfileEditHandler profileHandler = new ProfileEditHandler();
    private final ProfileDB profileDB = new ProfileDB();
    private int userID;

    /**
     * Called when the activity is first created.
     * Initializes UI components, loads the user profile, and sets up event listeners.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     *                           shut down, this contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section2_profile_activity);

        userID = getSharedPreferences("app_prefs", MODE_PRIVATE).getInt("userID", -1);
        if (userID == -1) {
            Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, StartupActivity.class));
            finish();
            return;
        }

        // Initialize UI components
        nameInput = findViewById(R.id.edit_name_input);
        emailInput = findViewById(R.id.edit_email_input);
        phoneInput = findViewById(R.id.edit_phone_input);
        notificationSwitch = findViewById(R.id.toggle_notif);
        locationSwitch = findViewById(R.id.toggle_location);
        saveProfileButton = findViewById(R.id.save_profile_button);
        deleteProfileButton = findViewById(R.id.delete_profile_button);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        setupBottomNavigationView();

        loadUserProfile();

        saveProfileButton.setOnClickListener(v -> saveUserProfile());
        deleteProfileButton.setOnClickListener(v -> deleteUserProfile());

        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                String message = isChecked ? "Notifications Enabled" : "Notifications Disabled";
            }
        });

        locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                String message = isChecked ? "Location Enabled" : "Location Disabled";
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
     * Loads the current user's profile data from the database and updates the UI.
     * <p>
     * Populates the name, email, phone number, and notification switch based on the stored profile.
     * If the profile cannot be loaded, a toast message is shown.
     * </p>
     */
    private void loadUserProfile() {
        profileDB.getUserByID(userID, new ProfileDB.GetCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile user) {
                if (user != null) {
                    nameInput.setText(user.getName());
                    emailInput.setText(user.getEmailID());
                    if (phoneInput != null) {
                        phoneInput.setText(user.getPhoneNumber());
                    }
                    notificationSwitch.setChecked(
                            user.getNotificationsEnabled() != null && user.getNotificationsEnabled()
                    );
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ProfileActivity.this, "Failed to load profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Saves the user's profile after editing.
     * <p>
     * Validates the input fields and calls {@link ProfileEditHandler#handleProfileUpdate} to update
     * the profile in the database. Displays toast messages for success or failure.
     * </p>
     */
    private void saveUserProfile() {
        String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        String phone = phoneInput.getText() != null ? phoneInput.getText().toString().trim() : "";
        Boolean notificationsEnabled = notificationSwitch.isChecked();

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please enter both name and email.", Toast.LENGTH_SHORT).show();
            return;
        }

        profileHandler.handleProfileUpdate(this, userID, name, email, phone, notificationsEnabled, new ProfileDB.ActionCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(ProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(ProfileActivity.this, "Update failed.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * Deletes the user's profile from the database and clears the saved user ID.
     * <p>
     * Displays a toast message upon success or failure.
     * After deletion, the user's ID is removed from shared preferences.
     * </p>
     */
    private void deleteUserProfile() {
        profileHandler.deleteUserProfile(userID, new ProfileDB.ActionCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(ProfileActivity.this, "Profile deleted!", Toast.LENGTH_SHORT).show();

                // Clear saved user ID
                getSharedPreferences("app_prefs", MODE_PRIVATE).edit().remove("userID").apply();

                // Navigate to StartupActivity
                Intent intent = new Intent(ProfileActivity.this, StartupActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();

            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ProfileActivity.this, "Profile deletion failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNavigationView() {
        // Highlight the current tab
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        // Set a single listener for all navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            }

            if (id == R.id.nav_search) {
                startActivity(new Intent(ProfileActivity.this, SearchActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            }

            if (id == R.id.nav_notifications) {
                startActivity(new Intent(ProfileActivity.this, NotificationsActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            }

            if (id == R.id.nav_events) {
                startActivity(new Intent(ProfileActivity.this, CreateEventActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            }

            if (id == R.id.nav_profile) {
                // Already on this activity, do nothing
                return true;
            }

            return false;
        });
    }
}



