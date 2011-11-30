package dmms.jpeg;

import dmms.jpeg.spec.*;

/**
 * Pattern implementation of interface RegionI.
 * @author Roland Tusch
 * @version 1.0
 */

public class Region implements RegionI {

    BlockI[] blocks;
    int compType;

    /**
     * Constructs a new region holding the given blocks for the specified
     * component type.
     * @param blocks the blocks covered by this region
     * @param compType the component type, from which this region is part of.
     *        Must be one of YUVImageI.Y, YUVImageI.Cb or YUVImageI.Cr
     */
    public Region(BlockI[] blocks, int compType) {
        if (compType >= YUVImageI.Y && compType <= YUVImageI.Cr) {
            this.blocks = blocks;
            this.compType = compType;
        }
        else
            throw new IllegalArgumentException("Invalid component type.");
    }

    public int getType() {
        return compType;
    }

    public BlockI[] getBlocks() {
        return blocks;
    }
}