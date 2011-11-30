package dmms.jpeg.test;

import dmms.jpeg.spec.*;
import dmms.jpeg.*;
import java.awt.*;
import java.io.*;

/**
 * Main program that allows to JPEG-encode a given image. This is
 * a command line based JPEG encoding program.
 * @author Roland Tusch
 * @version 1.0
 */

public class JPEGEncoder {

    private static final String SAMPLING_OPTION = "-s";
    private static final String QUALITY_OPTION = "-q";
    private static final String OUTPUT_FILE_OPTION = "-o";

    private static final String YUV_444 = "4:4:4";
    private static final String YUV_422 = "4:2:2";
    private static final String YUV_420 = "4:2:0";

    public static void main(String[] args) {
        try {
            if (args.length == 1 || args.length == 3 ||  args.length == 5 || args.length == 7) {
                String imgFile = "", compressedImgFile = "", samplingString = YUV_444;
                int samplingRatio = SubSamplerI.YUV_444;
                int qualityFactor = QuantizationI.DEFAULT_QUALITY_FACTOR;
                for (int i=0;i < args.length;i+=2) {
                    if (args[i].equalsIgnoreCase(SAMPLING_OPTION)) {
                        samplingString = args[i+1];
                        samplingRatio = getSamplingRatio(args[i+1]);
                    }
                    else if (args[i].equalsIgnoreCase(QUALITY_OPTION))
                        qualityFactor = Integer.parseInt(args[i+1]);
                    else if (args[i].equalsIgnoreCase(OUTPUT_FILE_OPTION))
                        compressedImgFile = args[i+1];
                    else
                        imgFile = args[i];
                }
                if (compressedImgFile.equals(""))
                    compressedImgFile = imgFile.substring(0,imgFile.indexOf(".")) + ".jpg";
                System.out.print("\nimage file to compress: " + imgFile);
                System.out.print("\ncompressed image file: " + compressedImgFile);
                Encoder encoder = new Encoder(getImage(imgFile),samplingRatio,qualityFactor);
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(compressedImgFile));
                long startTime = System.currentTimeMillis();
                encoder.compress(bos);
                long endTime = System.currentTimeMillis();
                bos.flush();
                bos.close();
                showStats(samplingString,qualityFactor,imgFile,
                          compressedImgFile,endTime - startTime);
            }
            else
                System.out.print("\nUsage: java dmms.jpeg.test.JPEGEncoder [-s <samplingRatio>] [-q <qualityFactor>] [-o <outputFile>] <imgFile>");
        }
        catch (Exception e) {
            System.out.print("\nException occurred: " + e.getMessage() + "\n");
            e.printStackTrace();
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

    private static void showStats(String samplingRatio, int qualityFactor, String imgFileName,
                                  String compressedFileName, long elapsedTime) {
        System.out.print("\n----------- DMMS JPEG-Encoder ----------");
        System.out.print("\nInput File: " + imgFileName);
        System.out.print("\nSubsampling Ratio: " + samplingRatio);
        System.out.print("\nQuality Factor: " + qualityFactor);
        System.out.print("\nOutput File: " + compressedFileName);
        System.out.print("\nElapsed compression time: ");
        long minutes = elapsedTime/1000/60;
        elapsedTime -= minutes*60*1000;
        long seconds = elapsedTime/1000;
        elapsedTime -= seconds*1000;
        long hundreds = elapsedTime/10;
        System.out.print(minutes + ":" + seconds + ":" + hundreds + " (mm:ss:hh)\n");
    }
}