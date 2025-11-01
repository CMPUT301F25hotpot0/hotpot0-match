package com.example.hotpot0.section2.views;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.models.ProfileDB;
import com.example.hotpot0.models.UserProfile;

public class HomeActivity extends AppCompatActivity {

    private ProfileDB profileDB;
    private UserProfile currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        profileDB = new ProfileDB();

        // Get the userID passed from MainActivity
        int userID = getIntent().getIntExtra("userID", -1);
        if (userID == -1) {
            Toast.makeText(this, "No user info found", Toast.LENGTH_SHORT).show();
            finish(); // close activity if no userID
            return;
        }

        // Fetch the full UserProfile from Firestore
        profileDB.getUserByID(userID, new ProfileDB.GetCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile user) {
                currentUser = user;
                setupUI(); // populate the UI with user data
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(HomeActivity.this, "Failed to load user profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupUI() {
        // For now, just show a welcome message with the user's name
        TextView tv = new TextView(this);
        tv.setText("Welcome, " + currentUser.getName() + "!");
        tv.setTextSize(24);
        setContentView(tv);
    }
}