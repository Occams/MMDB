package dmms.jpeg.spec;

/**
 * A YUV image contains luminance (Y), chrominance blue (Cb) and chrominance
 * red (Cr) components. That is why a YUV image is also referred to as YCbCr
 * image in digital image processing.
 * @author Roland Tusch
 * @version 1.0
 */

public interface YUVImageI {

    /** constant for luminance **/
    static final int Y = 0;
    /** constant for chrominance blue **/
    static final int Cb = 1;
    /** constant for chrominance red **/
    static final int Cr = 2;

    /**
     * Delivers the component of the specified component type of this
     * YUV image.
     * @param compType the type of component to return. Must be one of
     *        Y, Cb or Cr.
     * @return the component of specified type
     * @exception IllegalArgumentException if the given type is not valid
     */
    ComponentI getComponent(int compType);

    /**
     * Retrieves the currently used sampling ratio for this YUV (or YCbCr) image.
     * You can compare it with SubSamplerI.YUV_444, in order to figure out,
     * whether the image has been subsampled, or not.
     * @return the sampling factor of this component. Must be one of SubSamplerI.YUV_444,
     *         SubSamplerI.YUV_422 or SubSamplerI.YUV_420.
     */
    int getSamplingRatio();
}
