package com.example.hotpot0.section2.views;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;
import com.example.hotpot0.models.Event;
import com.example.hotpot0.models.EventDB;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

public class QRActivity extends AppCompatActivity {

    private CompoundBarcodeView barcodeView;
    private EventDB eventDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section2_qrcodesearch_activity);

        eventDB = new EventDB();

        findViewById(R.id.button_BackQrCode).setOnClickListener(v -> finish());

        barcodeView = findViewById(R.id.barcodeScannerView);

        barcodeView.decodeContinuous(result -> {
            if (result.getText() != null) {
                barcodeView.pause();
                handleQrValue(result.getText());
            }
        });
    }

    // Your handleQrValue() method here

    @Override
    protected void onResume() {
        super.onResume();
        if (barcodeView != null) barcodeView.resume();
    }

    @Override
    protected void onPause() {
        if (barcodeView != null) barcodeView.pause();
        super.onPause();
    }

    private void handleQrValue(String qrValue) {

        Toast.makeText(this, "Scanning: " + qrValue, Toast.LENGTH_SHORT).show();

        eventDB.getEventByQrValue(qrValue, new EventDB.GetCallback<Event>() {
            @Override
            public void onSuccess(Event event) {

                if (event == null) {
                    // Not found
                    Toast.makeText(QRActivity.this,
                            "No event found for this QR",
                            Toast.LENGTH_LONG
                    ).show();
                    barcodeView.resume();
                    return;
                }

                // Found!
                Toast.makeText(QRActivity.this,
                        "Event found: " + event.getName(),
                        Toast.LENGTH_LONG
                ).show();

                // TODO: navigate to event details
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(QRActivity.this,
                        "Error: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
                barcodeView.resume();
            }
        });
    }
}