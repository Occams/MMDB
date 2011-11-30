package dmms.jpeg.spec;

/**
 * A region contains the blocks of one component in a minimum coded unit.
 * @author Roland Tusch
 * @version 1.0
 */

public interface RegionI {

    /**
     * Determines the type of component, from which this region is part of.
     * @return the component type of this region. Must be one of YUVImageI.Y,
     *             YUVImageI.Cb, or YUVImageI.Cr.
     */
    int getType();

    /**
     * Delivers the blocks of component data contained by this region.
     * @return the blocks of component data contained by this region
     */
    BlockI[] getBlocks();
}
