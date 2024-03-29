package net.sf.theora_java.jna.utils;

import java.awt.image.BufferedImage;

import com.sun.jna.Pointer;
import net.sf.theora_java.jna.TheoraLibrary.theora_info;
import net.sf.theora_java.jna.TheoraLibrary.yuv_buffer;


/**
 * Converts a yuv_buffer to a BufferedImage (RGB).
 * Adapted from http://osdir.com/ml/multimedia.ogg.theora.devel/2004-12/txtewtF4xphR2.txt
 *
 * @author Ken Larson
 */
public class YUVConverter {

    public static BufferedImage toBufferedImage(yuv_buffer yuv, theora_info ti) {

        int sizeX = ti.width;
        int sizeY = ti.height;

        Pointer srcY = yuv.y;
        Pointer srcU = yuv.u;
        Pointer srcV = yuv.v;

//		check( YUVBuffer.y_width  == YUVBuffer.uv_width  * 2 );
//		check( YUVBuffer.y_height == YUVBuffer.uv_height * 2 );
//		check( YUVBuffer.y_width  == GetSizeX() );
//		check( YUVBuffer.y_height == GetSizeY() );
        // TODO: apply crop.
//		// we convert based on the full width/height, not the cropped width/height.
//		// for now, let's just fail if there is any cropping.
//		if (ti.frame_height != ti.height)
//			throw new RuntimeException("TODO: TheoraInfo.frame_height != TheoraInfo.height");
//		if (ti.frame_width != ti.width)
//			throw new RuntimeException("TODO: TheoraInfo.frame_width != TheoraInfo.width");
//		if (ti.offset_x != 0)
//			throw new RuntimeException("TODO: TheoraInfo.offset_x != 0");
//		if (ti.offset_y != 0)
//			throw new RuntimeException("TODO: TheoraInfo.offset_y != 0");


        BufferedImage bi = new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_INT_RGB);
        int[] pixels = new int[sizeX * sizeY];

        {
            int pixelIndex = 0;

            for (int y = 0; y < sizeY; y++) {
                for (int x = 0; x < sizeX / 2; x++) {
                    int OffsetY0 = yuv.y_stride * y + x * 2;
                    int OffsetY1 = yuv.y_stride * y + x * 2 + 1;
                    int OffsetUV = yuv.uv_stride * (y / 2) + x;

                    // TODO: convert these to ints?  unsigned problems?
                    int Y0 = srcY.getByte(OffsetY0) & 0xff;
                    int Y1 = srcY.getByte(OffsetY1) & 0xff;
                    int U = srcU.getByte(OffsetUV) & 0xff;
                    int V = srcV.getByte(OffsetUV) & 0xff;

                    {
                        byte r = clamp(appTrunc(Y0 + 1.402f * (V - 128)), 0, 255);
                        byte g = clamp(appTrunc(Y0 - 0.34414f * (U - 128) - 0.71414f * (V - 128)), 0, 255);
                        byte b = clamp(appTrunc(Y0 + 1.772f * (U - 128)), 0, 255);

                        int pixel = 0;
                        pixel += r & 0xff;    // red
                        pixel *= 256;
                        pixel += g & 0xff; // green
                        pixel *= 256;
                        pixel += b & 0xff;    // blue
                        pixels[pixelIndex++] = pixel;
                    }

                    {
                        byte r = clamp(appTrunc(Y1 + 1.402f * (V - 128)), 0, 255);
                        byte g = clamp(appTrunc(Y1 - 0.34414f * (U - 128) - 0.71414f * (V - 128)), 0, 255);
                        byte b = clamp(appTrunc(Y1 + 1.772f * (U - 128)), 0, 255);

                        int pixel = 0;
                        pixel += r & 0xff;    // red
                        pixel *= 256;
                        pixel += g & 0xff; // green
                        pixel *= 256;
                        pixel += b & 0xff;    // blue
                        pixels[pixelIndex++] = pixel;
                    }
                }
            }
        }

        bi.setRGB(0, 0, sizeX, sizeY, pixels, 0, sizeX);
        return bi;
    }

    private static int appTrunc(float value) {
        return (int) value;
    }

    private static byte clamp(int x, int min, int max) {
        if (x <= min)
            return (byte) min;
        else if (x >= max)
            return (byte) max;
        else
            return (byte) x;
    }
}
