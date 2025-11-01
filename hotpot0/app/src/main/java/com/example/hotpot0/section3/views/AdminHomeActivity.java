package com.example.hotpot0.section3.views;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AdminHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve adminID from the Intent
        int adminID = getIntent().getIntExtra("adminID", -1);

        // Simple TextView to indicate AdminHomeActivity is opened
        TextView tv = new TextView(this);
        tv.setTextSize(24);
        tv.setText("Welcome Admin!\nAdmin ID: " + adminID);
        setContentView(tv);
    }
}