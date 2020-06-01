package com.example.recorderservice;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.dropbox.core.v2.files.FileMetadata;
import com.example.recorderservice.Dropbox.DropboxActivity;
import com.example.recorderservice.Dropbox.DropboxClientFactory;
import com.example.recorderservice.Dropbox.UploadFileTask;

import java.io.File;
import java.io.IOException;

import static android.app.Application.getProcessName;
import static android.content.ContentValues.TAG;
import static com.example.recorderservice.App.CHANNEL_ID;

public class RecorderService extends Service {

    MediaRecorder mediaRecorder;
    Camera camera;
    File outputFile;
    public static boolean isRecording = false;
    CamcorderProfile profile;
    Activity activity;
    Context context;
    public static boolean hasToken = false;

    public RecorderService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        camera = MainActivity.camera;
        profile = MainActivity.profile;
        startCapture();
        createNotification();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pedingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Background Recorder")
                .setContentText("Recording...")
                .setContentIntent(pedingIntent)
                .build();
        startForeground(1, notification);
    }

    public boolean prepareVideoRecorder() {
        mediaRecorder = new MediaRecorder();
        camera.unlock();
        mediaRecorder.setCamera(ShowCamera.camera);
        mediaRecorder.setOrientationHint(90);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setMaxDuration(MainActivity.MAX_DURATION*60000);
        if(this.profile != null) {
            mediaRecorder.setProfile(this.profile);
        } else {
            mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));
        }
        outputFile = CameraHelper.getOutputMediaFile(this);

        if (outputFile == null) {
            return false;
        }
        mediaRecorder.setOutputFile(outputFile.getPath());

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    public void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            camera.lock();
        }
    }

    public void startCapture() {
        if(isRecording){
            ifRecording();
        } else {
            MediaPrepareTask mediaPrepareTask = new MediaPrepareTask();
            mediaPrepareTask.execute(null, null, null);
        }
    }

    public void releaseCamera() {
        if(camera != null) {
            camera.release();
            camera = null;
        }
    }

    class MediaPrepareTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {


            // initialize video camera
            if (prepareVideoRecorder()) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
                mediaRecorder.start();

                isRecording = true;
            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder();
                releaseCamera();
                chronoMeterStop();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {

            }
            // inform the user that recording has started
            MainActivity.ivRecord.setImageResource(R.drawable.video_stop);
            MainActivity.chronometer.setBase(SystemClock.elapsedRealtime());
            MainActivity.showView(MainActivity.chronometer);
            MainActivity.chronometer.start();
        }
    }

    public void chronoMeterStop() {
        MainActivity.chronometer.stop();
        MainActivity.hideView(MainActivity.chronometer);
    }

    public void ifRecording() {
        try{
            mediaRecorder.stop();
        } catch (RuntimeException e) {
            Log.d(TAG, "RuntimeException: stop() is called immediately after start()");
            outputFile.delete();
        }
        releaseMediaRecorder();


        if(hasToken) {
            String path = outputFile.getPath();
            Uri uri = Uri.fromFile(new File(path));
            uploadFile(uri.toString());
        }

        chronoMeterStop();
        camera.lock();
        MainActivity.ivRecord.setImageResource(R.drawable.video_record);
        isRecording = false;
        stopService(new Intent(getApplicationContext(), RecorderService.class));
    }

    private void uploadFile(String fileUri) {
        final ProgressDialog dialog = new ProgressDialog(MainActivity.context);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Uploading");
        dialog.show();

        new UploadFileTask(this, DropboxClientFactory.getClient(), new UploadFileTask.Callback() {
            @Override
            public void onUploadComplete(FileMetadata result) {
                dialog.dismiss();
                //loadData();
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();

                Log.e(TAG, "Failed to upload file.", e);
                Toast.makeText(activity,
                        "An error has occurred",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }).execute(fileUri, MainActivity.mPath);
    }
}
