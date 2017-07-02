package com.justin.ftcnndemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    final String inputName = "input";
    final String outputName = "output"; //original tiny-yolo-voc
    Classifier c;
    StatTimer timer;

    // Used to load the "native-lib" library on application startup.
    static {
        System.loadLibrary("imageutils_jni");
        System.loadLibrary("rgb2yuv");
        System.loadLibrary("yuv2rgb");
    }

    TextView v;
    ImageView i;
    Camera2Initializer camera2;
    CameraInitializer camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Create","1");
        setContentView(R.layout.activity_main);
        v = (TextView) findViewById(R.id.sample_text);
        i = (ImageView) findViewById(R.id.imageview);
        c = TensorFlowYoloDetector.create(getAssets(), "file:///android_asset/optimized-robot-redblueball-2.pb", 416, inputName, outputName, 32);
        BitmapUtils.setContext(this);
        timer=new StatTimer();
        textPaint=new Paint();
        textPaint.setColor(Color.YELLOW);
        textPaint.setTextSize(30);
        textPaint.setAntiAlias(true);
        rectPaint=new Paint();
        rectPaint.setColor(Color.GREEN);
        rectPaint.setStrokeWidth(3);
        rectPaint.setStyle(Paint.Style.STROKE);

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if(permissionCheck!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
            return;
        }
        initializeCamera();
    }

    public void initializeCamera(){
        if(Build.VERSION.SDK_INT>21){
            camera2 =new Camera2Initializer(camera2Listener,this);
            camera2.startCamera();
        }else {
            camera = new CameraInitializer(cameraListener);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
            initializeCamera();
        }
    }

    Bitmap drawBitmap,b,b2,screenMap;
    Canvas canvas;
    Paint textPaint,rectPaint;
    double recognize,draw;
    public void processBitmap(Bitmap input){
        timer.tic();
        b=BitmapUtils.resizeBitmap(input);
        b2=BitmapUtils.rotateBitmap(b);
        List<Classifier.Recognition> r = c.recognizeImage(b2);
        recognize=timer.toc("Recognize Image");
        timer.tic();
        drawBitmap=b2.copy(Bitmap.Config.ARGB_8888,true);
        canvas=new Canvas(drawBitmap);
        for (Classifier.Recognition recog : r) {
            Log.d("Recognition", recog.toString());
            float scalar=1.0f;
            canvas.drawRect(recog.getLocation().left*scalar,recog.getLocation().top*scalar,
                    recog.getLocation().right*scalar,recog.getLocation().bottom*scalar,rectPaint);
            String conf=Float.toString(recog.getConfidence());
            canvas.drawText(recog.getTitle(),recog.getLocation().left*scalar,recog.getLocation().top*scalar,textPaint);
            canvas.drawText(conf.substring(2,4),recog.getLocation().left*scalar,(recog.getLocation().top+25)*scalar,textPaint);
        }
        timer.toc("Draw");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                screenMap=Bitmap.createScaledBitmap(drawBitmap,1080,1080,true);
                i.setImageBitmap(screenMap);
                v.setText("Recognition time (ms): "+Double.toString(recognize));
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(camera2 !=null){
            camera2.stopCamera();
        }
        if(camera!=null){
            camera.stop();
        }
    }
    ExecutorService executorService= Executors.newFixedThreadPool(2);

    boolean computing=false;
    int[] rgbBytes=new int[640*480];
    byte[][]yuvBytes = new byte[3][];
    Bitmap frame= Bitmap.createBitmap(640,480, Bitmap.Config.ARGB_8888);
    int yRowStride,uvRowStride,uvPixelStride;
    Image.Plane[] planes;
    Camera2Initializer.ImageListener camera2Listener= new Camera2Initializer.ImageListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = null;
            try {
                image = reader.acquireNextImage();
                if (computing) {
                    image.close();
                    return;
                }
                final Image image2=image;
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        computing = true;
                        timer.tic();
                        planes = image2.getPlanes();
                        ImageUtils.fillBytes(planes, yuvBytes);
                        yRowStride = planes[0].getRowStride();
                        uvRowStride = planes[1].getRowStride();
                        uvPixelStride = planes[1].getPixelStride();
                        ImageUtils.convertYUV420ToARGB8888(yuvBytes[0], yuvBytes[1], yuvBytes[2], 640, 480, yRowStride, uvRowStride, uvPixelStride, rgbBytes);
                        frame.setPixels(rgbBytes,0,640,0,0,640,480);
                        timer.toc("Camera2 YUV-->RGB");
                        processBitmap(frame);
                        computing=false;
                        image2.close();
                    }
                });
            } catch (final Exception e) {
                if (image != null) {
                    image.close();
                }
                e.printStackTrace();
                return;
            }
        }
    };

    YuvImage yuv;
    Bitmap yuvBitmap;
    CameraInitializer.ImageListener cameraListener=new CameraInitializer.ImageListener() {
        @Override
        public void onImageAvailable(final byte[] data,final Camera camera) {
            if(!computing) {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        computing = true;
                        Camera.Parameters p = camera.getParameters();
                        timer.tic();
                        int width = p.getPreviewSize().width;
                        int height = p.getPreviewSize().height;
                        yuv = new YuvImage(data, p.getPreviewFormat(), width, height, null);

                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        yuv.compressToJpeg(new Rect(0, 0, width, height), 100, out);

                        byte[] bytes = out.toByteArray();
                        yuvBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        timer.toc("Camera YUV->RGB");
                        processBitmap(yuvBitmap);
                        computing=false;
                    }
                });
            }
        }
    };
}
