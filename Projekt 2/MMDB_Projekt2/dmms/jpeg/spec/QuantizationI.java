package dmms.jpeg.spec;

/**
 * Specifies the funcionality required for quantising a given block of DCT
 * coefficients. During quanitization, each DCT coefficient is devided by a
 * quantization coefficient. The quantization matrices for luminance and
 * chrominance have to be specified. The quality factor increases the
 * compression ratio, the lower the factor is. There is always a
 * tradeoff between quality and compression ratio.
 * @author Roland Tusch
 * @version 1.0
 */

public interface QuantizationI {

    /** The normative quantization matrix for luminance blocks. **/
    static final int[] QUANTUM_LUMINANCE = { 16,  11,  10,  16,  24,  40,  51,  61,
                                             12,  12,  14,  19,  26,  58,  60,  55,
                                             14,  13,  16,  24,  40,  57,  69,  56,
                                             14,  17,  22,  29,  51,  87,  80,  62,
                                             18,  22,  37,  56,  68, 109, 103,  77,
                                             24,  35,  55,  64,  81, 104, 113,  92,
                                             49,  64,  78,  87, 103, 121, 120, 101,
                                             72,  92,  95,  98, 112, 100, 103,  99};

    /** The normative quantization matrix for chrominance blocks. **/
    static final int[] QUANTUM_CHROMINANCE = { 17, 18, 24, 47, 99, 99, 99, 99,
                                               18, 21, 26, 66, 99, 99, 99, 99,
                                               24, 26, 56, 99, 99, 99, 99, 99,
                                               47, 66, 99, 99, 99, 99, 99, 99,
                                               99, 99, 99, 99, 99, 99, 99, 99,
                                               99, 99, 99, 99, 99, 99, 99, 99,
                                               99, 99, 99, 99, 99, 99, 99, 99,
                                               99, 99, 99, 99, 99, 99, 99, 99};

    /** the default quality factor to use for quality scaling **/
    static final int DEFAULT_QUALITY_FACTOR = 80;

    /**
     * Delivers the used quantization matrix for luminance as linear array of
     * coefficients (= sequence of rows). The quantization values must have already
     * been scaled with the quality scaling factor.
     * @return the quantisation matrix used for quantizing luminance coefficients
     */
    int[] getQuantumLuminance();

    /**
     * Delivers the used quantization matrix for chrominance as linear array of
     * coefficients (= sequence of rows). The quantization values must have already
     * been scaled with the quality scaling factor.
     * @return the quantisation matrix used for quantizing chrominance coefficients
     */
    int[] getQuantumChrominance();

    /**
     * Quantises the given DCT block of specified component type, using the
     * corresponding quantization matrix and quality scaling factor.
     * @param dctBlock the block of DCT coefficients to quantize
     * @param compType the type of component from which the DCT block is part of.
     *        Must be one of YUVImageI.Y, YUVImageI.Cb or YUVImageI.Cr.
     * @return a block containing the quantized DCT coefficients
     */
    BlockI quantizeBlock(DCTBlockI dctBlock, int compType);

    /**
     * Sets the quality factor used for quality scaling to the given factor.
     * The quality factor must be a value with the intervall [1 .. 100]. 1 means
     * extremely bad quality, but extremely good compression, 100 means good
     * quality, but nearly no compression. Usually, the default quality factor
     * of 80 delivers good results.
     * @param qualityFactor the new quality factor to use for quality scaling
     */
    void setQualityFactor(int qualityFactor);
}