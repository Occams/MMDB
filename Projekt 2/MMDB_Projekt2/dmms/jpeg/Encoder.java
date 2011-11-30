package dmms.jpeg;

import dmms.jpeg.spec.*;
import java.awt.*;
import java.io.*;
import java.util.*;

/**
 * JPEG-encodes a given image using the lossy, sequential DCT-based compression
 * mode (also known as the <i>baseline process</i>).
 * @author Roland Tusch
 * @version 1.0
 */

public class Encoder {

    /** the start-of-image marker **/
    private static final byte[] SOI = {(byte) 0xff,(byte) 0xd8};
    /** the define-quantization-table marker **/
    private static final byte[] DQT = {(byte) 0xff,(byte) 0xdb};
    /** the define-huffman-table marker **/
    private static final byte[] DHT = {(byte) 0xff,(byte) 0xc4};
    /** the comment marker **/
    private static final byte[] COM = {(byte) 0xff,(byte) 0xfe};
    /** the JFIF marker **/
    private static final byte[] JFIF = {(byte) 0xff,(byte) 0xe0};
    /** the start-of-frame marker **/
    private static final byte[] SOF = {(byte) 0xff,(byte) 0xc0};
    /** the start-of-scan marker **/
    private static final byte[] SOS = {(byte) 0xff,(byte) 0xda};
    /** the end-of-image marker **/
    private static final byte[] EOI = {(byte) 0xff,(byte) 0xd9};
    /** the default encoder comment string **/
    private static final String ENCODER_INFO = "JPEG Encoder 2001, (C) by Roland Tusch, University Klagenfurt (ITEC)";
    /** the component IDs used to label the components **/
    private static final int[] COMPONENT_IDS = {1,2,3};

    Image rgbImg;
    int samplingRatio;
    int qualityFactor;
    ImageScalatorI imgScalator;
    ColorSpaceConverterI colorConverter;
    SubSamplerI subSampler;
    BlockGeneratorI blockGenerator;
    DCTI dct;
    QuantizationI quantizer;
    EntropyCoderI entropyCoder;

    /**
     * Constructs a new JPEG encoder which does no downsampling of the
     * chrominance components and uses the default quality factor of 80
     * for quality scaling.
     * @param rgbImg the RGB image to compress
     */
    public Encoder(Image rgbImg) {
        this(rgbImg,SubSamplerI.YUV_444,QuantizationI.DEFAULT_QUALITY_FACTOR);
    }

    /**
     * Creates a JPEG encoder which compresses the given RGB image using
     * the given sampling ratio and quality factor.
     * @param rgbImg the RGB image to compress
     * @param samplingRatio the sampling ratio to use for downsampling. Must
     *        be one of SubSamplerI.YUV_444, SubSamplerI.YUV_422 or SubSamplerI.YUV_420.
     * @param qualityFactor the quality factor to use for scaling the quality
     *        of the compressed image during quantization. Must be a value between
     *        [0,100].
     */
    public Encoder(Image rgbImg, int samplingRatio, int qualityFactor) {
        if (samplingRatio >= SubSamplerI.YUV_444 && samplingRatio <= SubSamplerI.YUV_420) {
            this.rgbImg = rgbImg;
            this.samplingRatio = samplingRatio;
            this.qualityFactor = qualityFactor;
            initEncoder();
        }
        else
            throw new IllegalArgumentException("Invalid subsampling ratio.");
    }

    /**
     * Initializes the components of this JPEG encoder.
     */
    protected void initEncoder() {
        imgScalator = new ImageScalator();
        
        colorConverter = new ColorSpaceConverter();
        subSampler = new SubSampler();
        blockGenerator = new BlockGenerator();
        dct = new StandardDCT();
        quantizer = new Quantizer(qualityFactor);
        entropyCoder = new HuffmanCoder();
        
    }

    /**
     * Compresses the initially supplied image data and writes it
     * into the given output stream.
     * @param bos the output stream to write the JPEG-compressed
     *        image data into
     * @exception java.io.IOException if an error occurred while writing
     *            the compressed data into the given output stream
     */
    public void compress(BufferedOutputStream bos) throws IOException {
        bos.write(SOI);
        writeHeaderSegments(bos);
        writeCompressedImageData(bos);
        bos.write(EOI);
        bos.flush();
    }

    /**
     * Writes the JPEG header segments to the given output stream.
     */
    protected void writeHeaderSegments(BufferedOutputStream bos) throws IOException {
        writeApplicationDataSegment(bos);
        writeComment(bos);
        writeDQTSegment(bos);
        writeSOFHeader(bos);
        writeDHTSegment(bos);
        writeSOSHeader(bos);
    }

    /**
     * Writes the define-quantization-table segment to the given output stream.
     */
    protected void writeDQTSegment(BufferedOutputStream bos) throws IOException {
        if(quantizer == null) System.out.println("quanti1 null");
        byte[] dqtSegment = new byte[134];
        dqtSegment[0] = DQT[0];
        dqtSegment[1] = DQT[1];
        dqtSegment[2] = (byte) 0;
        dqtSegment[3] = (byte) 132;
        if(quantizer == null) System.out.println("quanti2 null");
        int currPos = 4;
        if(quantizer == null) System.out.println("quanti3 null");
        for (int i=0;i < 2;i++) {
            dqtSegment[currPos++] = (byte) ((0 << 4) + i);
            int[] quantTable = (i == 0 ? quantizer.getQuantumLuminance():
                                         quantizer.getQuantumChrominance());
            for (int j=0;j < quantTable.length;j++) {
                dqtSegment[currPos++] = (byte) quantTable[EntropyCoderI.ZIGZAG_ORDER[j]];
            }
        }
        bos.write(dqtSegment);
    }

    /**
     * Writes the define-huffman-table segment to the given output stream.
     */
    protected void writeDHTSegment(BufferedOutputStream bos) throws IOException {
        Vector bits = new Vector();
        bits.addElement(EntropyCoderI.BITS_DC_LUMINANCE);
        bits.addElement(EntropyCoderI.BITS_AC_LUMINANCE);
        bits.addElement(EntropyCoderI.BITS_DC_CHROMINANCE);
        bits.addElement(EntropyCoderI.BITS_AC_CHROMINANCE);
        Vector vals = new Vector();
        vals.addElement(EntropyCoderI.VALS_DC_LUMINANCE);
        vals.addElement(EntropyCoderI.VALS_AC_LUMINANCE);
        vals.addElement(EntropyCoderI.VALS_DC_CHROMINANCE);
        vals.addElement(EntropyCoderI.VALS_AC_CHROMINANCE);

        byte[] dhtSeg1, dhtSeg2, dhtSeg3, dhtSeg4;
        int index = 4, bitsIndex = 4, valsIndex;
        // all bits-arrays are of the same length
        dhtSeg1 = new byte[EntropyCoderI.BITS_DC_LUMINANCE.length];
        dhtSeg4 = new byte[4];
        dhtSeg4[0] = DHT[0];
        dhtSeg4[1] = DHT[1];
        for (int i=0;i < bits.size();i++) {
            int bytes = 0;
            int[] currBits = (int[]) bits.elementAt(i);
            dhtSeg1[index++ - bitsIndex] = (byte) currBits[0];
            for (int j=1;j < currBits.length;j++) {
                dhtSeg1[index++ - bitsIndex] = (byte) currBits[j];
                bytes += currBits[j];
            }
            valsIndex = index;
            dhtSeg2 = new byte[bytes];
            int[] currVals = (int[]) vals.elementAt(i);
            for (int j=0;j < bytes;j++) {
                dhtSeg2[index++ - valsIndex] = (byte) currVals[j];
            }
            dhtSeg3 = new byte[index];
            System.arraycopy(dhtSeg4,0,dhtSeg3,0,bitsIndex);
            System.arraycopy(dhtSeg1,0,dhtSeg3,bitsIndex,dhtSeg1.length);
            System.arraycopy(dhtSeg2,0,dhtSeg3,valsIndex,bytes);
            dhtSeg4 = dhtSeg3;
            bitsIndex = index;
        }
        // set the length of the DHT segment
        dhtSeg4[2] = (byte) (((index-2) >> 8) & 0xff);
        dhtSeg4[3] = (byte) ((index-2) & 0xff);
        bos.write(dhtSeg4);
    }

    /**
     * Writes the comment segment to the given output stream.
     */
    protected void writeComment(BufferedOutputStream bos) throws IOException {
        byte[] commentSeg = new byte[4 + ENCODER_INFO.length()];
        commentSeg[0] = COM[0];
        commentSeg[1] = COM[1];
        commentSeg[2] = (byte) (((ENCODER_INFO.length()+2) >> 8) & 0xff);
        commentSeg[3] = (byte) ((ENCODER_INFO.length()+2) & 0xff);
        System.arraycopy(ENCODER_INFO.getBytes(),0,commentSeg,4,ENCODER_INFO.length());
        bos.write(commentSeg);
    }

    /**
     * Writes the application data segment to the given output stream.
     * The only application data segment used here is the JFIF header segment.
     */
    protected void writeApplicationDataSegment(BufferedOutputStream bos) throws IOException {
        byte[] jfifHeader = new byte[18];
        jfifHeader[0] = JFIF[0];
        jfifHeader[1] = JFIF[1];
        jfifHeader[2] = 0x00;
        jfifHeader[3] = 0x10;
        jfifHeader[4] = 0x4a;
        jfifHeader[5] = 0x46;
        jfifHeader[6] = 0x49;
        jfifHeader[7] = 0x46;
        jfifHeader[8] = 0x00;
        jfifHeader[9] = 0x01;
        jfifHeader[10] = 0x00;
        jfifHeader[11] = 0x00;
        jfifHeader[12] = 0x00;
        jfifHeader[13] = 0x01;
        jfifHeader[14] = 0x00;
        jfifHeader[15] = 0x01;
        jfifHeader[16] = 0x00;
        jfifHeader[17] = 0x00;
        bos.write(jfifHeader);
    }

    /**
     * Writes the start-of-frame header to the given output stream.
     */
    protected void writeSOFHeader(BufferedOutputStream bos) throws IOException {
        byte[] sofHeader = new byte[19];
        sofHeader[0] = SOF[0];
        sofHeader[1] = SOF[1];
        sofHeader[2] = (byte) 0;
        sofHeader[3] = (byte) 17;
        sofHeader[4] = (byte) 8;    // sample precision
        sofHeader[5] = (byte) ((rgbImg.getHeight(null) >> 8) & 0xff);
        sofHeader[6] = (byte) (rgbImg.getHeight(null) & 0xff);
        sofHeader[7] = (byte) ((rgbImg.getWidth(null) >> 8) & 0xff);
        sofHeader[8] = (byte) (rgbImg.getWidth(null) & 0xff);
        sofHeader[9] = (byte) COMPONENT_IDS.length; // number of components
        int[] horizSampFactors = computeHorizontalSamplingFactors();
        int[] vertSampFactors = computeVerticalSamplingFactors();
        int index = 10;
        for (int i=0;i < sofHeader[9];i++) {
            sofHeader[index++] = (byte) COMPONENT_IDS[i];
            sofHeader[index++] = (byte) ((horizSampFactors[i] << 4) + vertSampFactors[i]);
            sofHeader[index++] = (byte) (i == 0 ? 0:1);
        }
        bos.write(sofHeader);
    }

    /**
     * Determines the horizonal sampling factors for the components
     * from the initially specified sampling ratio.
     */
    protected int[] computeHorizontalSamplingFactors() {
        int[] hSampFactors = {1,1,1};
        if (samplingRatio != SubSamplerI.YUV_444)
            hSampFactors[0] = 2;
        return hSampFactors;
    }

    /**
     * Determines the vertical sampling factors for the components
     * from the initially specified sampling ratio.
     */
    protected int[] computeVerticalSamplingFactors() {
        int[] vSampFactors = {1,1,1};
        if (samplingRatio != SubSamplerI.YUV_444) {
            vSampFactors[0] = 2;
            if (samplingRatio == SubSamplerI.YUV_422) {
                vSampFactors[1] = 2;
                vSampFactors[2] = 2;
            }
        }
        return vSampFactors;
    }

    /**
     * Writes the start-of-scan header to the given output stream.
     */
    protected void writeSOSHeader(BufferedOutputStream bos) throws IOException {
        byte[] sosHeader = new byte[14];
        sosHeader[0] = SOS[0];
        sosHeader[1] = SOS[1];
        sosHeader[2] = (byte) 0;
        sosHeader[3] = (byte) 12;
        sosHeader[4] = (byte) COMPONENT_IDS.length;
        int index = 5;
        for (int i=0;i < sosHeader[4];i++) {
            sosHeader[index++] = (byte) COMPONENT_IDS[i];
            int tableIndex = (i == 0 ? 0:1);
            // 0 for luminance tables, 1 for chrominance tables
            sosHeader[index++] = (byte) ((tableIndex << 4) + tableIndex);
        }
        sosHeader[index++] = (byte) 0;  // Ss - start of spectral selection
        sosHeader[index++] = (byte) 63; // Se - end of spectral selection
        sosHeader[index++] = (byte) 0;  // Ah + Al
        bos.write(sosHeader);
    }

    /**
     * Performs a JPEG compression of the initially supplied image and writes
     * the compressed data into the given output stream.
     */
    protected void writeCompressedImageData(BufferedOutputStream bos) throws IOException {
        Image scaledImg = imgScalator.scaleImage(rgbImg,samplingRatio);
        // showImage(scaledImg);
    
        YUVImageI yuvImg = colorConverter.convertRGBToYUV(scaledImg);
        yuvImg = subSampler.downSample(yuvImg,samplingRatio);
        MinimumCodedUnitI[] minCodedUnits = blockGenerator.generateMinimumCodedUnits(yuvImg);
        System.out.print("\nNumber of minimum coded units: " + minCodedUnits.length);
        for (int i=0;i < minCodedUnits.length;i++) {
            // System.out.print("\nProcessing MCU: " + i);
            RegionI[] regions = minCodedUnits[i].getRegions();
            for (int j=0;j < regions.length;j++) {
                // System.out.print("\n  Region: " + getRegionType(regions[j]));
                BlockI[] blocks = regions[j].getBlocks();
                for (int k=0;k < blocks.length;k++) {
                    // System.out.print("\n    Block " + k + ": --------------");
                    // showBlock(blocks[k],"Component block (" + regions[j].getType() + "):");
                    DCTBlockI dctBlock = dct.forward(blocks[k]);
                    // showDCTBlock(dctBlock);
                    BlockI quantBlock = quantizer.quantizeBlock(dctBlock,regions[j].getType());
                    // showBlock(quantBlock,"Quantized block:");
                    entropyCoder.huffmanEncode(quantBlock,regions[j].getType(),bos);
                }
            }
        }
        entropyCoder.flushBuffer(bos);
   
    }

    protected void showImage(Image img) {
        javax.swing.JFrame imgFrame = new javax.swing.JFrame("Scaled image");
        javax.swing.JLabel imgLbl = new javax.swing.JLabel(new javax.swing.ImageIcon(img));
        imgFrame.getContentPane().add(BorderLayout.CENTER,imgLbl);
        imgFrame.setDefaultCloseOperation(imgFrame.HIDE_ON_CLOSE);
        imgFrame.pack();
        imgFrame.setVisible(true);
    }

    protected String getRegionType(RegionI region) {
        switch (region.getType()) {
            case YUVImageI.Y:   return "luminance";
            case YUVImageI.Cb:  return "chrominance blue";
            default:            return "chrominance red";
        }
    }

    protected void showDCTBlock(DCTBlockI dctBlock) {
        System.out.print("\n    DCT-Block:\n\t");
        double[][] blockData = dctBlock.getData();
        for (int i=0;i < blockData.length;i++) {
            for (int j=0;j < blockData[i].length;j++) {
                System.out.print(blockData[i][j] + "\t");
            }
            System.out.print("\n\t");
        }
    }

    protected void showBlock(BlockI quantBlock,String msg) {
        System.out.print("\n    " + msg + "\n\t");
        int[][] blockData = quantBlock.getData();
        for (int i=0;i < blockData.length;i++) {
            for (int j=0;j < blockData[i].length;j++) {
                System.out.print(blockData[i][j] + "\t");
            }
            System.out.print("\n\t");
        }
    }
}