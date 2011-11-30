package dmms.jpeg.spec;

/**
 * Performs discrete cosine transform operations on a block of data.
 * @author Roland Tusch
 * @version 1.0
 */

public interface DCTI {

    /**
     * Performs a forward DCT (also known as FDCT) on the given block of data.
     * @param the component block to transform into the frequency domain
     * @return a new data block containing the corresponding DCT coefficients
     *         for the given block. The returned block is not of type BlockI,
     *         sind the DCT coefficients are values of type double, not int.
     */
    DCTBlockI forward(BlockI b);
}
