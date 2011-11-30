package dmms.jpeg.spec;

/**
 * The SubSampler interface allows a 4:2:2 or 4:2:0 downsampling of chrominance
 * components. During JPEG compression, subsampling is an optional task. Thus, if
 * the chrominance components are not downsampled, the sampling ratio is 4:4:4 and
 * all components are of same width and height.
 * @author Roland Tusch
 * @version 1.0
 */

public interface SubSamplerI {

    /** constant for 4:4:4 subsampling of chrominance **/
    static final int YUV_444 = 0;
    /** constant for 4:2:2 subsampling of chrominance **/
    static final int YUV_422 = 1;
    /** constant for 4:2:0 subsampling of chrominance **/
    static final int YUV_420 = 2;

    /**
     * Performs downsampling of the chrominance components of the given YUV image.
     * In digital video and image processing, only the ratio's 4:2:2 and 4:2:0
     * denote meaningful sampling ratios (beside 4:4:4 of course), as components
     * are furthermore devided into blocks and minimum coded units for further
     * processing (see BlockGeneratorI).
     * @param yuvImg the YUV image to be downsampled
     * @param samplingRatio the ratio to use for downsampling chrominance information.
     *        Must be one of YUV_222 or YUV_420. YUV_444 is also possible but useless,
     *        since it only defines a ratio, where none of the components is downsampled.
     * @return a new YUV image, whose components Cb and Cr represent downsampled
     * components from those of the input image
     */
    YUVImageI downSample(YUVImageI yuvImg, int samplingRatio);
}
