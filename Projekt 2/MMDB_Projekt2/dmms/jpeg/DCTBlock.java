package dmms.jpeg;

import dmms.jpeg.spec.*;

/**
 * Pattern implementation of interface DCTBlockI.
 * @author Roland Tusch
 * @version 1.0
 */

public class DCTBlock implements DCTBlockI {

    double[][] dctCoeffs;

    /**
     * Creates a new DCT block holding the given DCT coefficient matrix.
     * @param dctCoeffs a matrix containing the DCT coefficients for a
     *        component block. The size of the matrix must be BlockI.N x
     *        BlockI.N.
     */
    public DCTBlock(double[][] dctCoeffs) {
        if (dctCoeffs != null) {
            if (dctCoeffs.length == BlockI.N && dctCoeffs[0].length == BlockI.N)
                this.dctCoeffs = dctCoeffs;
            else
                throw new IllegalArgumentException("Block is not of size BlockI.N x BlockI.N.");
        }
        else
            throw new IllegalArgumentException("The DCT block data must not be null.");
    }

    public double[][] getData() {
        return dctCoeffs;
    }
}