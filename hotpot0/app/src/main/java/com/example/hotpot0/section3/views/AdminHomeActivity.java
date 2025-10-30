// AdminHomeActivity.java
package com.example.hotpot0.section3.views;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AdminHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Simple TextView to indicate AdminHomeActivity is opened
        TextView tv = new TextView(this);
        tv.setText("Welcome to AdminHomeActivity!");
        tv.setTextSize(24);
        setContentView(tv);
    }
}