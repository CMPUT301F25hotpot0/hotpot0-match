package com.example.hotpot0.section3.views;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;
import com.example.hotpot0.models.ProfileDB;
import com.example.hotpot0.models.UserProfile;

public class AdminOrganizerViewActivity extends AppCompatActivity {

    private ProfileDB profileDB;
    private int organizerID;

    private TextView organizerName, organizerEmail, organizerPhone, organizerUserID;
    private Button removeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section3_adminorganizer_view_activity);

        profileDB = new ProfileDB();
        organizerID = getIntent().getIntExtra("organizerID", -1);

        organizerName = findViewById(R.id.organizerName);
        organizerEmail = findViewById(R.id.organizerEmail);
        organizerPhone = findViewById(R.id.organizerPhone);
        organizerUserID = findViewById(R.id.organizerUserID);
        removeButton = findViewById(R.id.removeOrganizerButton);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        loadOrganizer();
        removeButton.setOnClickListener(v -> removeOrganizer());
    }

    private void loadOrganizer() {
        profileDB.getUserByID(organizerID, new ProfileDB.GetCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile user) {
                organizerName.setText(user.getName());
                organizerEmail.setText(user.getEmailID());
                organizerPhone.setText(user.getPhoneNumber());
                organizerUserID.setText(String.valueOf(user.getUserID()));
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminOrganizerViewActivity.this,
                        "Failed to load organizer", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void removeOrganizer() {
        profileDB.deleteUser(organizerID, new ProfileDB.ActionCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(AdminOrganizerViewActivity.this,
                        "Organizer removed", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminOrganizerViewActivity.this,
                        "Failed to remove organizer", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

