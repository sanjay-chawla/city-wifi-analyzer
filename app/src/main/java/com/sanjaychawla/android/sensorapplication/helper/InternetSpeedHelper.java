package com.sanjaychawla.android.sensorapplication.helper;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class InternetSpeedHelper {
    public static final String TAG = InternetSpeedHelper.class.getSimpleName();
    // bandwidth in kbps
    private int POOR_BANDWIDTH = 150;
    private int AVERAGE_BANDWIDTH = 550;
    private int GOOD_BANDWIDTH = 2000;

    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder()
            .url("https://b.zmtcdn.com/data/user_profile_pictures/3be/8d1eff0f14f4c5e2d4de16f08151e3be.jpg?fit=around%7C400%3A400&crop=400%3A400%3B%2A%2C%2A")
            .build();
    Call imageCall = client.newCall(request);

    public double calculateSpeed(){
        Log.d(TAG, "inside speed calculator");
        CustomImageCallback cb = new CustomImageCallback();
        imageCall.enqueue(cb);
        Log.d(TAG, "complete flag: " + cb.complete);
        while(!cb.complete){
            Log.d(TAG, "waiting");
        }
        Log.d(TAG, "Download Speed bahar: " + cb.speed + " kbps");
        return cb.speed;
    }

    private class CustomImageCallback implements Callback {
        long endTime;
        long fileSize;
        double speed;
        long startTime;
        boolean complete;

        public CustomImageCallback() {
            complete = false;
            startTime = System.currentTimeMillis();
        }

        @Override
        public void onFailure(Call call, IOException e) {
            e.printStackTrace();
            complete = true;
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            Headers responseHeaders = response.headers();
            for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                Log.d(TAG, responseHeaders.name(i) + ": " + responseHeaders.value(i));
            }

            InputStream input = response.body().byteStream();

            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];

                while (input.read(buffer) != -1) {
                    bos.write(buffer);
                }
                byte[] docBuffer = bos.toByteArray();
                fileSize = bos.size();

            } finally {
                input.close();
            }

            endTime = System.currentTimeMillis();


            // calculate how long it took by subtracting endtime from starttime

            double timeTakenMills = Math.floor(endTime - startTime);  // time taken in milliseconds
            double timeTakenSecs = timeTakenMills / 1000;  // divide by 1000 to get time in seconds
            final int kilobytePerSec = (int) Math.round(1024 / timeTakenSecs);

            if(kilobytePerSec <= POOR_BANDWIDTH){
                // slow connection
            }

            // get the download speed by dividing the file size by time taken to download
            speed = fileSize / timeTakenMills;
            Log.d(TAG, "start time: " + startTime);
            Log.d(TAG, "Download Speed: " + speed + " kbps and complete flag: " + complete);
            complete = true;
        }
    }
}
