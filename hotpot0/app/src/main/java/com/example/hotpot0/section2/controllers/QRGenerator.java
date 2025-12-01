package com.example.hotpot0.section2.controllers;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Generates QR codes for given data.
 */
public class QRGenerator {

    public QRGenerator() {}

    public interface Callback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }

    /**
     * Generates a QR code bitmap for the provided data string.
     *
     * @param data The string data to encode in the QR code.
     * @return A Bitmap representing the QR code, or null if generation fails.
     */
    public Bitmap generateQR(String data) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512);

            Bitmap bmp = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);

            for (int x = 0; x < 512; x++) {
                for (int y = 0; y < 512; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            return bmp;

        } catch (Exception e) {
            Log.e("QRGenerator", "Failed to generate QR", e);
            return null;
        }
    }
}