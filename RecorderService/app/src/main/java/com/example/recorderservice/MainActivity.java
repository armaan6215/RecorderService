package com.example.recorderservice;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PermissionInfo;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.icu.text.AlphabeticIndex;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.StatFs;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.recorderservice.Dropbox.DropboxActivity;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainActivity extends DropboxActivity {

    public static Camera camera;
    private boolean openedFrontCamera = false;
    FrameLayout frameLayout;
    private static final String TAG = "Recorder";
    ShowCamera showCamera;
    private String[] permissions;
    private SeekBar seekBar;
    public static Chronometer chronometer;
    private TextView maxDuration;
    private EditText maxDurationEdit;
    Button editTextbutton, recordBtn;
    ImageView switchCamera;
    public static ImageView ivRecord;
    String item1;
    public static CamcorderProfile profile;
    Menu menu;
    public final static String EXTRA_PATH = "FilesActivity_Path";
    public static String mPath;
    public static Context context;
    public static int MAX_DURATION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        defineViews();
        setViewActions();

        checkPermission("BACK");

        String path = getIntent().getStringExtra(EXTRA_PATH);
        mPath = path == null ? "" : path;
        context = this;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem dropbox = menu.findItem(R.id.dropbox);
        MenuItem quality = menu.findItem(R.id.quality);
        MenuItem itemQualityHigh = menu.findItem(R.id.qualityHigh);
        if(!hasToken()) {
            dropbox.setTitle("Dropbox Login");
            invalidateOptionsMenu();
            RecorderService.hasToken = false;
        } else {
            dropbox.setTitle("Dropbox Logout");
            invalidateOptionsMenu();
            RecorderService.hasToken = true;
        }

        if(RecorderService.isRecording) {
            quality.setVisible(false);
            invalidateOptionsMenu();
        }

        if(openedFrontCamera){
            itemQualityHigh.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MenuItem itemQuality = menu.findItem(R.id.quality);
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.duration) {
            showView(maxDuration);
            showView(maxDurationEdit);
            showView(editTextbutton);
            showView(seekBar);
        }

        else if(id == R.id.quality) {

        }

        else if(id == R.id.listItem) {
            Intent listIntent = new Intent(this, ListViewActivity.class);
            startActivity(listIntent);
        }



        else if(id == R.id.qualityLow) {
            profile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
        }

        else if(id == R.id.qualityMed) {
            profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
        }

        else if(id == R.id.qualityHigh) {
            profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        }

        else if(id == R.id.dropbox) {
            if(!hasToken()) {
                loginDialog().show();
            } else {
                logoutDialog().show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void defineViews() {
        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
        recordBtn = (Button) findViewById(R.id.btnRecord);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        maxDuration = (TextView) findViewById(R.id.maxDuration);
        maxDurationEdit = (EditText) findViewById(R.id.maxDurationEdit);
        editTextbutton = (Button) findViewById(R.id.editTextButton);
        switchCamera = (ImageView) findViewById(R.id.switchCamera);
        ivRecord = (ImageView) findViewById(R.id.ivRecord);
    }

    public void setViewActions() {
        editTextbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(maxDurationEdit.getText().toString().equals("")) {
                    MAX_DURATION = 0;
                    Toast.makeText(getApplicationContext(), "Max Duration set to Unlimited ",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    MAX_DURATION = Integer.parseInt(maxDurationEdit.getText().toString());
                    Toast.makeText(getApplicationContext(), "Max Duration set to " + MAX_DURATION
                            + " minutes",Toast.LENGTH_SHORT).show();
                }
                hideView(editTextbutton);
                hideView(maxDurationEdit);
                hideView(maxDuration);
                hideView(seekBar);
                seekBar.setProgress(MAX_DURATION);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                MAX_DURATION = progress;
                if (MAX_DURATION == 0) {
                    maxDuration.setText("Unlimited");
                    maxDurationEdit.setHint("Unlimited");
                } else {
                    maxDuration.setText("Set Recording Duration: " + MAX_DURATION + "-min");
                    maxDurationEdit.setHint("Set Recording Duration:   " + MAX_DURATION + "-min");
                    maxDurationEdit.setText(""+MAX_DURATION);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ivRecord.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                Intent serviceIntent = new Intent(getApplicationContext(), RecorderService.class);
                startService(serviceIntent);

            }
        });


        switchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (RecorderService.isRecording) {
                    Toast.makeText(getApplicationContext(), "Cannot switch camera while recording", Toast.LENGTH_SHORT).show();
                } else {
                    if (openedFrontCamera) {
                    checkPermission("BACK");
                    openedFrontCamera = false;
                } else {
                    checkPermission("FRONT");
                    openedFrontCamera = true;
                }
            }
        }
        });
    }

    public void checkPermission(String item) {
        item1 = item;
        permissions = new String[] {
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        Permissions.check(this, permissions, null, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                selectCamera(item1);
                showCamera = new ShowCamera(getApplicationContext(), camera);
                frameLayout.removeAllViews();
                frameLayout.addView(showCamera);
                //showView(checkBox);
            }
        });
    }
    public void selectCamera(String cam) {
        if (camera == null) {
            cameraSet(cam);
        } else {
            try {
                camera.release();
                camera = null;
                cameraSet(cam);
            } catch (Exception e) {
                Toast.makeText(this, "camera is being used by other application", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    public void cameraSet(String cam) {
        if (cam.equals("FRONT")) {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        } else {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
    }

    public static void showView(View v) {
        v.setVisibility(View.VISIBLE);
    }

    public static void hideView(View v) {
        v.setVisibility(View.INVISIBLE);
    }

    public AlertDialog loginDialog() {
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(MainActivity.this)
                // set message, title, and icon
                .setTitle("Login?")
                .setMessage("Login to Dropbox?")
                .setPositiveButton("Login", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        DropboxActivity.startOAuth2Authentication(MainActivity.this, getString(R.string.app_key), null);
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Logged in", Toast.LENGTH_SHORT).show();
                    }

                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        return myQuittingDialogBox;
    }

    private AlertDialog logoutDialog() {
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(this)
                // set message, title, and icon
                .setTitle("Logout?")
                .setMessage("Are you sure?")
                .setPositiveButton("Logout", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        prefs.edit().clear().commit();
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Logged out", Toast.LENGTH_SHORT).show();
                    }

                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        return myQuittingDialogBox;
    }
}


