package dmms.jpeg;

import dmms.jpeg.spec.*;

/**
 * Pattern implementation of interface BlockI.
 * @author Roland Tusch
 * @version 1.0
 */

public class Block implements BlockI {

    int[][] blockData;

    /**
     * Creates a new block containing the given block data.
     * @param blockData the data contained by this block. The given matrix
     *        must not be null and must be of size N x N.
     */
    public Block(int[][] blockData) {
        if (blockData != null) {
            if (blockData.length == N && blockData[0].length == N)
                this.blockData = blockData;
            else
                throw new IllegalArgumentException("Block is not of size N x N.");
        }
        else
            throw new IllegalArgumentException("The block data must not be null.");
    }

    public int[][] getData() {
        return blockData;
    }
}