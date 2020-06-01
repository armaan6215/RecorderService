package com.example.recorderservice;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraHelper {


    public static File getOutputMediaFile(Context context) {
        File video = null;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "VID_" +timeStamp;
        File directory = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        try {
            video = File.createTempFile(fileName, ".mp4", directory);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return video;
    }
}
