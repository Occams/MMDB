package dmms.jpeg.spec;

import java.awt.*;

/**
 * Scales a given RGB image to a size, where its width and height are
 * a multiple of a block size of 8 or macroblock size of 16, depending on the
 * given sampling ratio. In other words, an image scalator resizes the given
 * image to a size suitable for downsampling and block generation.
 *
 * If e.g. the input image has a dimension of (76,38) in width and height,
 * it has to be scaled to a dimension of (80,40) in case of 4:4:4 subsamping,
 * or (80,48) in case of 4:2:2 or 4:2:0 sampling ratios.
 * @author Roland Tusch
 * @version 1.0
 */

public interface ImageScalatorI {

    /**
     * Scales the given RGB image to a size, where both its width and height
     * represent a multiple of a block size of 8 or macroblock size of
     * 16 pixels, depending on the given sampling ratio. If the sampling ratio
     * equals SubSamplerI.YUV_444, the width and height of the scaled image
     * must be a multiple of 8. If 4:2:2 or 4:2:0 subsampling in specified,
     * the dimensions of the scaled image must be a multiple of 16. The distinction
     * between 4:4:4 and the other sampling ratios has to be done, since JPEG
     * does not allow a minimum coded unit to cover more than 10 blocks. Thus,
     * 4 luminance, 4 chrominance blue and 4 chrominance red blocks are not allowed.
     * Instead, for 4:4:4 sampling the MCUs contain 1 Y, 1 Cb and 1 Cr block each
     * with a block size of 8x8 samples. <p>
     * The additional pixel columns and pixel rows should be replicas of the
     * right-most column and bottom-most row of the original image, respectively.
     * Note: this method does not the same as method <i>getScaledInstance()</i>
     * of class <i>java.awt.Image</i>!
     * @param rgbImg the image in RGB color space to scale
     * @param samplingRatio the sampling ratio effecting the dimensions of the
     *        scaled image. Must be one of SubSamplerI.444, SubSamplerI.422, or
     *        SubSamplerI.420.
     * @return a new RGB image containing all the data of the input image, but
     *         scaled to a size suitable for downsampling and generating
     *         minimum coded units.
     */
    Image scaleImage(Image rgbImg, int samplingRatio);
}
