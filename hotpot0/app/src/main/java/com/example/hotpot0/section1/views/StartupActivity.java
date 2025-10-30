package com.example.hotpot0.section1.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.hotpot0.R;
import com.google.android.material.button.MaterialButton;

public class StartupActivity extends AppCompatActivity {

    private MaterialButton createProfileButton;
    private MaterialButton adminLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section1_startup_activity); // <-- ensure this is the XML filename

        // find views (IDs must match your XML)
        createProfileButton = findViewById(R.id.createProfileButton);
        adminLoginButton = findViewById(R.id.adminLoginButton);

        createProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start UserSignUpActivity
                Intent intent = new Intent(StartupActivity.this, UserSignupActivity.class);
                startActivity(intent);
                // optionally finish() if you don't want StartupActivity on the back stack:
                // finish();
            }
        });

        adminLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start AdminLoginActivity
                Intent intent = new Intent(StartupActivity.this, AdminLoginActivity.class);
                startActivity(intent);
            }
        });
    }
}