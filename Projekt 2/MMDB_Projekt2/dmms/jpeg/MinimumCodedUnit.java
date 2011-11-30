package dmms.jpeg;

import dmms.jpeg.spec.*;

/**
 * Pattern implementation of interface MinimumCodedUnitI.
 * @author Roland Tusch
 * @version 1.0
 */

public class MinimumCodedUnit implements MinimumCodedUnitI {

    RegionI[] regions;

    /**
     * Creates a new minimum coded unit holding the given array of regions.
     * @param regions an array containing one region for luminance (Y),
     *        one for chrominance blue (Cb), and one for chrominance red (Cr),
     *        excactly in this ordering.
     */
    public MinimumCodedUnit(RegionI[] regions) {
        if (regions != null && regions.length == 3) {
            if (regions[0].getType() == YUVImageI.Y &&
                regions[1].getType() == YUVImageI.Cb &&
                regions[2].getType() == YUVImageI.Cr)
                this.regions = regions;
            else
                throw new IllegalArgumentException("Wrong order of component regions.");
        }
        else
            throw new IllegalArgumentException("Argument 'regions' must not be null and of length 3.");
    }

    public RegionI[] getRegions() {
        return regions;
    }

    public int getNumberOfBlocks() {
        int numBlocks = 0;
        for (int i=0;i < regions.length;i++) {
            numBlocks += regions[i].getBlocks().length;
        }
        return numBlocks;
    }
}