package com.sanjaychawla.android.sensorapplication.helper;

import android.location.Location;
import android.util.Log;

import com.sanjaychawla.android.sensorapplication.data.Record;
import com.sanjaychawla.android.sensorapplication.writer.CSVRecorder;
import com.sanjaychawla.android.sensorapplication.writer.FirebaseDBRecorder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class InternetSpeedHelper {
    public static final String TAG = InternetSpeedHelper.class.getSimpleName();
    // bandwidth in kbps
    static private int POOR_BANDWIDTH = 150;
    private int AVERAGE_BANDWIDTH = 550;
    private int GOOD_BANDWIDTH = 2000;

    static OkHttpClient client = new OkHttpClient();
    static Request request = new Request.Builder()
            .url("https://b.zmtcdn.com/data/user_profile_pictures/3be/8d1eff0f14f4c5e2d4de16f08151e3be.jpg?fit=around%7C400%3A400&crop=400%3A400%3B%2A%2C%2A")
            .build();

    public static double calculateSpeed(Record record, String filepath){
        Log.d(TAG, "inside speed calculator");
        CustomImageCallback cb = new CustomImageCallback(record, filepath);
        client.newCall(request).enqueue(cb);
        return cb.speed;
    }

    private static class CustomImageCallback implements Callback {
        long endTime;
        long fileSize;
        double speed;
        long startTime;
        String filepath;
        Record record;

        public CustomImageCallback(Record record, String filepath) {
            startTime = System.currentTimeMillis();
            this.record = record;
            this.filepath = filepath;
        }

        @Override
        public void onFailure(Call call, IOException e) {
            Log.e(TAG, "Error in HTTP call: " + e.getMessage());
            record.setNetworkSpeed(speed);
            record.setTimestamp(getCurrentTime());
            CSVRecorder.write(filepath, record);
            FirebaseDBRecorder.write(record);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            Headers responseHeaders = response.headers();
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
            double timeTakenMills = Math.floor(endTime - startTime);  // time taken in milliseconds
            speed = fileSize / timeTakenMills;
            record.setNetworkSpeed(speed);
            record.setTimestamp(getCurrentTime());
            CSVRecorder.write(filepath, record);
            FirebaseDBRecorder.write(record);
        }

        private static String getCurrentTime() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        }
    }
}
