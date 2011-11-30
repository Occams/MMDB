package dmms.jpeg;

import dmms.jpeg.spec.*;
import java.awt.*;
import java.awt.image.*;

/**
 * Pattern implementation of interface ColorSpaceConverterI.
 * @author Roland Tusch
 * @version 1.0
 */

public class ColorSpaceConverter implements ColorSpaceConverterI {

    int imgWidth = 0;
    int imgHeight = 0;
    int[] imgData = null;

    public YUVImageI convertRGBToYUV(Image rgbImg) {
        try {
            imgWidth = rgbImg.getWidth(null);
            imgHeight = rgbImg.getHeight(null);
            imgData = new int[imgWidth * imgHeight];
            PixelGrabber grabber = new PixelGrabber(rgbImg,0,0,imgWidth,imgHeight,imgData,0,imgWidth);
            if (grabber.grabPixels())
                return transformRGBtoYUV();
        }
        catch (Exception e) {}
        return null;
    }

    protected YUVImageI transformRGBtoYUV() {
        int[][] Y = new int[imgHeight][imgWidth];
        int[][] Cb = new int[imgHeight][imgWidth];
        int[][] Cr = new int[imgHeight][imgWidth];

        for (int i=0;i < imgData.length;i++) {
            // extract RGB values from current pixel
            // the default color model is TYPE_INT_ARGB
            int r = (imgData[i] >> 16) & 0xff;
            int g = (imgData[i] >> 8) & 0xff;
            int b = imgData[i] & 0xff;
            // convert to Y, Cb and Cr

            Y[i/imgWidth][i%imgWidth] = (int) (0.299f*r + 0.587f*g + 0.114f*b);
            Cb[i/imgWidth][i%imgWidth] = 128 + (int) (-0.1687f*r - 0.3313f*g + 0.5f*b);
            Cr[i/imgWidth][i%imgWidth] = 128 + (int) (0.5f*r - 0.4187f*g - 0.0813f*b);
        }
        return new YUVImage(new Component(Y,YUVImageI.Y),
                            new Component(Cb,YUVImageI.Cb),
                            new Component(Cr,YUVImageI.Cr),
                            SubSamplerI.YUV_444);
    }
}
