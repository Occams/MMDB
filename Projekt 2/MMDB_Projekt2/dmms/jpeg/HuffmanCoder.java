package dmms.jpeg;

import dmms.jpeg.spec.*;
import java.util.*;

/**
 * Pattern implementation of interface EntropyCoderI.
 * @author Roland Tusch
 * @version 1.0
 */

public class HuffmanCoder extends AbstractHuffmanCoder {

    /***** Inner class RunLevel ********************************/
    public class RunLevel implements RunLevelI {

        int run, level;

        /**
         * Creates a new RUN-LEVEL object.
         * @param run the run of zeros, which must not be negative.
         * @param level the level following the run
         */
        public RunLevel(int run, int level) {
            if (run >= 0) {
                this.run = run;
                this.level = level;
            }
            else
                throw new IllegalArgumentException("The RUN must not be negative.");
        }

        public int getRun() {
            return run;
        }

        public int getLevel() {
            return level;
        }
    }
    /***********************************************************/

    public RunLevelI[] runLengthEncode(BlockI quantBlock) {
        int[] block = linearizeBlock(quantBlock);
        Vector runLevelVec = new Vector();
        int run = 0;
        for (int i=1;i < block.length;i++) {
            if (block[ZIGZAG_ORDER[i]] == 0)
                run++;
            else {
                runLevelVec.addElement(new RunLevel(run,block[ZIGZAG_ORDER[i]]));
                run = 0;
            }
        }
        if (run > 0)
            // there was a run of zeros not being terminated by a level
            runLevelVec.addElement(new RunLevel(run,0));
        RunLevelI[] runLevels = new RunLevelI[runLevelVec.size()];
        runLevelVec.toArray(runLevels);
        return runLevels;
    }

    /**
     * Transforms the given block of quantized data into a linear
     * array containing the block data row by row.
     * @param quantBlock the block to linearize
     * @return a linearized array containing the block data as a
     *         sequence of rows
     */
    protected int[] linearizeBlock(BlockI quantBlock) {
        int[] linearBlockData = new int[quantBlock.N * quantBlock.N];
        int[][] blockData = quantBlock.getData();
        int index = 0;
        for (int i=0;i < blockData.length;i++) {
            for (int j=0;j < blockData[i].length;j++) {
                linearBlockData[index] = blockData[i][j];
                index++;
            }
        }
        return linearBlockData;
    }
}