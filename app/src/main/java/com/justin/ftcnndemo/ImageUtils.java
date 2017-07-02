package com.justin.ftcnndemo;

import android.media.Image;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created by justin on 6/13/17.
 */

public class ImageUtils {
    static final int kMaxChannelValue = 262143;
    public static void convertYUV420ToARGB8888(
            byte[] yData,
            byte[] uData,
            byte[] vData,
            int width,
            int height,
            int yRowStride,
            int uvRowStride,
            int uvPixelStride,
            int[] out) {
        convertYUV420ToARGB8888(yData, uData, vData, out, width, height, yRowStride, uvRowStride, uvPixelStride, false);

//        int i = 0;
//        for (int y = 0; y < height; y++) {
//            int pY = yRowStride * y;
//            int uv_row_start = uvRowStride * (y >> 1);
//            int pUV = uv_row_start;
//            int pV = uv_row_start;
//
//            for (int x = 0; x < width; x++) {
//                int uv_offset = pUV + (x >> 1) * uvPixelStride;
//                out[i++] =
//                        YUV2RGB(
//                                convertByteToInt(yData, pY + x),
//                                convertByteToInt(uData, uv_offset),
//                                convertByteToInt(vData, uv_offset));
//            }
//        }
    }
    private static int convertByteToInt(byte[] arr, int pos) {
        return arr[pos] & 0xFF;
    }

    public static void fillBytes(final Image.Plane[] planes, final byte[][] yuvBytes) {
        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        for (int i = 0; i < planes.length; ++i) {
            final ByteBuffer buffer = planes[i].getBuffer();
            if (yuvBytes[i] == null) {
                yuvBytes[i] = new byte[buffer.capacity()];
            }
            buffer.get(yuvBytes[i]);
        }
    }
    private static int YUV2RGB(int nY, int nU, int nV) {
        nY -= 16;
        nU -= 128;
        nV -= 128;
        if (nY < 0) nY = 0;

        // This is the floating point equivalent. We do the conversion in integer
        // because some Android devices do not have floating point in hardware.
        // nR = (int)(1.164 * nY + 2.018 * nU);
        // nG = (int)(1.164 * nY - 0.813 * nV - 0.391 * nU);
        // nB = (int)(1.164 * nY + 1.596 * nV);

        final int foo = 1192 * nY;
        int nR = foo + 1634 * nV;
        int nG = foo - 833 * nV - 400 * nU;
        int nB = foo + 2066 * nU;

        nR = Math.min(kMaxChannelValue, Math.max(0, nR));
        nG = Math.min(kMaxChannelValue, Math.max(0, nG));
        nB = Math.min(kMaxChannelValue, Math.max(0, nB));

        return 0xff000000 | ((nR << 6) & 0x00ff0000) | ((nG >> 2) & 0x0000FF00) | ((nB >> 10) & 0xff);
    }
    public static native void convertYUV420SPToARGB8888(
            byte[] input, int[] output, int width, int height, boolean halfSize);

    /**
     * Converts YUV420 semi-planar data to ARGB 8888 data using the supplied width
     * and height. The input and output must already be allocated and non-null.
     * For efficiency, no error checking is performed.
     *
     * @param y
     * @param u
     * @param v
     * @param uvPixelStride
     * @param width The width of the input image.
     * @param height The height of the input image.
     * @param halfSize If true, downsample to 50% in each dimension, otherwise not.
     * @param output A pre-allocated array for the ARGB 8:8:8:8 output data.
     */
    public static native void convertYUV420ToARGB8888(
            byte[] y,
            byte[] u,
            byte[] v,
            int[] output,
            int width,
            int height,
            int yRowStride,
            int uvRowStride,
            int uvPixelStride,
            boolean halfSize);

    /**
     * Converts YUV420 semi-planar data to RGB 565 data using the supplied width
     * and height. The input and output must already be allocated and non-null.
     * For efficiency, no error checking is performed.
     *
     * @param input The array of YUV 4:2:0 input data.
     * @param output A pre-allocated array for the RGB 5:6:5 output data.
     * @param width The width of the input image.
     * @param height The height of the input image.
     */
    public static native void convertYUV420SPToRGB565(
            byte[] input, byte[] output, int width, int height);

    /**
     * Converts 32-bit ARGB8888 image data to YUV420SP data.  This is useful, for
     * instance, in creating data to feed the classes that rely on raw camera
     * preview frames.
     *
     * @param input An array of input pixels in ARGB8888 format.
     * @param output A pre-allocated array for the YUV420SP output data.
     * @param width The width of the input image.
     * @param height The height of the input image.
     */
    public static native void convertARGB8888ToYUV420SP(
            int[] input, byte[] output, int width, int height);

    /**
     * Converts 16-bit RGB565 image data to YUV420SP data.  This is useful, for
     * instance, in creating data to feed the classes that rely on raw camera
     * preview frames.
     *
     * @param input An array of input pixels in RGB565 format.
     * @param output A pre-allocated array for the YUV420SP output data.
     * @param width The width of the input image.
     * @param height The height of the input image.
     */
    public static native void convertRGB565ToYUV420SP(
            byte[] input, byte[] output, int width, int height);



}
