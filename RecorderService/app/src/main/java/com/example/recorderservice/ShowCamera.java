package com.example.recorderservice;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

public class ShowCamera extends SurfaceView implements SurfaceHolder.Callback {
    public static Camera camera;
    SurfaceHolder holder;
    public static Parameters params;

    public ShowCamera(Context context, Camera camera) {
        super(context);
        this.camera = camera;
        holder = getHolder();
        holder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        params = camera.getParameters();

        if(this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            params.set("orientation","portrait");
            camera.setDisplayOrientation(90);
            params.setRotation(90);
        } else {
            params.set("orientation", "landscape");
            camera.setDisplayOrientation(0);
            params.setRotation(0);
        }
        setResolution(params);
        camera.setParameters(params);
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }


    public void setResolution(Parameters params) {
        List<Camera.Size> sizes = params.getSupportedPictureSizes();
        Camera.Size mSize = null;
        for(Camera.Size size : sizes) {
            mSize = size;
        }
        params.setPictureSize(mSize.width, mSize.height);
    }

}
