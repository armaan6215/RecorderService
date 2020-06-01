package com.example.recorderservice;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class AddNotesActivity extends AppCompatActivity {
    public String FILE_PATH, FILE_NAME;
    EditText editText;
    TextView textView;
    Button notesButton, loadButton, playButton;
    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_notes);
        editText = (EditText) findViewById(R.id.editNotes);
        notesButton = (Button) findViewById(R.id.notesButton);
        loadButton = (Button) findViewById(R.id.loadButton);
        playButton = (Button) findViewById(R.id.playButton);

        videoView = (VideoView) findViewById(R.id.video_view);

        FILE_PATH = getIntent().getStringExtra("filePath");
        FILE_NAME = getIntent().getStringExtra("fileName");
        editText.setText(FILE_NAME);

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.setVisibility(View.INVISIBLE);
                editText.setVisibility(View.VISIBLE);
                notesButton.setVisibility(View.VISIBLE);
                loadButton.setVisibility(View.VISIBLE);
                playButton.setVisibility(View.VISIBLE);
            }
        });
        notesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });

        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                load();
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
            }
        });
    }

    public void save() {
        String text = editText.getText().toString();
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
            fos.write(text.getBytes());
            editText.getText().clear();
            Toast.makeText(getApplicationContext(), "Saved to" + getFilesDir() + "/" + FILE_NAME, Toast.LENGTH_LONG);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void load() {
        FileInputStream fis = null;
        try {
            fis = openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text = br.readLine();
            if (text != null) {
                sb.append(text).append("\n");
                editText.setText(sb.toString());
            } else {
                Toast.makeText(this, "Notes not added", Toast.LENGTH_SHORT).show();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void play() {
        videoView.setVisibility(View.VISIBLE);
        editText.setVisibility(View.INVISIBLE);
        notesButton.setVisibility(View.INVISIBLE);
        loadButton.setVisibility(View.INVISIBLE);
        playButton.setVisibility(View.INVISIBLE);
        String filePath = FILE_PATH + "/" + FILE_NAME;
        Uri uri = Uri.parse(filePath);
        videoView.setVideoURI(uri);
        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);
        videoView.start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
