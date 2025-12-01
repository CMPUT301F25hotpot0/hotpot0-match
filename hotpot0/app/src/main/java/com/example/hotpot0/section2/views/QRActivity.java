package com.example.hotpot0.section2.views;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;
import com.example.hotpot0.models.Event;
import com.example.hotpot0.models.EventDB;
import com.example.hotpot0.section2.controllers.EventActivityController;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

/**
 * Activity for scanning QR codes to find events.
 */
public class QRActivity extends AppCompatActivity {

    private CompoundBarcodeView barcodeView;
    private EventDB eventDB;
    private EventActivityController eventActivityController;

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

//    private void handleQrValue(String qrValue) {
//
//        Toast.makeText(this, "Scanning: " + qrValue, Toast.LENGTH_SHORT).show();
//
//        eventDB.getEventByQrValue(qrValue, new EventDB.GetCallback<Event>() {
//            @Override
//            public void onSuccess(Event event) {
//
//                if (event == null) {
//                    // Not found
//                    Toast.makeText(QRActivity.this,
//                            "No event found for this QR",
//                            Toast.LENGTH_LONG
//                    ).show();
//                    barcodeView.resume();
//                    return;
//                }
//
//                // Found!
//                Toast.makeText(QRActivity.this,
//                        "Event found: " + event.getName(),
//                        Toast.LENGTH_LONG
//                ).show();
//
//                // TODO: navigate to event details
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//                Toast.makeText(QRActivity.this,
//                        "Error: " + e.getMessage(),
//                        Toast.LENGTH_LONG
//                ).show();
//                barcodeView.resume();
//            }
//        });
//    }
    /**
     * Handles the scanned QR code value.
     *
     * @param qrValue The scanned QR code value.
     */
    private void handleQrValue(String qrValue) {

        // Validate QR code format: event:<number>
        if (qrValue == null || !qrValue.matches("^event:\\d+$")) {
            Toast invalidToast = Toast.makeText(this,
                    "Invalid QR code",
                    Toast.LENGTH_SHORT);
            invalidToast.show();
            new android.os.Handler().postDelayed(invalidToast::cancel, 1000);

            barcodeView.resume(); // resume scanning
            return;
        }

        eventDB.getEventByQrValue(qrValue, new EventDB.GetCallback<Event>() {
            @Override
            public void onSuccess(Event event) {

                if (event == null) {
                    // Not found
//                    Toast toast = Toast.makeText(QRActivity.this,
//                            "Event found: " + event.getName(),
//                            Toast.LENGTH_SHORT);
//                    toast.show();
//                    // Dismiss manually after 1 second
//                    new android.os.Handler().postDelayed(toast::cancel, 1000);

                    barcodeView.resume();
                    return;
                }

                if (event.getIsEventActive() == null || !event.getIsEventActive()) {
                    // Event exists but is not active
                    Toast toast = Toast.makeText(QRActivity.this,
                            "Event is not active",
                            Toast.LENGTH_LONG
                    );
                    toast.show();
                    new android.os.Handler().postDelayed(toast::cancel, 1000);
                    barcodeView.resume();
                    return;
                }

                // Event is active - navigate based on user's status
                int userID = getSharedPreferences("app_prefs", MODE_PRIVATE)
                        .getInt("userID", -1);

                if (userID == -1) {
                    Toast.makeText(QRActivity.this,
                            "User not logged in",
                            Toast.LENGTH_LONG
                    ).show();
                    barcodeView.resume();
                    return;
                }

                // Navigate using EventActivityController
                new EventActivityController(QRActivity.this)
                        .navigateToEventActivity(event.getEventID(), userID);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(QRActivity.this,
                        "Error: " + e.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
                barcodeView.resume();
            }
        });
    }
}