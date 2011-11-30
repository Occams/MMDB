package dmms.jpeg;

import dmms.jpeg.spec.*;
import java.awt.*;
import java.awt.image.*;

/**
 * Pattern implementation of interface ImageScalatorI.
 * @author Roland Tusch
 * @version 1.0
 */

public class ImageScalator implements ImageScalatorI {

    public Image scaleImage(Image rgbImg, int samplingRatio) {
        if (samplingRatio >= SubSamplerI.YUV_444 && samplingRatio <= SubSamplerI.YUV_420) {
            int imgWidth = rgbImg.getWidth(null);
            int imgHeight = rgbImg.getHeight(null);
            int newImgWidth = 0, newImgHeight = 0;
            if (samplingRatio == SubSamplerI.YUV_444) {
                // special handling for non-subsampled images
                if (imgWidth % 8 != 0 || imgHeight % 8 != 0) {
                    newImgWidth = ((int) Math.ceil(imgWidth/8.0)) * 8;
                    newImgHeight = ((int) Math.ceil(imgHeight/8.0)) * 8;
                }
                else
                    // no scaling required
                    return rgbImg;
            }
            else { // YUV_422 or YUV_420
                if (imgWidth % 16 != 0 || imgHeight % 16 != 0) {
                    newImgWidth = ((int) Math.ceil(imgWidth/16.0)) * 16;
                    newImgHeight = ((int) Math.ceil(imgHeight/16.0)) * 16;
                }
                else
                    // no scaling required
                    return rgbImg;
            }
            // create new image with new dimensions
            try {
                BufferedImage scaledImg = new BufferedImage(newImgWidth,
                                                            newImgHeight,
                                                            BufferedImage.TYPE_INT_ARGB);
                int[] imgData = new int[imgWidth * imgHeight];
                PixelGrabber grabber = new PixelGrabber(rgbImg,0,0,imgWidth,imgHeight,imgData,0,imgWidth);
                if (grabber.grabPixels()) {
                    scaledImg.setRGB(0,0,imgWidth,imgHeight,imgData,0,imgWidth);
                    // set remaining pixel columns to right-most pixel column of
                    // the source image
                    for (int y=0;y < imgHeight;y++) {
                        int lastColumnValue = imgData[(y+1)*imgWidth-1];
                        for (int x=imgWidth;x < newImgWidth;x++) {
                            scaledImg.setRGB(x,y,lastColumnValue);
                        }
                    }
                    // set remaining pixel rows to bottom-most pixel row of
                    // the source image
                    for (int x=0;x < imgWidth;x++) {
                        int lastRowValue = imgData[(imgHeight-1)*imgWidth+x];
                        for (int y=imgHeight;y < newImgHeight;y++) {
                            scaledImg.setRGB(x,y,lastRowValue);
                        }
                    }
                    // fill the last bottom-right square with
                    // the last pixel value of the source image
                    int widthDiff = newImgWidth - imgWidth;
                    int heightDiff = newImgHeight - imgHeight;
                    for (int i=0;i < heightDiff;i++) {
                        for (int j=0;j < widthDiff;j++) {
                            scaledImg.setRGB(imgWidth+j,imgHeight+i,imgData[imgData.length-1]);
                        }
                    }
                    return scaledImg;
                }
                // pixel grabbing failed
            }
            catch (Exception e) {
                System.out.print("\nException occurred: " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }
        else
            throw new IllegalArgumentException("Invalid sampling ratio.");
    }
}
