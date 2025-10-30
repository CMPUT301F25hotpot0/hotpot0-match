package com.example.hotpot0.section2.views;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Simple TextView to indicate HomeActivity is opened
        TextView tv = new TextView(this);
        tv.setText("Welcome to HomeActivity!");
        tv.setTextSize(24);
        setContentView(tv);
    }
}
