package com.justin.ftcnndemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by justin on 7/2/17.
 */

public class CameraInitializer {
    public interface ImageListener{
        public void onImageAvailable(final byte[] data,final Camera camera);
    }
    Camera c;
    Camera.Parameters p;
    SurfaceTexture t;
    ImageListener listener;
    public CameraInitializer(final ImageListener listener){
        this.listener=listener;
        c=Camera.open();
        t=new SurfaceTexture(0);
        try {
            c.setPreviewTexture(t);
        } catch (IOException e) {
            e.printStackTrace();
        }
        c.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                listener.onImageAvailable(data,camera);
            }
        });
        p=c.getParameters();
        p.setPreviewSize(640,480);
        c.setParameters(p);
        c.startPreview();
    }
    public void stop(){
        if(c!=null){
            c.release();
        }
    }
}
