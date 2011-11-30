package dmms.jpeg.spec;

import java.io.*;

/**
 * Specifies the requirements of an entropy encoder for the JPEG baseline process.
 * For the baseline process only the Huffman encoding is defined as entropy encoding
 * technique. Blocks of quantized DCT coefficients need to be variable length coded
 * (VLC) in case of DC coefficients, and Huffman encoded in case of RUN-LEVELS of
 * AC coefficients. The bits and index values specified to use for generating the Huffman
 * tables result in other Huffman tables, than those standardised by the JPEG standard!
 * Therefore the information on bits and value indexes for luminance and chrominance
 * have to be coded into the JPEG file header.
 * @author Roland Tusch
 * @version 1.0
 */

public interface EntropyCoderI {

    /**
     * The zigzag-order of block indexes to use for run length encoding.
     * A block hereby must be a linear vector of the original two-dimensional
     * matrix, containing the matrix data as a sequence of rows.
     */
    static final int[] ZIGZAG_ORDER = { 0,  1,  8,  16,  9,  2,  3, 10,
                                        17, 24, 32, 25, 18, 11,  4,  5,
                                        12, 19, 26, 33, 40, 48, 41, 34,
                                        27, 20, 13,  6,  7, 14, 21, 28,
                                        35, 42, 49, 56, 57, 50, 43, 36,
                                        29, 22, 15, 23, 30, 37, 44, 51,
                                        58, 59, 52, 45, 38, 31, 39, 46,
                                        53, 60, 61, 54, 47, 55, 62, 63};


    /**
     * Specifies the number of Huffman codes for DC luminance of length <i>i</i>
     * for each of the 16 possible length allowed by the specification. The value
     * at position 0 defines the Huffman table class (upper 4 bits) and the Huffman
     * table destination identifier (lower 4 bits).
     **/
    static final int[] BITS_DC_LUMINANCE = {0x00, 0, 1, 5, 1, 1,1,1,1,1,0,0,0,0,0,0,0};
    /** The number of Huffman codes for chrominance DC coefficients. **/
    static final int[] BITS_DC_CHROMINANCE = {0x01,0,3,1,1,1,1,1,1,1,1,1,0,0,0,0,0};
    /** The number of Huffman codes for luminance RUN-LEVEL codes. **/
    static final int[] BITS_AC_LUMINANCE = {0x10,0,2,1,3,3,2,4,3,5,5,4,4,0,0,1,0x7d};
    /** The number of Huffman codes for chrominance RUN-LEVEL codes. **/
    static final int[] BITS_AC_CHROMINANCE = {0x11,0,2,1,2,4,4,3,4,7,5,4,4,0,1,2,0x77};


    /**
     * Index values to use for calculating the position of a luminance DC-code
     * within the Huffman table (or VLC table) for DC luminance.
     **/
    static final int[] VALS_DC_LUMINANCE = {0,1,2,3,4,5,6,7,8,9,10,11};
    /**
     * Index values for calculating the position of a chrominance DC-code
     * within the Huffman table (or VLC table) for DC chrominance.
     **/
    static final int[] VALS_DC_CHROMINANCE = {0,1,2,3,4,5,6,7,8,9,10,11};
    /**
     * Index values for calculating the position of a Huffman code for RUN-LEVELS
     * of luminance AC coefficients.
     **/
    static final int[] VALS_AC_LUMINANCE = {
          0x01, 0x02, 0x03, 0x00, 0x04, 0x11, 0x05, 0x12,
          0x21, 0x31, 0x41, 0x06, 0x13, 0x51, 0x61, 0x07,
          0x22, 0x71, 0x14, 0x32, 0x81, 0x91, 0xa1, 0x08,
          0x23, 0x42, 0xb1, 0xc1, 0x15, 0x52, 0xd1, 0xf0,
          0x24, 0x33, 0x62, 0x72, 0x82, 0x09, 0x0a, 0x16,
          0x17, 0x18, 0x19, 0x1a, 0x25, 0x26, 0x27, 0x28,
          0x29, 0x2a, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39,
          0x3a, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49,
          0x4a, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59,
          0x5a, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69,
          0x6a, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79,
          0x7a, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89,
          0x8a, 0x92, 0x93, 0x94, 0x95, 0x96, 0x97, 0x98,
          0x99, 0x9a, 0xa2, 0xa3, 0xa4, 0xa5, 0xa6, 0xa7,
          0xa8, 0xa9, 0xaa, 0xb2, 0xb3, 0xb4, 0xb5, 0xb6,
          0xb7, 0xb8, 0xb9, 0xba, 0xc2, 0xc3, 0xc4, 0xc5,
          0xc6, 0xc7, 0xc8, 0xc9, 0xca, 0xd2, 0xd3, 0xd4,
          0xd5, 0xd6, 0xd7, 0xd8, 0xd9, 0xda, 0xe1, 0xe2,
          0xe3, 0xe4, 0xe5, 0xe6, 0xe7, 0xe8, 0xe9, 0xea,
          0xf1, 0xf2, 0xf3, 0xf4, 0xf5, 0xf6, 0xf7, 0xf8,
          0xf9, 0xfa};

    /**
     * Index values for calculating the position of a Huffman code for RUN-LEVELS
     * of chrominance AC coefficients.
     **/
    static final int[] VALS_AC_CHROMINANCE = {
          0x00, 0x01, 0x02, 0x03, 0x11, 0x04, 0x05, 0x21,
          0x31, 0x06, 0x12, 0x41, 0x51, 0x07, 0x61, 0x71,
          0x13, 0x22, 0x32, 0x81, 0x08, 0x14, 0x42, 0x91,
          0xa1, 0xb1, 0xc1, 0x09, 0x23, 0x33, 0x52, 0xf0,
          0x15, 0x62, 0x72, 0xd1, 0x0a, 0x16, 0x24, 0x34,
          0xe1, 0x25, 0xf1, 0x17, 0x18, 0x19, 0x1a, 0x26,
          0x27, 0x28, 0x29, 0x2a, 0x35, 0x36, 0x37, 0x38,
          0x39, 0x3a, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48,
          0x49, 0x4a, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58,
          0x59, 0x5a, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68,
          0x69, 0x6a, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78,
          0x79, 0x7a, 0x82, 0x83, 0x84, 0x85, 0x86, 0x87,
          0x88, 0x89, 0x8a, 0x92, 0x93, 0x94, 0x95, 0x96,
          0x97, 0x98, 0x99, 0x9a, 0xa2, 0xa3, 0xa4, 0xa5,
          0xa6, 0xa7, 0xa8, 0xa9, 0xaa, 0xb2, 0xb3, 0xb4,
          0xb5, 0xb6, 0xb7, 0xb8, 0xb9, 0xba, 0xc2, 0xc3,
          0xc4, 0xc5, 0xc6, 0xc7, 0xc8, 0xc9, 0xca, 0xd2,
          0xd3, 0xd4, 0xd5, 0xd6, 0xd7, 0xd8, 0xd9, 0xda,
          0xe2, 0xe3, 0xe4, 0xe5, 0xe6, 0xe7, 0xe8, 0xe9,
          0xea, 0xf2, 0xf3, 0xf4, 0xf5, 0xf6, 0xf7, 0xf8,
          0xf9, 0xfa};

    /**
     * Run length encodes the given block of quantized DCT coefficients. A linear
     * representation of the given block is thereby walked through in ZIGZAG_ORDER.
     * @param quantBlock a block containing quantization coefficients
     * @return a sequence of (RUN,LEVEL) combinations for the quantized
     *         AC-coefficients of the given block
     */
    RunLevelI[] runLengthEncode(BlockI quantBlock);

    /**
     * Huffman encodes the given block of quantization coefficients.
     * The difference of the DC coefficients of this block and its previous
     * block is variable length coded. The AC coefficients are first run length
     * coded into a (RUN,LEVEL)-sequence. The RUN-LEVEL combinations are afterwards
     * Huffman coded using a kind of modified Huffman table for DC and AC values
     * for luminance and chrominance respectively. The entropy encoded data is
     * finally written to the the given buffered output stream.
     * @param quantBlock the block containing the quantized DCT coefficients
     * @param compType the component type the quantization block is part of. Must
     *        be one of YUVImageI.Y, YUVImageI.Cb or YUVImageI.Cr.
     * @param bos the output stream to write the entropy encoded block into
     * @exception java.io.IOException if a problem occurred while writing to the
     *            output stream.
     */
    void huffmanEncode(BlockI quantBlock, int compType, BufferedOutputStream bos) throws IOException;

    /**
     * Flushes the content of the buffer to the given stream. This is only neccessary,
     * if this entropy encoder supports a buffering of entropy encoded bits.
     * @param bos the output stream to flush the content of the buffer
     * @exception java.io.IOException if an error occurred while writing to the stream
     */
    void flushBuffer(BufferedOutputStream bos) throws IOException;
}