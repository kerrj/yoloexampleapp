package com.example.justin.tensorflowtest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;

/**
 * Created by justin on 6/13/17.
 */

public class BitmapUtils {
    static Context context;
    public static void setContext(Context c){
        context=c;
    }
    public static float[] cvtBitmap2FloatArray(Bitmap b) {
        int[] imageData = new int[b.getWidth() * b.getWidth()];
        b.getPixels(imageData, 0, b.getWidth(), 0, 0, b.getWidth(), b.getHeight());
        float[] data = new float[b.getWidth() * b.getWidth() * 3];
        for (int i = 0; i < imageData.length; ++i) {
            final int val = imageData[i];
            data[i * 3 + 0] = ((val >> 16) & 0xFF);
            data[i * 3 + 1] = ((val >> 8) & 0xFF);
            data[i * 3 + 2] = (val & 0xFF);
        }
        return data;
    }

    public static Bitmap resizeBitmap(Bitmap input){
        if(input.getWidth()==input.getHeight()){
            return Bitmap.createScaledBitmap(input,416,416,false);
        }
        int width=input.getWidth(),height=input.getHeight();
        Bitmap b=width<height?Bitmap.createScaledBitmap(input,416,416*height/width,false):Bitmap.createScaledBitmap(input,416*width/height,416,false);
        Bitmap output= ThumbnailUtils.extractThumbnail(b,416,416);
        return output;
    }

    public static Bitmap loadRes(String name) {
        int res = context.getResources().getIdentifier(name, "raw", context.getPackageName());
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inScaled = false;
        Bitmap inputImage = BitmapFactory.decodeResource(context.getResources(), res, o);
        return inputImage;
    }

    public static Bitmap rotateBitmap(Bitmap in){
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        return Bitmap.createBitmap(in , 0, 0, in.getWidth(), in.getHeight(), matrix,false);
    }
}
