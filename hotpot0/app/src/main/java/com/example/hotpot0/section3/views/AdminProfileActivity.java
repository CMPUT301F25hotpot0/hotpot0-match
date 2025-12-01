package com.example.hotpot0.section3.views;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;
import com.example.hotpot0.models.ProfileDB;
import com.example.hotpot0.models.UserProfile;
import com.example.hotpot0.section2.controllers.ProfileEditHandler;

public class AdminProfileActivity extends AppCompatActivity {

    private TextView profileName, profileEmail, profilePhone, profileUserID;
    private Button removeProfileButton;
    private ImageButton backButton;

    private ProfileDB profileDB;
    private UserProfile currentProfile;
    private int profileID;
    private ProfileEditHandler profileHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section3_manageprofile_activity);

        // Views
        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        profilePhone = findViewById(R.id.profilePhone);
        profileUserID = findViewById(R.id.profileUserID);
        removeProfileButton = findViewById(R.id.removeProfileButton);
        backButton = findViewById(R.id.backButton);

        profileDB = new ProfileDB();
        profileHandler = new ProfileEditHandler();

        // Get profileID from intent
        profileID = getIntent().getIntExtra("profileID", -1);
        if (profileID == -1) {
            Toast.makeText(this, "No profile ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadProfile();

        backButton.setOnClickListener(v -> finish());

        // Remove button
        removeProfileButton.setOnClickListener(v -> {
            new AlertDialog.Builder(AdminProfileActivity.this)
                    .setTitle("Remove Profile")
                    .setMessage("Are you sure you want to remove this profile?")
                    .setPositiveButton("Yes", (dialog, which) -> removeProfile())
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void loadProfile() {
        profileDB.getUserByID(profileID, new ProfileDB.GetCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile profile) {
                currentProfile = profile;
                updateUI();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateUI() {
        if (currentProfile != null) {
            profileName.setText(currentProfile.getName());
            profileEmail.setText(currentProfile.getEmailID());
            profilePhone.setText(currentProfile.getPhoneNumber());
            profileUserID.setText(String.valueOf(currentProfile.getUserID()));
        }
    }

    private void removeProfile() {
        profileHandler.deleteUserProfile(currentProfile.getUserID(), new ProfileDB.ActionCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(AdminProfileActivity.this, "Profile removed successfully", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminProfileActivity.this, "Failed to remove profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

