package com.example.recorderservice;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class ListViewActivity extends ListActivity {
    EditText editTextRename;
    File directory, delFile, file;
    VideoView videoView;
    String[] files;
    String path, fileName, child;
    FrameLayout frameLayout;
    int i = 0;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
        context = getApplicationContext();
        doStuff();

        videoView = (VideoView) findViewById(R.id.video_view);

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.setVisibility(View.INVISIBLE);
                getListView().setVisibility(View.VISIBLE);
            }
        });
    }

    public void doStuff() {
        directory = this.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        files = directory.list();
        path = directory.getPath();

        setListAdapter(new ImageAndTextAdapter(context, R.layout.lv_layout, files, path));

        registerForContextMenu(getListView());

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getListView().setVisibility(View.GONE);
                frameLayout.setVisibility(View.VISIBLE);
                fileName = files[position];
                videoPlay(fileName);
                Toast.makeText(getApplicationContext(), "Playing " + fileName, Toast.LENGTH_LONG).show();
            }
        });

    }

    public void videoPlay(String fileName) {
        String filePath = path + "/" + fileName;
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_menu, menu);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =  (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int filePosition = info.position;
        String fileName = files[filePosition];
        Path filePath;
        delFile = this.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        filePath = Paths.get(directory.getPath() + "/" +fileName);
        delFile = filePath.toFile();
        switch (item.getItemId()) {
            case R.id.addNotes:
                Toast.makeText(this, "Clicked" + fileName, Toast.LENGTH_SHORT).show();
                Intent intentAddNote = new Intent(ListViewActivity.this, AddNotesActivity.class);
                intentAddNote.putExtra("fileName", fileName);
                intentAddNote.putExtra("filePath", path);
                startActivity(intentAddNote);
                break;

            case R.id.deleteFile:
                AlertDialog diaBox = deleteDialog(delFile);
                diaBox.show();
                break;

            case R.id.renameFile:
                AlertDialog renameBox = renameDialog(fileName);
                renameBox.show();
                break;

            case R.id.shareFile:
                shareFile(delFile);
                break;
        }
        return super.onContextItemSelected(item);
    }


    public void shareFile(File file) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType(URLConnection.guessContentTypeFromName(file.getName()));
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+file.getPath()));
        Intent sendIntent = Intent.createChooser(shareIntent, "Share via");
        startActivity(sendIntent);
    }

    private AlertDialog deleteDialog(File delFile) {
        file = delFile;
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(this)
                // set message, title, and icon
                .setTitle("Delete?")
                .setMessage("Are you sure?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        file.delete();
                        Toast.makeText(getApplicationContext(), "deleted", Toast.LENGTH_SHORT).show();
                        doStuff();
                        dialog.dismiss();
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

    private AlertDialog renameDialog(String currentFileName) {
        fileName = currentFileName;
        editTextRename = new EditText(this);
        i++;
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(this)
                // set message, title, and icon
                .setTitle("Rename")
                .setView(editTextRename)
                .setPositiveButton("Rename", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        File from = new File(directory, fileName);
                        File to = new File(directory, (editTextRename.getText().toString()).trim() + ".mp4");
                        if (to.exists()) {
                            Toast.makeText(getApplicationContext(), "File name already exists", Toast.LENGTH_SHORT).show();
                        } else {
                            from.renameTo(to);
                            doStuff();
                            Toast.makeText(getApplicationContext(), "File renamed", Toast.LENGTH_SHORT).show();
                        }
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

