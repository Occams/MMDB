package dmms.jpeg.test;

import dmms.jpeg.spec.*;
import dmms.jpeg.*;
import java.awt.*;
import javax.swing.*;

/**
 * Main class for testing <b>partial>/> JPEG implementations
 *
 * @author Roland Tusch
 * @version 1.0
 */

public class JPEGTest {

    private static final String SAMPLING_OPTION = "-s";
    private static final String QUALITY_OPTION = "-q";

    private static final String YUV_444 = "4:4:4";
    private static final String YUV_422 = "4:2:2";
    private static final String YUV_420 = "4:2:0";

    public static void main(String[] args) {
        try {
            if (args.length == 1 || args.length == 3 ||  args.length == 5) {
                String imgFile = "", samplingString = YUV_444;
                int samplingRatio = SubSamplerI.YUV_444;
                int qualityFactor = QuantizationI.DEFAULT_QUALITY_FACTOR;
                // parse arguments
                for (int i=0;i < args.length;i+=2) {
                    if (args[i].equalsIgnoreCase(SAMPLING_OPTION)) {
                        samplingString = args[i+1];
                        samplingRatio = getSamplingRatio(args[i+1]);
                    }
                    else if (args[i].equalsIgnoreCase(QUALITY_OPTION))
                        qualityFactor = Integer.parseInt(args[i+1]);
                    else
                        imgFile = args[i];
                }
                processImage(getImage(imgFile),samplingRatio,samplingString,qualityFactor);
            }
            else
                System.out.print("\nUsage: java dmms.jpeg.test.JPEGTest [-s <samplingRatio>] [-q <qualityFactor>] <imgFile>");
        }
        catch (Exception e) {
            System.out.print("\nException occurred: " + e.getMessage());
        }
    }

    protected static void processImage(Image testImg, int samplingRatio, String samplingString, int qualityFactor) throws Exception {
        ImageScalatorI scalator = new ImageScalator();
        Image scaledImg = scalator.scaleImage(testImg,samplingRatio);
        showImage(scaledImg);

    /* ------------ remove comments from parts where implementations exist ------------*/

        ColorSpaceConverterI colorConverter = new ColorSpaceConverter();
        YUVImageI yuvImg = colorConverter.convertRGBToYUV(scaledImg);

        SubSamplerI subSampler = new SubSampler();
        yuvImg = subSampler.downSample(yuvImg,samplingRatio);
        showImageInfo(yuvImg,samplingString);

        BlockGeneratorI blockGenerator = new BlockGenerator();
        MinimumCodedUnitI[] minCodedUnits = blockGenerator.generateMinimumCodedUnits(yuvImg);
        System.out.print("\nNr. of MCUs generated: " + minCodedUnits.length);
    
        DCTI dct = new StandardDCT();
        QuantizationI quantizer = new Quantizer(qualityFactor);
   
        EntropyCoderI entropyCoder = new HuffmanCoder();
    
    
        for (int i=0;i < minCodedUnits.length;i++) {
            System.out.print("\nProcessing MCU: " + i);
            RegionI[] regions = minCodedUnits[i].getRegions();
            for (int j=0; j < regions.length;j++) {
                System.out.print("\n  Region: " + getRegionType(regions[j]));
                BlockI[] blocks = regions[j].getBlocks();
                for (int k=0;k < blocks.length;k++) {
                    System.out.print("\n    Block " + k + ": --------------");
                    showBlock(blocks[k],"Component block:");
                    DCTBlockI dctBlock = dct.forward(blocks[k]);
                    showDCTBlock(dctBlock);
                    BlockI quantBlock = quantizer.quantizeBlock(dctBlock,regions[j].getType());
                    showBlock(quantBlock,"Quantized block:");
                    RunLevelI[] runLevels = entropyCoder.runLengthEncode(quantBlock);
                    showRunLevels(runLevels);
                    System.out.print("\n    -------------------------------");
                }
            }
        }
    }

    private static int getSamplingRatio(String ratio) {
        if (ratio.equals(YUV_444))
            return SubSamplerI.YUV_444;
        else if (ratio.equals(YUV_422))
            return SubSamplerI.YUV_422;
        else if (ratio.equals(YUV_420))
            return SubSamplerI.YUV_420;
        throw new IllegalArgumentException("Invalid subsampling ratio " + ratio + ".");
    }

    private static Image getImage(String imgFile) throws InterruptedException {
        Image rgbImg = Toolkit.getDefaultToolkit().createImage(imgFile);
        Frame f = new Frame();
        MediaTracker tracker = new MediaTracker(f);
        tracker.addImage(rgbImg,0);
        tracker.waitForID(0);
        return rgbImg;
    }

    private static void showImage(Image img) {
        JFrame imgFrame = new JFrame("Scaled image");
        JLabel imgLbl = new JLabel(new javax.swing.ImageIcon(img));
        imgFrame.getContentPane().add(BorderLayout.CENTER,imgLbl);
        imgFrame.setDefaultCloseOperation(imgFrame.HIDE_ON_CLOSE);
        imgFrame.pack();
        imgFrame.setVisible(true);
    }

    protected static void showImageInfo(YUVImageI yuvImg, String samplingString) {
        System.out.print("\n--- YUV image information: ---");
        System.out.print("\nSampling ratio: " + samplingString);
        System.out.print("\nSize of the components: ");
        System.out.print("\nY: " + yuvImg.getComponent(yuvImg.Y).getSize());
        System.out.print("\nCb: " + yuvImg.getComponent(yuvImg.Cb).getSize());
        System.out.print("\nCr: " + yuvImg.getComponent(yuvImg.Cr).getSize());
        System.out.print("\n-------------------------------");
    }

    protected static String getRegionType(RegionI region) {
        switch (region.getType()) {
            case YUVImageI.Y:   return "luminance";
            case YUVImageI.Cb:  return "chrominance blue";
            default:            return "chrominance red";
        }
    }

    protected static void showBlock(BlockI block, String msg) {
        System.out.print("\n    " + msg + "\n\t");
        int[][] blockData = block.getData();
        for (int i=0;i < blockData.length;i++) {
            for (int j=0;j < blockData[i].length;j++) {
                System.out.print(blockData[i][j] + "\t");
            }
            System.out.print("\n\t");
        }
    }

    protected static void showDCTBlock(DCTBlockI dctBlock) {
        System.out.print("\n    DCT-Block:\n\t");
        double[][] blockData = dctBlock.getData();
        for (int i=0;i < blockData.length;i++) {
            for (int j=0;j < blockData[i].length;j++) {
                System.out.print(blockData[i][j] + "\t");
            }
            System.out.print("\n\t");
        }
    }

    protected static void showRunLevels(RunLevelI[] runLevels) {
        System.out.print("\n    RUN-LEVELS of AC coefficients:\n\t");
        for (int i=0;i < runLevels.length;i++) {
            System.out.print("(" + runLevels[i].getRun() + "," + runLevels[i].getLevel() + ")");
            if (i < runLevels.length-1)
                System.out.print(", ");
        }
    }
}
