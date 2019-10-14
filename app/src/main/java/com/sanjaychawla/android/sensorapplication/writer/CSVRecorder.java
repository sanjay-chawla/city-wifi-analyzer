package com.sanjaychawla.android.sensorapplication.writer;

import android.os.Environment;
import android.util.Log;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CSVRecorder {

    public static void write(String path, double latitude, double longitude, String networkType, double connectionSpeed) {
        String fileName = "AnalysisData.csv";
        String filePath = path + File.separator + fileName;
        Log.d("CSVWriter", "path: " + filePath);
        if (checkRWAccess()) {
            File f = new File(filePath);
            CSVWriter writer;
            // File exist
            try {
                if (f.exists() && !f.isDirectory()) {
                    writer = new CSVWriter(new FileWriter(filePath, true));
                } else {
                    writer = new CSVWriter(new FileWriter(filePath));
                    String[] header = {"Timestamp", "Latitude", "Longitude", "NetworkType", "NetworkSpeed"};
                    writer.writeNext(header);
                }
                String[] data = {getCurrentTime(), Double.toString(latitude), Double.toString(longitude), networkType, Double.toString(connectionSpeed)};
                writer.writeNext(data);
                writer.close();
            }catch (IOException ioException){
                Log.e("CSVRecorder", "IOException in write(): " + ioException.getMessage());
            }
        }
    }

    private static String getCurrentTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
    }

    private static boolean checkRWAccess() {
        /* Checks if external storage is available for read and write */
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}
