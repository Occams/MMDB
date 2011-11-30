package dmms.jpeg.spec;

/**
 * A minimum coded unit (MCU) represents the smallest group of data units that is
 * coded. In non-interleaved image coding the MCU represents one data unit (= block).
 * In interleaved image coding, which is the default coding for images where the
 * number of components is > 1, the MCU is the sequence of blocks defined by
 * the horizontal and vertical sampling factors of the component. Blocks of the
 * same component are grouped together to regions.
 * @author Roland Tusch
 * @version 1.0
 */

public interface MinimumCodedUnitI {

    /**
     * Delivers the three regions contained by this MCU.
     * There must always be returned exactly one region for each component
     * (Y, Cb, and Cr), exactly in this ordering.
     * @return the three regions covered by this minumum coded unit
     */
    RegionI[] getRegions();

    /**
     * Delivers the total number of blocks contained by this MCU
     * @return the total number of blocks contained by this MCU's regions
     */
    int getNumberOfBlocks();
}
