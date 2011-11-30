package dmms.jpeg.spec;

/**
 * A DCT block contains the DCT coefficients of one block of component
 * data. It is different to a normal YUV component block (interface BlockI),
 * since the DCT coefficients are of type double, not of type integer.
 * @author Roland Tusch
 * @version 1.0
 */

public interface DCTBlockI {

    /**
     * Delivers the DCT coefficient matrix contained by this DCT block.
     * @return the DCT coefficients as an array of array of doubles, where the
     * length of each dimension defaults to BlockI.N.
     */
    double[][] getData();
}