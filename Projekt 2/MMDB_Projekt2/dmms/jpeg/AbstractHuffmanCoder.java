package dmms.jpeg;

import dmms.jpeg.spec.*;
import java.io.*;

/**
 * Partial implementation of interface EntropyCoderI.
 * @author Roland Tusch
 * @version 1.0
 */

public abstract class AbstractHuffmanCoder implements EntropyCoderI {

    Object[] dcTables, acTables;
    /** last DC coefficients of component Y, Cb and Cr **/
    int[] lastDCValues = {0,0,0};
    int codeBuffer, bitBuffer;

    /**
     * Creates a new abstract Huffman encoder object.
     */
    public AbstractHuffmanCoder() {
        codeBuffer = bitBuffer = 0;
        initHuffmanTables();
        // showHuffmanTables();
    }

    /**
     * Initializes the Huffman tables for DC and AC values of luminance and
     * chrominance components.
     */
    protected void initHuffmanTables() {
        dcTables = new Object[2];
        acTables = new Object[2];
        // Huffman tables (codes and sizes) for DC coefficients
        dcTables[0] = createTable(true,BITS_DC_LUMINANCE,VALS_DC_LUMINANCE);
        // showMatrix("\nHuffman table for DC luminance:",(int[][]) DC_matrix[0]);
        dcTables[1] = createTable(true,BITS_DC_CHROMINANCE,VALS_DC_CHROMINANCE);
        // showMatrix("\nHuffman table for DC chrominance:",(int[][]) DC_matrix[1]);
        // Huffman tables (codes and sizes) for AC coefficients
        acTables[0] = createTable(false,BITS_AC_LUMINANCE,VALS_AC_LUMINANCE);
        // showMatrix("\nHuffman table for AC luminance:",(int[][]) AC_matrix[0]);
        acTables[1] = createTable(false,BITS_AC_CHROMINANCE,VALS_AC_CHROMINANCE);
        // showMatrix("\nHuffman table for AC chrominance:",(int[][]) AC_matrix[1]);
    }

    /**
     * Creates a Huffman table for the given type of coefficents, using
     * the given arrays of Huffman bits and values.
     * @param dc specifies, whether the table should contain Huffman codes for DC
     *        coefficients, or not (AC)
     * @param bits the bits used to calculate the Huffman codes and sizes. Must be one of
     *        BITS_DC_LUMINANCE, BITS_DC_CHROMINANCE, BITS_AC_LUMINANCE or BITS_AC_CHROMINANCE.
     * @param vals an array of index values used for storing the calculated
     *        Huffman sizes and codes. Must be one of VALS_DC_LUMINANCE, VALS_DC_CHROMINANCE,
     *        VALS_AC_LUMINANCE or VALS_AC_CHROMINANCE. The given bits and vals must always
     *        represent a pair of the same type, e. g. (BITS_DC_LUMINANCE, VALS_DC_LUMINANCE).
     * @return a table of Huffman codes for DC or AC coefficients concerning luminance
     *         or chrominance (depending on the values given for bits and vals). The first
     *         column of the matrix contains the Huffman code, the second one the number
     *         of bits required for storing the code.
     */
    protected int[][] createTable(boolean dc, int[] bits, int[] vals) {
        int[][] huffTable;
        int[] huffsize = new int[257];
        int[] huffcode= new int[257];
        int p = 0, lastp, code, si;
        for (int i = 1; i < bits.length; i++) {
            for (int j = 1; j <= bits[i]; j++) {
                huffsize[p++] = i;
            }
        }
        huffsize[p] = 0;
        lastp = p;

        code = 0;
        si = huffsize[0];
        p = 0;
        while(huffsize[p] != 0) {
            while(huffsize[p] == si) {
                huffcode[p++] = code;
                code++;
            }
            code <<= 1;
            si++;
        }

        if (dc)
            // create Huffman table for DC coefficients
            huffTable = new int[12][2];
        else
            // create Huffman table for run-levels of AC coefficients
            huffTable = new int[255][2];

        for (p = 0; p < lastp; p++) {
            huffTable[vals[p]][0] = huffcode[p];
            huffTable[vals[p]][1] = huffsize[p];
        }

        return huffTable;
    }

    protected void showHuffmanTables() {
        System.out.print("\nGenerated Huffman tables:");
        showTable((int[][]) dcTables[0],"Table for DC luminance:");
        showTable((int[][]) dcTables[1],"Table for DC chrominance:");
        showTable((int[][]) acTables[0],"Table for AC luminance:");
        showTable((int[][]) acTables[1],"Table for AC chrominance:");
    }

    protected void showTable(int[][] huffTable,String msg) {
        System.out.print("\n" + msg + "\n");
        for (int i=0;i < huffTable.length;i++) {
            System.out.print(i + "\t");
            for (int j=0;j < huffTable[i].length;j++) {
                System.out.print(huffTable[i][j] + "\t");
            }
            System.out.print("\n");
        }
    }

    /**
     * Huffman encodes the given block of quantization coefficients.
     * The difference of the DC coefficients of this block and its previous
     * block is variable length coded. The AC coefficients are first run length
     * coded into a (RUN,LEVEL)-sequence. The RUN-LEVEL combinations are afterwards
     * Huffman coded using a kind of modified Huffman table for luminance and
     * chrominance respectively. The entropy encoded data is finally written to
     * the the given buffered output stream.
     * @param quantBlock the block containing the quantized DCT coefficients
     * @param compType the component type the quantization block is part of. Must
     *        be one of YUVImageI.Y, YUVImageI.Cb or YUVImageI.Cr.
     * @param bos the output stream to write the entropy encoded block into
     * @exception java.io.IOException if a problem occurred while writing to the
     *            output stream.
     */
    public void huffmanEncode(BlockI quantBlock, int compType, BufferedOutputStream bos) throws IOException  {
        if (compType >= YUVImageI.Y && compType <= YUVImageI.Cr) {
            // encode DC portion
            encodeDifferentialDCValue(quantBlock.getData()[0][0],compType,bos);
            // encode AC portion
            encodeRunLevels(runLengthEncode(quantBlock),compType,bos);
            lastDCValues[compType] = quantBlock.getData()[0][0];
        }
        else
            throw new IllegalArgumentException("Illegal component type.");
    }

    /**
     * Performs a differential pulse code modulation (DPCM) of the given DC value and the one
     * of the last block. Finally, the difference is variable length encoded (VLC) and written
     * to the given output stream.
     * @param dcValue the DC value to encode
     * @param compType the type of component from which the DC value is part of
     * @param bos the buffered output stream to write the encoded bits into
     * @exception java.io.IOException if a problem occurrs while writing to the output stream
     */
    protected void encodeDifferentialDCValue(int dcValue, int compType, BufferedOutputStream bos) throws IOException {
        int dcValueDiff = dcValue - lastDCValues[compType];
        int temp = dcValueDiff;
        if(dcValueDiff < 0) {
            dcValueDiff = -dcValueDiff;
            temp--;
        }
        // calcualte number of bits required for dcValueDiff
        int nbits = 0;
        while (dcValueDiff != 0) {
            nbits++;
            dcValueDiff >>= 1;
        }
        int huffTableIndex = (compType == YUVImageI.Y ? 0:1);
        int[][] currDCTable = (int[][]) dcTables[huffTableIndex];
        // store variable length code
        bufferIt(currDCTable[nbits][0],currDCTable[nbits][1],bos);
        if (nbits != 0)
            // store DC value difference
            bufferIt(temp,nbits,bos);
    }

    /**
     * Performs a Huffman coding of the given sequence of RUN-LEVEL combinations.
     * @param runLevels the sequence of RUN-LEVEL combinations to encode. Only the last (RUN,LEVEL)
     *        combination in the sequence can have a level of zero, if there was a
     *        run of zeros not terminated by a level.
     * @param compType the type of component from which the RUN-LEVELS have been computed
     * @param bos the buffered output stream to write the encoded bits into
     * @exception java.io.IOException if a problem occurrs while writing to the output stream
     * @exception java.lang.IllegalArgumentException if the RUN-LEVEL sequence contains
     *            a RUN-LEVEL with a level of 0, not being the last RUN-LEVEL in the
     *            sequence. An IllegalArgumentException is also thrown, if the runLevels
     *            argument is null.
     */
    protected void encodeRunLevels(RunLevelI[] runLevels, int compType, BufferedOutputStream bos) throws IOException {
        if (runLevels != null) {
            if (isValidRLSequence(runLevels)) {
                int huffTableIndex = (compType == YUVImageI.Y ? 0:1);
                int[][] currACTable = (int[][]) acTables[huffTableIndex];
                for (int i=0;i < runLevels.length;i++) {
                    int run = runLevels[i].getRun();
                    int level = runLevels[i].getLevel();
                    if (i < runLevels.length-1 || level != 0) {
                        // System.out.print("\nCoding run-level: (" + run + "," + level + ")");
                        while (run > 15) {
                            // store Huffman code for a run of 16
                            bufferIt(currACTable[0xF0][0],currACTable[0xF0][1],bos);
                            run -= 16;
                        }
                        int temp = level;
                        if (level < 0) {
                            level = -level;
                            temp--;
                        }
                        int nbits = 0;
                        while (level != 0) {
                            nbits++;
                            level >>= 1;
                        }
                        int tableIndex = (run << 4) + nbits;
                        // store Huffman code for the length of the RUN-LEVEL combination
                        bufferIt(currACTable[tableIndex][0],currACTable[tableIndex][1],bos);
                        // store the level
                        bufferIt(temp,nbits,bos);
                    }
                    else
                        // end-of-block sentinel value with zero-level
                        bufferIt(currACTable[0][0],currACTable[0][1],bos);
                }
            }
            else
                throw new IllegalArgumentException("An illegal RUN-LEVEL with level of 0 found.");
        }
        else
            throw new IllegalArgumentException("The RUN-LEVEL sequence must not be null.");
    }

    /**
     * Checks, whether the given RUN-LEVEL sequence is valid, or not.
     */
    protected boolean isValidRLSequence(RunLevelI[] runLevels) {
        for (int i=0;i < runLevels.length-1;i++) {
            if (runLevels[i].getLevel() == 0)
                return false;
        }
        return true;
    }

    /**
     * The Huffman encoded bits are not immediately stored to the given output
     * stream. Instead, a 32 bit long buffer (in fact an integer variable) is
     * used to buffer the codes. If suffient bits have been buffered, the bits
     * are sent to the stream byte by byte.
     * @param code the Huffman encoded bits to buffer
     * @param size the number of bits needed to store the code
     * @param bos the output stream to write the encoded bits into
     * @exception java.io.IOException if an error occurred while writing to the stream
     */
    protected void bufferIt(int code, int size, BufferedOutputStream bos) throws IOException {
        // System.out.print("\nBuffering Huffman code: " + code + " (size: " + size + ")");
        int codeBuffer = code;
        int bitBuffer = this.bitBuffer;

        codeBuffer &= (1 << size) - 1;
        bitBuffer += size;
        codeBuffer <<= 24 - bitBuffer;
        codeBuffer |= this.codeBuffer;

        while(bitBuffer >= 8) {
            int c = ((codeBuffer >> 16) & 0xFF);
            bos.write(c);
            if (c == 0xFF)
                bos.write(0);
            codeBuffer <<= 8;
            bitBuffer -= 8;
        }
        this.codeBuffer = codeBuffer;
        this.bitBuffer = bitBuffer;
    }

    /**
     * Flushes the content of the buffer to the given stream.
     * @param bos the output stream to flush the content of the buffer
     * @exception java.io.IOException if an error occurred while writing to the stream
     */
    public void flushBuffer(BufferedOutputStream bos) throws IOException {
        while (bitBuffer >= 8) {
            int c = ((codeBuffer >> 16) & 0xFF);
            bos.write(c);
            if (c == 0xFF)
                bos.write(0);
            codeBuffer <<= 8;
            bitBuffer -= 8;
        }
        if (bitBuffer > 0) {
            int c = ((codeBuffer >> 16) & 0xFF);
            bos.write(c);
        }
    }
}