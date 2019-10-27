package com.sanjaychawla.android.sensorapplication.writer;

import android.os.Environment;
import android.util.Log;

import com.opencsv.CSVWriter;
import com.sanjaychawla.android.sensorapplication.data.Record;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CSVRecorder {

    public static void write(String path, Record record) {
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
                String[] data = {
                        record.getTimestamp(),
                        Double.toString(record.getLatitude()),
                        Double.toString(record.getLongitude()),
                        record.getNetworkType(),
                        Double.toString(record.getNetworkSpeed())
                };
                writer.writeNext(data);
                writer.close();
            }catch (IOException ioException){
                Log.e("CSVRecorder", "IOException in write(): " + ioException.getMessage());
            }
        }
    }

    private static boolean checkRWAccess() {
        /* Checks if external storage is available for read and write */
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}
