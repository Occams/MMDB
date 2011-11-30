package dmms.jpeg.spec;

/**
 * This interface specifies, how minimum coded units (MCU's) have to be extracted
 * from a YUV image. A minimum coded unit contains a number of blocks from each component,
 * depending on the subsampling ratios used for chrominance components. E. g., if the used
 * subsampling ratio is 4:2:2, a MCU has to contain 4 luminance, 2 chrominance blue
 * and 2 chrominance red blocks. If the ratio equals 4:2:0, 4 luminance, 1 chrominance
 * blue and 1 chrominance red block constitute a MCU. A ratio of 4:4:4 has to be
 * specially treated, since a MCU in JPEG terminology must not contain more than 10
 * blocks. Therefore 4 Y, 4 Cb and 4 Cr blocks are not allowed. Instead, a MCU fullfilling
 * a 4:4:4 sampling ratio contains 1 Y, 1 Cb and 1 Cr block. In other words, a subsampling
 * ratio of 4:4:4 has to be translated to horizontal and vertical sampling factor
 * vectors of [1,1,1] each. In order to treat blocks from one component as an own data
 * unit, these blocks are aggregated to a region. As a result, a minimum coded unit
 * contains one region for each component. <p>
 * For the reason that subsampling ratios like 4:2:2 or 4:2:0 only are possible, if
 * the scaled image dimensions modulo 16 are 0 in both horizontal and vertical direction,
 * a block generation only should be performed, if these conditions are valid
 * (= macroblock-based conditions).
 * @author Roland Tusch
 * @version 1.0
 */

public interface BlockGeneratorI {

    /**
     * Extracts the minimum coded units from the components of the given YUV image.
     * Depending on the subsampling ratio of the components, a MCU's region might
     * contain one, two or four blocks of component data.
     * The order of the blocks is the result of left-to-right scanning within the
     * components.
     * @param yuvImg the YUV image from which to extract the MCU's
     * @return an array of MCU's, representing all the component data of the image
     *         in an interleaved way, or null, if the image has illegal dimensions
     *         (width or height modulo 16 is not equal to zero).
     */
    MinimumCodedUnitI[] generateMinimumCodedUnits(YUVImageI yuvImg);
}
