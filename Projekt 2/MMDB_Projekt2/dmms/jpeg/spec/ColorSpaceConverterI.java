package dmms.jpeg.spec;

import java.awt.*;

/**
 * An interface specifying the functionality for converting an image from
 * one color space to another. Actually, only RGB to YUV conversion is
 * supported.
 * @author Roland Tusch
 * @version 1.0
 */

public interface ColorSpaceConverterI {

    /**
     * Converts the given image in RGB color space to an equivalent image
     * in the YUV color space. The YUV image is represented by its components
     * Y, Cb and Cr.
     * @param rgbImg the RGB image to be converted
     * @return the equivalent image in YUV color space, or null, if there
     *         was a problem in reading the pixel values of the given image
     */
    YUVImageI convertRGBToYUV(Image rgbImg);
}
