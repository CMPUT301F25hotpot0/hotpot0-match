package com.example.hotpot0.section2.controllers;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class QRGenerator {

    public QRGenerator() {}

    public interface Callback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }

    // Generate QR bitmap
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