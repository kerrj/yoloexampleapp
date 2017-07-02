package com.justin.ftcnndemo;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.ContactsContract;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.util.Log;
import android.util.Size;

import java.util.Arrays;

/**
 * Created by justin on 6/13/17.
 */

public class CameraInitializer {

    public interface ImageListener {
        public void onImageAvailable(ImageReader reader);
    }

    ImageReader.OnImageAvailableListener onImageAvailableListener=new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            imageListener.onImageAvailable(reader);
        }
    };
    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private String mCameraId;
    private Handler mHandler;
    private ImageListener imageListener;
    private Context context;
    private ImageReader imageReader;




    public CameraInitializer(ImageListener listener,Context c) {
        this.imageListener=listener;
        context=c;
        imageReader=ImageReader.newInstance(640,480,ImageFormat.YUV_420_888,2);
    }
    public void startCamera(){
        startHandler();
        setupCamera();
    }


    private void startHandler() {
        HandlerThread mHandlerThread = new HandlerThread("CameraThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

    }


    private CameraDevice.StateCallback callback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            startPreview();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int i) {

        }
    };

    public void stopCamera(){
        mCameraDevice.close();
    }

    private void setupCamera() {
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

        try {
            for (String id : mCameraManager.getCameraIdList()) {
                CameraCharacteristics mCameraCharacteristics = mCameraManager.getCameraCharacteristics(id);
                if (mCameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    mCameraId = id;
                }
            }
            CameraCharacteristics mCameraCharacteristics = mCameraManager.getCameraCharacteristics(mCameraId);
            StreamConfigurationMap mStreamConfigurationMap = mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size[] sizes = mStreamConfigurationMap.getOutputSizes(ImageFormat.YUV_420_888);
            for(Size s:sizes){
                Log.d("size",s.toString());
            }
            try {
                mCameraManager.openCamera(mCameraId, callback, mHandler);
            }catch (SecurityException e){
                e.printStackTrace();
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }



    private void startPreview(){
        imageReader.setOnImageAvailableListener(onImageAvailableListener,mHandler);
        try {
            final CaptureRequest.Builder mCaptureRequestBuilder=mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(imageReader.getSurface());
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_OFF);
            mCameraDevice.createCaptureSession(Arrays.asList(imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    try {
//                        mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE,CaptureRequest.FLASH_MODE_TORCH);
                        CaptureRequest mCaptureRequest = mCaptureRequestBuilder.build();
                        cameraCaptureSession.setRepeatingRequest(mCaptureRequest, null, mHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


}