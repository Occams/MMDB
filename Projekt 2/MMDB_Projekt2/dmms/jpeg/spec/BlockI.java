package dmms.jpeg.spec;

/**
 * A block is the minimum treated data unit used in lossy image compression.
 * The default size of a block is 8 x 8 pixels.
 * @author Roland Tusch
 * @version 1.0
 */

public interface BlockI {

    /**
     * Constant for the default size of a block dimension. Thus, the default
     * block size is N x N.
     */
    static final int N = 8;

    /**
     * Delivers the component data contained by this block.
     * @return the block data as an array of array of integers, where the length
     *         of each dimension defaults to N.
     */
    int[][] getData();
}
