package com.example.hotpot0.section1.views;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;
import com.example.hotpot0.models.ProfileDB;
import com.example.hotpot0.models.UserProfile;

public class MainActivity extends AppCompatActivity {

    private ProfileDB profileDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Show loading layout while we check the database
        setContentView(R.layout.activity_main);

        // Initialize ProfileDB
        profileDB = new ProfileDB();

        // Get device ID
        String deviceID = getDeviceID();

        // Check if a user with this deviceID exists
        profileDB.getUserByDeviceID(deviceID, new ProfileDB.GetCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile user) {
                // Device exists → go to HomeActivity
                Intent intent = new Intent(MainActivity.this, com.example.hotpot0.section2.views.HomeActivity.class);
                intent.putExtra("userID", user.getUserID()); // Pass user info
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                // Device does not exist → go to StartupActivity
                Intent intent = new Intent(MainActivity.this, com.example.hotpot0.section1.views.StartupActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    // Helper method to get device ID
    private String getDeviceID() {
        return android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
    }
}
