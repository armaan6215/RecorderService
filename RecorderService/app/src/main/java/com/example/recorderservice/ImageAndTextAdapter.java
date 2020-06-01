package com.example.recorderservice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class ImageAndTextAdapter extends ArrayAdapter<String> {
    private LayoutInflater mInflater;
    private String[] mStrings;
    Bitmap thumb;
    private int mViewResourceId;
    private String filePath;
    File file;

    public ImageAndTextAdapter(Context ctx, int viewResourceId, String[] strings, String path) {
        super(ctx, viewResourceId, strings);
        mInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mStrings = strings;
        mViewResourceId = viewResourceId;
        filePath = path;
    }

    @Override
    public int getCount() {
        return mStrings.length;
    }

    @Override
    public String getItem(int position) {
        return mStrings[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = mInflater.inflate(mViewResourceId, null);

        String filepath = filePath + "/" + mStrings[position];

        TextView tv = (TextView) convertView.findViewById(R.id.textView);
        TextView tvSize = (TextView) convertView.findViewById(R.id.videoSize);
        TextView tvLength = (TextView) convertView.findViewById(R.id.videoLength);

        tv.setText(mStrings[position]);

        ImageView iv= (ImageView) convertView.findViewById(R.id.imageView);
        thumb = ThumbnailUtils.createVideoThumbnail(filepath, MediaStore.Video.Thumbnails.MINI_KIND);
        iv.setImageBitmap(thumb);

        file = new File(filepath);
        long length = file.length();
        length = length/1024;
        tvSize.setText("Size: "+length +"KB");

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(filepath);
        long duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        @SuppressLint("DefaultLocale") String durationDisplay = String.format("%d:%d", TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration)-
                        TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
        tvLength.setText(durationDisplay);
        retriever.release();

        return convertView;
    }




}

